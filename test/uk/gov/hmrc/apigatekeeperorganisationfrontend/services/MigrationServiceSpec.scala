/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apigatekeeperorganisationfrontend.services

import scala.concurrent.ExecutionContext.Implicits.global

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationIdFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.{LookupResponse, VatRegisteredCompany}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors.{OrganisationConnectorMockModule, TpoConnectorMockModule, VatRegisteredCompaniesConnectorMockModule}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.repository.MigrationRepositoryMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationStatus._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{AsyncHmrcSpec, MigrationFixtures}

class MigrationServiceSpec extends AsyncHmrcSpec with TpoConnectorMockModule with VatRegisteredCompaniesConnectorMockModule with OrganisationConnectorMockModule
    with MigrationRepositoryMockModule with ApplicationIdFixtures {

  trait Setup extends MigrationFixtures {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = new MigrationService(TpoConnectorMock.aMock, MigrationRepositoryMock.aMock, VatRegisteredCompaniesConnectorMock.aMock, OrganisationConnectorMock.aMock)

    val questionType = "vat-registration-number"
    val answer       = "123456789"
    val record       = MigrationRecord(answer, List.empty, Unverified, questionType, None)
  }

  "fetchAll" should {
    "return all records from the repository" in new Setup {
      MigrationRepositoryMock.FetchAll.willReturn(List(record))

      val result = await(underTest.fetchAll())

      result shouldBe List(record)
    }
  }

  "loadData" should {
    "fetch applications and save them to the repository" in new Setup {
      val appsByAnswer = List(ApplicationsByAnswer(answer, List(applicationIdOne)))
      TpoConnectorMock.FetchApplicationsByAnswer.willReturn(appsByAnswer)
      MigrationRepositoryMock.Save.willReturn(List(record))

      val result = await(underTest.loadData(questionType))

      result shouldBe List(record)
    }
  }

  "fetch" should {
    "return a specific record from the repository" in new Setup {
      MigrationRepositoryMock.Fetch.willReturn(Some(record))

      val result = await(underTest.fetch(questionType, answer))

      result shouldBe Some(record)
    }
  }

  "processVat" should {
    "update record to Verified when VAT number is found" in new Setup {
      val vatCompany     = VatRegisteredCompany("Existing Company", answer)
      val lookupResponse = LookupResponse(Some(vatCompany))

      MigrationRepositoryMock.FetchUnchecked.willReturn(List(record))
      VatRegisteredCompaniesConnectorMock.LookupVatNumber.willReturn(lookupResponse)
      MigrationRepositoryMock.Update.willReturn(record.copy(status = Verified, details = Some("Existing Company")))

      val result = await(underTest.processVat(1))

      result.head.status shouldBe Verified
      result.head.details shouldBe Some("Existing Company")
    }

    "update record to Unverified when VAT number is not found" in new Setup {
      val lookupResponse = LookupResponse(None)

      MigrationRepositoryMock.FetchUnchecked.willReturn(List(record))
      VatRegisteredCompaniesConnectorMock.LookupVatNumber.willReturn(lookupResponse)
      MigrationRepositoryMock.Update.willReturn(record.copy(status = Unverified))

      val result = await(underTest.processVat(1))

      result.head.status shouldBe Unverified
    }
  }

  "processCompaniesHouse" should {
    "update record to Verified when Company is found" in new Setup {
      private val companyName = "Existing Company"
      val company             = CompaniesHouseCompanyProfile(companyName)

      MigrationRepositoryMock.FetchUnchecked.willReturn(List(record))
      OrganisationConnectorMock.FetchByCompanyNumber.willReturn(Some(company))
      MigrationRepositoryMock.Update.willReturn(record.copy(status = Verified, details = Some(companyName)))

      val result = await(underTest.processCompaniesHouse(1))

      result.head.status shouldBe Verified
      result.head.details shouldBe Some(companyName)
    }

    "update record to Unverified when Company is not found" in new Setup {
      MigrationRepositoryMock.FetchUnchecked.willReturn(List(record))
      OrganisationConnectorMock.FetchByCompanyNumber.willReturn(None)
      MigrationRepositoryMock.Update.willReturn(record.copy(status = Unverified))

      val result = await(underTest.processCompaniesHouse(1))

      result.head.status shouldBe Unverified
    }
  }
}
