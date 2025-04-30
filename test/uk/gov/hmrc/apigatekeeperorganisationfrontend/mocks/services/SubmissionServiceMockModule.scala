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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{ExtendedSubmission, Submission, SubmissionId, SubmissionReview}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.SubmissionService

trait SubmissionServiceMockModule extends SubmissionsTestData with MockitoSugar with ArgumentMatchersSugar {

  object SubmissionServiceMock {
    val aMock = mock[SubmissionService]

    object SearchSubmissionReviews {
      def succeed(submissionReviews: List[SubmissionReview]) = when(aMock.searchSubmissionReviews(*)(*)).thenReturn(Future.successful(submissionReviews))

      def verifyCalled(params: Seq[(String, String)]) = verify(aMock).searchSubmissionReviews(eqTo(params))(*)
    }

    object FetchSubmissionReview {
      def succeed(submissionReview: Option[SubmissionReview]) = when(aMock.fetchSubmissionReview(*[SubmissionId], *)(*)).thenReturn(Future.successful(submissionReview))
    }

    object FetchSubmission {
      def succeed(submission: Option[ExtendedSubmission]) = when(aMock.fetchSubmission(*[SubmissionId])(*)).thenReturn(Future.successful(submission))
    }

    object ApproveSubmission {
      def succeed(submission: Submission) = when(aMock.approveSubmission(*[SubmissionId], *, *)(*)).thenReturn(Future.successful(Right(submission)))

      def failed(msg: String) = when(aMock.approveSubmission(*[SubmissionId], *, *)(*)).thenReturn(Future.successful(Left(msg)))

      def verifyCalled(submissionId: SubmissionId, approvedBy: String, comment: Option[String]) =
        verify(aMock).approveSubmission(eqTo(submissionId), eqTo(approvedBy), eqTo(comment))(*)

      def verifyNeverCalled() = verify(aMock, never).approveSubmission(*[SubmissionId], *, *)(*)
    }

    object UpdateSubmissionReview {
      def succeed(submissionReview: SubmissionReview) = when(aMock.updateSubmissionReview(*[SubmissionId], *, *, *)(*)).thenReturn(Future.successful(Right(submissionReview)))

      def failed(msg: String) = when(aMock.updateSubmissionReview(*[SubmissionId], *, *, *)(*)).thenReturn(Future.successful(Left(msg)))

      def verifyCalled(submissionId: SubmissionId, instanceIndex: Int, approvedBy: String, comment: String) =
        verify(aMock).updateSubmissionReview(eqTo(submissionId), eqTo(instanceIndex), eqTo(approvedBy), eqTo(comment))(*)

      def verifyNeverCalled() = verify(aMock, never).updateSubmissionReview(*[SubmissionId], *, *, *)(*)
    }
  }
}
