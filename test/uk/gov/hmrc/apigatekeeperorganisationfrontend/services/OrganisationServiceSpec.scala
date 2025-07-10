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

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors.OrganisationConnectorMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{AsyncHmrcSpec, OrganisationFixtures}

class OrganisationServiceSpec extends AsyncHmrcSpec with OrganisationConnectorMockModule {

  trait Setup extends FixedClock with OrganisationFixtures {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = new OrganisationService(OrganisationConnectorMock.aMock)
  }

  "searchOrganisations" should {
    "fetch all organisations" in new Setup {
      OrganisationConnectorMock.SearchOrganisations.willReturn(List(standardOrg))
      val result = await(underTest.searchOrganisations(Seq.empty))
      result shouldBe List(standardOrg)
      OrganisationConnectorMock.SearchOrganisations.verifyCalled(Seq.empty)
    }

    "fetch organisations matching the given criteria" in new Setup {
      OrganisationConnectorMock.SearchOrganisations.willReturn(List(standardOrg))
      val result = await(underTest.searchOrganisations(Seq("organisationName" -> standardOrg.organisationName.value)))
      result shouldBe List(standardOrg)
      OrganisationConnectorMock.SearchOrganisations.verifyCalled(Seq("organisationName" -> standardOrg.organisationName.value))
    }

    "return empty list when no organisations match" in new Setup {
      OrganisationConnectorMock.SearchOrganisations.willReturn(List.empty)
      val result = await(underTest.searchOrganisations(Seq("organisationName" -> standardOrg.organisationName.value)))
      result shouldBe List.empty
    }
  }
}
