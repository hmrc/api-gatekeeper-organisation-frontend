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

class UpdateSubmissionControllerSpec extends AsyncHmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends SubmissionServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val page        = app.injector.instanceOf[UpdateSubmissionPage]
    val confirmPage = app.injector.instanceOf[UpdateSubmissionConfirmPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val controller  = new UpdateSubmissionController(mcc, page, confirmPage, SubmissionServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)

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

    val submissionReviewFailed =
      SubmissionReview(SubmissionId.random, 0, OrganisationName("Failed org"), instant, "bob@example.com", instant, SubmissionReview.State.Failed, List(submissionReviewEvent))
  }

  "get update page" should {
    val fakeRequest = FakeRequest("GET", "/submission/update").withCSRFToken
    "return 200 for submission review found and isSubmitted" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val result = controller.page(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"Add a comment for the ${submissionReviewSubmitted.organisationName.value} business check")
      contentAsString(result) should include(
        s"Leave a comment if you have an update about this business check, it will be added to the business check history."
      )
      contentAsString(result) should include("Business checks")
    }

    "return 200 for submission review found and isInProgress" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewInProgress))

      val result = controller.page(submissionReviewInProgress.submissionId, submissionReviewInProgress.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"Add a comment for the ${submissionReviewInProgress.organisationName.value} business check")
      contentAsString(result) should include(
        s"Leave a comment if you have an update about this business check, it will be added to the business check history."
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

  "post update action" should {
    val fakeRequest = FakeRequest("POST", "/submission/update").withCSRFToken
    "return 303 for form validation successful and submission update successful with comment" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.UpdateSubmissionReview.succeed(submissionReviewSubmitted)

      val request = fakeRequest.withFormUrlEncodedBody("comment" -> "update comment")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.routes.UpdateSubmissionController.confirmPage(
        submissionReviewSubmitted.submissionId,
        submissionReviewSubmitted.instanceIndex
      ).url)
      SubmissionServiceMock.UpdateSubmissionReview.verifyCalled(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex, "Bobby Example", "update comment")
    }

    "return 400 for form validation failed" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val request = fakeRequest.withFormUrlEncodedBody("comment" -> "")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Please add a comment")
      SubmissionServiceMock.UpdateSubmissionReview.verifyNeverCalled()
    }

    "return 400 for submission update failed" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.UpdateSubmissionReview.failed("Update failed")

      val request = fakeRequest.withFormUrlEncodedBody("comment" -> "update comment")
      val result  = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(request)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Update failed")
    }

    "return 400 for form validation failed and submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.action(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found or not submitted")
      SubmissionServiceMock.UpdateSubmissionReview.verifyNeverCalled()
    }
  }

  "get update confirmation page" should {
    val fakeRequest = FakeRequest("GET", "/submission/update-confirm").withCSRFToken
    "return 200 for submission review found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val result = controller.confirmPage(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include(s"Your comment has been added for the ${submissionReviewSubmitted.organisationName.value} business check")
      contentAsString(result) should include(
        s"Your comment is visible on the business check history of ${submissionReviewSubmitted.organisationName.value}."
      )
      contentAsString(result) should include("Back to business checks")
    }

    "return 400 when submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.confirmPage(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found")
    }
  }
}
