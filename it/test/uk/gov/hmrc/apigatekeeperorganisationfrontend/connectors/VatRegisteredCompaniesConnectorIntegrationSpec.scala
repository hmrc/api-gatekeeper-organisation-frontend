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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application => PlayApplication, Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.apigatekeeperorganisationfrontend.AppsByAnswerFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.stubs.VatRegisteredCompaniesStub

class VatRegisteredCompaniesConnectorIntegrationSpec extends BaseConnectorIntegrationSpec with GuiceOneAppPerSuite {

  private val stubConfig = Configuration(
    "microservice.services.vat-registered-companies.port" -> stubPort
  )

  trait Setup extends AppsByAnswerFixtures {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = app.injector.instanceOf[VatRegisteredCompaniesConnector]
    val vatNumber                  = "123456789"
    val companyName                = "Example Corp"
    val lookupResponse             = LookupResponse(Some(VatRegisteredCompany(companyName, vatNumber)))

  }

  override def fakeApplication(): PlayApplication =
    GuiceApplicationBuilder()
      .configure(stubConfig)
      .in(Mode.Test)
      .build()

  "VRC Connector" should {
    "lookupVatNumber successfully" in new Setup {
      VatRegisteredCompaniesStub.LookupVatNumber.succeeds(vatNumber, lookupResponse)
      val result = await(underTest.lookupVatNumber(vatNumber))
      result shouldBe lookupResponse
    }

    "lookupVatNumber errors" in new Setup {
      VatRegisteredCompaniesStub.LookupVatNumber.fails(vatNumber, INTERNAL_SERVER_ERROR)
      intercept[UpstreamErrorResponse] {
        await(underTest.lookupVatNumber(vatNumber))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
