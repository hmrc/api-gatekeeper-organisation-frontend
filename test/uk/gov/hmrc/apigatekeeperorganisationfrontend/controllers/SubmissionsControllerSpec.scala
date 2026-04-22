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

import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview.State.{Approved, Declined, InProgress, ReSubmitted, Submitted}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.WithCSRFAddToken
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.SubmissionServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

class SubmissionsControllerSpec extends HmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends SubmissionServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val fakeRequest = FakeRequest("GET", "/")
    val page        = app.injector.instanceOf[SubmissionListPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val controller  = new SubmissionsController(mcc, page, SubmissionServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)

    val submissionReviewEvent = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)

    val submissionReviewSubmitted =
      SubmissionReview(SubmissionId.random, OrganisationName("Submitted org"), instant, "bob@example.com", instant, Submitted, List(submissionReviewEvent))

    val submissionReviewInProgress =
      SubmissionReview(SubmissionId.random, OrganisationName("InProgress org"), instant, "bob@example.com", instant, InProgress, List(submissionReviewEvent))

    val submissionReviewApproved =
      SubmissionReview(SubmissionId.random, OrganisationName("Approved org"), instant, "bob@example.com", instant, Approved, List(submissionReviewEvent))

    val submissionReviewDeclined =
      SubmissionReview(SubmissionId.random, OrganisationName("Declined org"), instant, "bob@example.com", instant, Declined, List(submissionReviewEvent))

    val submissionReviewReSubmitted =
      SubmissionReview(SubmissionId.random, OrganisationName("ReSubmitted org"), instant, "bob@example.com", instant, ReSubmitted, List(submissionReviewEvent))
  }

  "GET /" should {
    "return 200 for no filter and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(submissionReviewSubmitted, submissionReviewInProgress, submissionReviewReSubmitted))

      val result = controller.submissionsView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Submitted org")
      contentAsString(result) should include("InProgress org")
      contentAsString(result) should include("ReSubmitted org")
      contentAsString(result) shouldNot include("Approved org")
      contentAsString(result) shouldNot include("Declined org")

      SubmissionServiceMock.SearchSubmissionReviews.verifyCalled(Seq("status" -> "SUBMITTED", "status" -> "IN_PROGRESS", "status" -> "RE_SUBMITTED"))
    }

    "filter submitted submission reviews" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(submissionReviewSubmitted))

      val result = controller.submissionsView(fakeRequest.withFormUrlEncodedBody("control" -> "true", "submittedStatus" -> "true"))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Submitted org")
      contentAsString(result) shouldNot include("InProgress org")
      contentAsString(result) shouldNot include("ReSubmitted org")
      contentAsString(result) shouldNot include("Approved org")
      contentAsString(result) shouldNot include("Declined org")

      SubmissionServiceMock.SearchSubmissionReviews.verifyCalled(Seq("status" -> "SUBMITTED"))
    }

    "filter with no statuses selected" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(
        submissionReviewSubmitted,
        submissionReviewInProgress,
        submissionReviewApproved,
        submissionReviewDeclined,
        submissionReviewReSubmitted
      ))

      val result = controller.submissionsView(fakeRequest.withFormUrlEncodedBody("control" -> "true"))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Submitted org")
      contentAsString(result) should include("InProgress org")
      contentAsString(result) should include("ReSubmitted org")
      contentAsString(result) should include("Approved org")
      contentAsString(result) should include("Declined org")

      SubmissionServiceMock.SearchSubmissionReviews.verifyCalled(Seq.empty)
    }

    "filter with all statuses selected" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(
        submissionReviewSubmitted,
        submissionReviewInProgress,
        submissionReviewApproved,
        submissionReviewDeclined,
        submissionReviewReSubmitted
      ))

      val result = controller.submissionsView(fakeRequest.withFormUrlEncodedBody(
        "control"           -> "true",
        "submittedStatus"   -> "true",
        "inProgressStatus"  -> "true",
        "approvedStatus"    -> "true",
        "failedStatus"      -> "true",
        "reSubmittedStatus" -> "true"
      ))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Submitted org")
      contentAsString(result) should include("InProgress org")
      contentAsString(result) should include("ReSubmitted org")
      contentAsString(result) should include("Approved org")
      contentAsString(result) should include("Declined org")

      SubmissionServiceMock.SearchSubmissionReviews.verifyCalled(Seq(
        "status" -> "SUBMITTED",
        "status" -> "IN_PROGRESS",
        "status" -> "RE_SUBMITTED",
        "status" -> "APPROVED",
        "status" -> "DECLINED"
      ))
    }

    "return 200 for Ldap auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.succeeds
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(submissionReviewApproved))

      val result = controller.submissionsView(fakeRequest)

      status(result) shouldBe Status.OK
    }

    "return 403 for incorrect auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.notAuthorised

      val result = controller.submissionsView(fakeRequest)

      status(result) shouldBe Status.FORBIDDEN
    }

    "return HTML" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      SubmissionServiceMock.SearchSubmissionReviews.succeed(List(submissionReviewSubmitted))

      val result = controller.submissionsView(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }
}
