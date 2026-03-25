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
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.{FixedClock, HmrcSpec}
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.WithCSRFAddToken
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.AllowListServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

class AllowListControllerSpec extends HmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends AllowListServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule
      with FixedClock {

    val mainPage          = app.injector.instanceOf[AllowListPage]
    val addPage           = app.injector.instanceOf[AddAllowListPage]
    val addConfirmPage    = app.injector.instanceOf[AddAllowListConfirmPage]
    val removePage        = app.injector.instanceOf[RemoveAllowListPage]
    val removeConfirmPage = app.injector.instanceOf[RemoveAllowListConfirmPage]
    val mcc               = app.injector.instanceOf[MessagesControllerComponents]

    val controller =
      new AllowListController(
        mcc,
        mainPage,
        addPage,
        addConfirmPage,
        removePage,
        removeConfirmPage,
        AllowListServiceMock.aMock,
        StrideAuthorisationServiceMock.aMock,
        LdapAuthorisationServiceMock.aMock
      )

    val userId                = UserId.random
    val allowList             = AllowList(userId, OrganisationName("My Org"), "Bob", "Fleming", LaxEmailAddress("bob@fleming.com"))
    val organisationAllowList = OrganisationAllowList(userId, OrganisationName("My Org"), "requestedBy", instant)
  }

  "GET allow list page" should {
    "return 200 with all organisations for no filter and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.FetchAllowList.succeed(List(allowList))
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("GET", "/allow-list"))

      val result = controller.allowListView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Organisation allow list")
      contentAsString(result) should include("Add a user to the allow list")
      contentAsString(result) should include("Users on the allow list")
      contentAsString(result) should include(allowList.organisationName.value)
      contentAsString(result) should include(allowList.email.text)

      AllowListServiceMock.FetchAllowList.verifyCalled()
    }
  }

  "GET add allow list page" should {
    "return 200 with Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("GET", "/allow-list/add"))

      val result = controller.addAllowListView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Enter the details of the user you want to add to the allow list")
      contentAsString(result) should include("This will allow the user to access the organisation registration journey on the Developer Hub.")
      contentAsString(result) should include("Developer Hub account email address")
      contentAsString(result) should include("Organisation")
      contentAsString(result) should include("Add user to the allow list")
    }
  }

  "POST add allow list page" should {
    "return 303 with Stride auth with valid data" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.CreateAllowList.succeed(organisationAllowList)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", "/allow-list/add").withFormUrlEncodedBody("email" -> "bob@example.com", "organisation" -> "My Org Ltd"))

      val result = controller.addAllowListAction()(fakeRequest)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some("/api-gatekeeper-organisation/allow-list/add-confirm")
    }

    "return 400 with Stride auth with invalid email" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.CreateAllowList.succeed(organisationAllowList)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", "/allow-list/add").withFormUrlEncodedBody("email" -> "bob", "organisation" -> "My Org Ltd"))

      val result = controller.addAllowListAction()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Enter the details of the user you want to add to the allow list")
      contentAsString(result) should include("Provide a valid email address")
    }

    "return 400 with Stride auth with email not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.CreateAllowList.failed("User not found")
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", "/allow-list/add").withFormUrlEncodedBody("email" -> "bob@example.com", "organisation" -> "My Org Ltd"))

      val result = controller.addAllowListAction()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Enter the details of the user you want to add to the allow list")
      contentAsString(result) should include("User not found")
    }
  }

  "GET add allow list confirm page" should {
    "return 200 with Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("GET", "/allow-list/add-confirm"))

      val result = controller.addAllowListConfirmView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("User added to the allow list")
      contentAsString(result) should include("Back to organisation allow list")
    }
  }

  "GET remove allow list page" should {
    "return 200 with Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.FetchAllowListForUserId.succeed(allowList)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("GET", s"/allow-list/remove/$userId"))

      val result = controller.removeAllowListView(userId)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Are you sure that you want to remove this user from the allow list?")
      contentAsString(result) should include(allowList.email.text)
      contentAsString(result) should include("will no longer be able to access the organisation registration journey on the Developer Hub.")
    }
  }

  "POST remove allow list page" should {
    "return 303 with Stride auth with valid data" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.DeleteAllowList.succeed()
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", s"/allow-list/remove/$userId").withFormUrlEncodedBody("confirm" -> "Yes"))

      val result = controller.removeAllowListAction(userId)(fakeRequest)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some("/api-gatekeeper-organisation/allow-list/remove-confirm")
    }

    "return 400 with Stride auth with no confirmation" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.FetchAllowListForUserId.succeed(allowList)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", "/allow-list/remove/$userId"))

      val result = controller.removeAllowListAction(userId)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Are you sure that you want to remove this user from the allow list?")
      contentAsString(result) should include("Please select an option")
    }

    "return 303 and return to main allow list page if select No" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("POST", "/allow-list/remove/$userId").withFormUrlEncodedBody("confirm" -> "No"))

      val result = controller.removeAllowListAction(userId)(fakeRequest)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some("/api-gatekeeper-organisation/allow-list")
    }
  }

  "GET remove allow list confirm page" should {
    "return 200 with Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      val fakeRequest = CSRFTokenHelper.addCSRFToken(FakeRequest("GET", "/allow-list/remove-confirm"))

      val result = controller.removeAllowListConfirmView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("User removed from the allow list")
      contentAsString(result) should include("Back to organisation allow list")
    }
  }
}
