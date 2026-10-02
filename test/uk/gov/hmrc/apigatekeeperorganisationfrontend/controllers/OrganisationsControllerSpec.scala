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

import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import uk.gov.hmrc.apiplatform.modules.common.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apigatekeeperorganisationfrontend.config.AppConfig
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.OrganisationServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.*
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

    val listPage    = app.injector.instanceOf[OrganisationsListPage]
    val detailsPage = app.injector.instanceOf[OrganisationDetailsPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val appConfig   = app.injector.instanceOf[AppConfig]
    val controller  = new OrganisationsController(mcc, listPage, detailsPage, OrganisationServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)
  }

  "GET /organisations" should {
    val fakeRequest = FakeRequest("GET", "/organisations")

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

      val result = controller.organisationsView(FakeRequest("GET", s"/?organisationName=${standardOrg.organisationName.value}"))

      status(result) shouldBe Status.OK
      contentAsString(result) should include(standardOrg.organisationName.value)

      OrganisationServiceMock.SearchOrganisations.verifyCalled(Seq("organisationName" -> standardOrg.organisationName.value))
    }

    "return 200 with empty list for name filter and no matching orgs and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.SearchOrganisations.succeed(List.empty)

      val result = controller.organisationsView(FakeRequest("GET", "/?organisationName=test"))

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

  "GET /organisations/:oid" should {
    val fakeRequest = FakeRequest("GET", "/organisations/123456")

    "return 200 with the organisation" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchWithAllMembersDetails.succeed(extendedOrgWithMultipleApplications)

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(standardOrg.organisationName.value)

      contentAsString(result) should include(prodAppWithLastAccess.details.name)
      contentAsString(result) should include("Production")
      contentAsString(result) should include("03 January 2020")

      contentAsString(result) should include(sandboxAppWithNoLastAccess.details.name)
      contentAsString(result) should include("Sandbox")
      contentAsString(result) should include("No API called")

      contentAsString(result) should include("firstName1 lastName1")
      val membersTile = Jsoup.parse(contentAsString(result)).getElementById("members-tile")
      membersTile.text should not include "Joe Bloggs"

      contentAsString(result) should include("Responsible individuals")
      contentAsString(result) should include("Joe Bloggs")

      contentAsString(result) should include("Date registered")
      contentAsString(result) should include("02 January 2020")

      contentAsString(result) should include("Company type")
      contentAsString(result) should include("UK limited company")
    }

    "return 200 with the organisation and its extra organisation data" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchWithAllMembersDetails.succeed(extendedOrgWithExtraData)

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.OK

      contentAsString(result) should include("Company registration number")
      contentAsString(result) should include("08947216")

      contentAsString(result) should include("Registered address")
      contentAsString(result) should include("Easy Soft Limited")
      contentAsString(result) should include("Unit 12")
      contentAsString(result) should include("PO Box 42")
      contentAsString(result) should include("Stoxley Industrial Park")
      contentAsString(result) should include("Stoxley Park Avenue")
      contentAsString(result) should include("Stoxley")
      contentAsString(result) should include("Canningley")
      contentAsString(result) should include("West Yorkshire")
      contentAsString(result) should include("CI8 2JS")
      contentAsString(result) should include("United Kingdom")

      contentAsString(result) should include("Website")
      contentAsString(result) should include("http://easysoft.co.uk")

      val companyNumberLink = Jsoup.parse(contentAsString(result)).getElementById("company-number")
      companyNumberLink.attr("href") shouldBe s"${appConfig.companiesHouseCompanyPageUrl}/08947216"
    }

    "return 200 with an organisation with no extra organisation data" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchWithAllMembersDetails.succeed(extendedOrgWithMinimalData)

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(orgWithMinimalData.organisationName.value)
      contentAsString(result) should include("No applications")
      contentAsString(result) should include("No organisation members")

      contentAsString(result) should include("Date registered")
      contentAsString(result) should include("02 January 2020")

      contentAsString(result) should include("Company type")
      contentAsString(result) should include("Sole trader")

      contentAsString(result) shouldNot include("Company registration number")
      contentAsString(result) shouldNot include("Registered address")
      contentAsString(result) shouldNot include("Website")
    }

    "return 404" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchWithAllMembersDetails.fails()

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
    }

    "return 200 for Ldap auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.succeeds
      OrganisationServiceMock.FetchWithAllMembersDetails.succeed(extendedOrg)

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "return 403 for incorrect auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.notAuthorised

      val result = controller.organisationView(OrganisationId.random)(fakeRequest)

      status(result) shouldBe Status.FORBIDDEN
    }
  }
}
