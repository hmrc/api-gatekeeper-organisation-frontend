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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._

import uk.gov.hmrc.apiplatform.modules.common.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.OrganisationServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{OrganisationFixtures, WithCSRFAddToken}

class OrganisationsControllerSpec extends HmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken
    with OrganisationFixtures {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends OrganisationServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val fakeRequest = FakeRequest("GET", "/")
    val page        = app.injector.instanceOf[OrganisationsListPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val controller  = new OrganisationsController(mcc, page, OrganisationServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)
  }

  "GET /" should {
    "return 200 with all organisations for no filter and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      val standardOrg2 = standardOrg.copy(id = OrganisationId.random, organisationName = OrganisationName("Organisation 2"))
      OrganisationServiceMock.SearchOrganisations.succeed(List(standardOrg, standardOrg2))

      val result = controller.organisationsView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Organisation name")
      contentAsString(result) should include("Created date")
      contentAsString(result) should include(standardOrg.organisationName.value)
      contentAsString(result) should include(standardOrg2.organisationName.value)

      OrganisationServiceMock.SearchOrganisations.verifyCalled(Seq.empty)
    }

    "return 200 with matching organisations for name filter and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.SearchOrganisations.succeed(List(standardOrg))

      val result = controller.organisationsView(fakeRequest.withFormUrlEncodedBody("organisationName" -> standardOrg.organisationName.value))

      status(result) shouldBe Status.OK
      contentAsString(result) should include(standardOrg.organisationName.value)

      OrganisationServiceMock.SearchOrganisations.verifyCalled(Seq("organisationName" -> standardOrg.organisationName.value))
    }

    "return 200 with empty list for name filter and no matching orgs and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.SearchOrganisations.succeed(List.empty)

      val result = controller.organisationsView(fakeRequest.withFormUrlEncodedBody("organisationName" -> "test"))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Organisation name")

      OrganisationServiceMock.SearchOrganisations.verifyCalled(Seq("organisationName" -> "test"))
    }

    "return 200 for Ldap auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.succeeds
      OrganisationServiceMock.SearchOrganisations.succeed(List(standardOrg))

      val result = controller.organisationsView(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "return 403 for incorrect auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.notAuthorised

      val result = controller.organisationsView(fakeRequest)

      status(result) shouldBe Status.FORBIDDEN
    }
  }
}
