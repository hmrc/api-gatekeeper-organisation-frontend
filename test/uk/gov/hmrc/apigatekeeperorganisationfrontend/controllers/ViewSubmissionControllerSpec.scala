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
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.OrganisationServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{AsyncHmrcSpec, WithCSRFAddToken}

class ViewSubmissionControllerSpec extends AsyncHmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends OrganisationServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule
      with SubmissionsTestData {

    val summaryPage = app.injector.instanceOf[ViewSubmissionSummaryPage]
    val answersPage = app.injector.instanceOf[ViewSubmittedAnswersPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]

    val controller =
      new ViewSubmissionController(mcc, summaryPage, answersPage, OrganisationServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)

    val submissionReviewEvent = SubmissionReview.Event("Submitted", "bob@example.com", instant, Some("comment"))

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

    val extendedSubmittedSubmission = aSubmission.copy(id = completedSubmissionId)
      .hasCompletelyAnsweredWith(answersToQuestions)
      .withSubmittedProgress()

  }

  "get view summary page" should {
    val fakeRequest = FakeRequest("GET", "/submission").withCSRFToken
    "return 200 for submission review found and isSubmitted" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewSubmitted))

      val result = controller.summaryPage(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(submissionReviewSubmitted.organisationName.value)
      contentAsString(result) should include("New")
      contentAsString(result) should include("Review this check")
      contentAsString(result) should include("Add a comment on this check")
      contentAsString(result) should include("Check history")
      contentAsString(result) should include(submissionReviewSubmitted.events.head.description)
    }

    "return 200 for submission review found and isInProgress" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewInProgress))

      val result = controller.summaryPage(submissionReviewInProgress.submissionId, submissionReviewInProgress.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(submissionReviewInProgress.organisationName.value)
      contentAsString(result) should include("In progress")
      contentAsString(result) should include("Review this check")
      contentAsString(result) should include("Add a comment on this check")
      contentAsString(result) should include("Check history")
      contentAsString(result) should include(submissionReviewInProgress.events.head.description)
    }

    "return 200 for submission review found and isApproved" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewApproved))

      val result = controller.summaryPage(submissionReviewApproved.submissionId, submissionReviewApproved.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(submissionReviewApproved.organisationName.value)
      contentAsString(result) should include("Approved")
      contentAsString(result) should include("View check answers")
      contentAsString(result) shouldNot include("Review this check")
      contentAsString(result) shouldNot include("Add a comment on this check")
      contentAsString(result) should include("Check history")
      contentAsString(result) should include(submissionReviewApproved.events.head.description)
    }

    "return 200 for submission review found and isFailed" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmissionReview.succeed(Some(submissionReviewFailed))

      val result = controller.summaryPage(submissionReviewFailed.submissionId, submissionReviewFailed.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(submissionReviewFailed.organisationName.value)
      contentAsString(result) should include("Failed")
      contentAsString(result) should include("View check answers")
      contentAsString(result) shouldNot include("Review this check")
      contentAsString(result) shouldNot include("Add a comment on this check")
      contentAsString(result) should include("Check history")
      contentAsString(result) should include(submissionReviewFailed.events.head.description)
    }

    "return 400 if submission review not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmissionReview.succeed(None)

      val result = controller.summaryPage(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission review not found")
    }
  }

  "get check summitted answers page" should {
    val fakeRequest = FakeRequest("GET", "/submission/answers").withCSRFToken

    "return 200 for submission found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmission.succeed(Some(extendedSubmittedSubmission))

      val result = controller.checkAnswersPage(extendedSubmittedSubmission.submission.id, extendedSubmittedSubmission.submission.latestInstance.index)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(extendedSubmittedSubmission.submission.name)
      contentAsString(result) should include("Approve this check")
      contentAsString(result) should include("Fail this check")
    }

    "return 200 for submission found but instance not latest" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmission.succeed(Some(extendedSubmittedSubmission))

      val result = controller.checkAnswersPage(extendedSubmittedSubmission.submission.id, 3)(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Business checks")
      contentAsString(result) should include(extendedSubmittedSubmission.submission.name)
      contentAsString(result) shouldNot include("Approve this check")
      contentAsString(result) shouldNot include("Fail this check")
    }

    "return 400 if submission not found" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      OrganisationServiceMock.FetchSubmission.succeed(None)

      val result = controller.checkAnswersPage(submissionReviewSubmitted.submissionId, submissionReviewSubmitted.instanceIndex)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include("Submission not found")
    }
  }
}
