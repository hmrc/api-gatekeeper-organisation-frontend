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

import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.SubmissionServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{AsyncHmrcSpec, WithCSRFAddToken}

class ApproveSubmissionControllerSpec extends AsyncHmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends SubmissionServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val page        = app.injector.instanceOf[ApproveSubmissionPage]
    val confirmPage = app.injector.instanceOf[ApproveSubmissionConfirmPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val controller  = new ApproveSubmissionController(mcc, page, confirmPage, SubmissionServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)

    val submissionReviewEvent = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)

    val submissionReviewSubmitted =
      SubmissionReview(
        SubmissionId.random,
        0,
        OrganisationName("Submitted org"),
        instant,
        "bob@example.com",
        instant,
        SubmissionReview.State.Submitted,
        List(submissionReviewEvent)
      )

    val submissionReviewInProgress =
      SubmissionReview(
        SubmissionId.random,
        0,
        OrganisationName("InProgress org"),
        instant,
        "bob@example.com",
        instant,
        SubmissionReview.State.InProgress,
        List(submissionReviewEvent)
      )

    val submissionReviewApproved =
      SubmissionReview(SubmissionId.random, 0, OrganisationName("Approved org"), instant, "bob@example.com", instant, SubmissionReview.State.Approved, List(submissionReviewEvent))

    val submissionReviewFailed =
      SubmissionReview(SubmissionId.random, 0, OrganisationName("Failed org"), instant, "bob@example.com", instant, SubmissionReview.State.Failed, List(submissionReviewEvent))
  }

  "get approve page" should {
    val fakeRequest = FakeRequest("GET", "/submission/approve").withCSRFToken
    "return 200 for submission review found and isSubmitted" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val result = controller.page(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"Are you sure that you want to approve ${submissionReviewSubmitted.organisationName.value}?")
      contentAsString(result) should include(
        s"This will allow ${submissionReviewSubmitted.organisationName.value} to request credentials to use live tax data in our production environment."
      )
      contentAsString(result) should include("Business checks")
    }

    "return 200 for submission review found and isInProgress" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewInProgress))

      val result = controller.page(submissionReviewInProgress.submissionId, submissionReviewInProgress.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"Are you sure that you want to approve ${submissionReviewInProgress.organisationName.value}?")
      contentAsString(result) should include(
        s"This will allow ${submissionReviewInProgress.organisationName.value} to request credentials to use live tax data in our production environment."
      )
      contentAsString(result) should include("Business checks")
    }

    "return 400 if submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.page(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found or not submitted")
    }

    "return 400 if submission review found but not submitted or in progress" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewFailed))

      val result = controller.page(submissionReviewFailed.submissionId, submissionReviewFailed.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found or not submitted")
    }
  }

  "post approve action" should {
    val fakeRequest = FakeRequest("POST", "/submission/approve").withCSRFToken
    "return 303 for form validation successful and submission approval successful with comment" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.ApproveSubmission.succeed(aSubmission)

      val request = fakeRequest.withFormUrlEncodedBody("confirm" -> "Yes", "comment" -> "approve comment")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.routes.ApproveSubmissionController.confirmPage(
        submissionReviewSubmitted.submissionId,
        submissionReviewSubmitted.instanceIndex
      ).url)
      SubmissionServiceMock.ApproveSubmission.verifyCalled(submissionReviewSubmitted.submissionId, "Bobby Example", Some("approve comment"))
    }

    "return 303 for form validation successful and submission approval successful with no comment" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.ApproveSubmission.succeed(aSubmission)

      val request = fakeRequest.withFormUrlEncodedBody("confirm" -> "Yes", "comment" -> "")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.routes.ApproveSubmissionController.confirmPage(
        submissionReviewSubmitted.submissionId,
        submissionReviewSubmitted.instanceIndex
      ).url)
      SubmissionServiceMock.ApproveSubmission.verifyCalled(submissionReviewSubmitted.submissionId, "Bobby Example", None)
    }

    "return 303 for user selected No" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.ApproveSubmission.succeed(aSubmission)

      val request = fakeRequest.withFormUrlEncodedBody("confirm" -> "No")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.routes.ViewSubmissionController.checkAnswersPage(
        submissionReviewSubmitted.submissionId,
        submissionReviewSubmitted.instanceIndex
      ).url)
      SubmissionServiceMock.ApproveSubmission.verifyNeverCalled()
    }

    "return 400 for form validation failed" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val result = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Please select an option")
      SubmissionServiceMock.ApproveSubmission.verifyNeverCalled()
    }

    "return 400 for submission approval failed" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.ApproveSubmission.failed("Approval failed")

      val request = fakeRequest.withFormUrlEncodedBody("confirm" -> "Yes", "comment" -> "approve comment")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Approval failed")
    }

    "return 400 for form validation failed and submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found or not submitted")
      SubmissionServiceMock.ApproveSubmission.verifyNeverCalled()
    }
  }

  "get approve confirmation page" should {
    val fakeRequest = FakeRequest("GET", "/submission/approve-confirm").withCSRFToken
    "return 200 for submission review found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewApproved))

      val result = controller.confirmPage(submissionReviewApproved.submissionId, submissionReviewApproved.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"The business check for ${submissionReviewApproved.organisationName.value} has been approved")
      contentAsString(result) should include(
        s"${submissionReviewApproved.organisationName.value} can now request access to tax data in our production environment."
      )
      contentAsString(result) should include("Back to business checks")
    }

    "return 400 when submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.confirmPage(submissionReviewApproved.submissionId, submissionReviewApproved.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found")
    }
  }
}
