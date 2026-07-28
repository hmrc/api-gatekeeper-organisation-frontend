/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{OrganisationId, UserId}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Organisation, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.*
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.OrganisationConnector
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.CompaniesHouseCompanyProfile

trait OrganisationConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationConnectorMock {
    val aMock = mock[OrganisationConnector]

    object FetchOrganisation {
      def willReturn(organisation: Organisation) = when(aMock.fetchOrganisation(*[OrganisationId])(using *)).thenReturn(Future.successful(Some(organisation)))
      def fails()                                = when(aMock.fetchOrganisation(*[OrganisationId])(using *)).thenReturn(Future.successful(None))
    }

    object SearchSubmissionReviews {
      def willReturn(submissionReviews: List[SubmissionReview]) = when(aMock.searchSubmissionReviews(*)(using *)).thenReturn(Future.successful(submissionReviews))
    }

    object FetchSubmissionReview {
      def willReturn(submissionReview: Option[SubmissionReview]) = when(aMock.fetchSubmissionReview(*[SubmissionId])(using *)).thenReturn(Future.successful(submissionReview))
    }

    object ApproveSubmission {
      def willReturn(submission: Submission) = when(aMock.approveSubmission(*[SubmissionId], *, *)(using *)).thenReturn(Future.successful(Right(submission)))
    }

    object DeclineSubmission {
      def willReturn(submission: Submission) = when(aMock.declineSubmission(*[SubmissionId], *, *)(using *)).thenReturn(Future.successful(Right(submission)))
    }

    object UpdateSubmissionReview {
      def willReturn(submissionReview: SubmissionReview) = when(aMock.updateSubmissionReview(*[SubmissionId], *, *)(using *)).thenReturn(Future.successful(Right(submissionReview)))
    }

    object FetchSubmission {
      def willReturn(submission: Option[ExtendedSubmission]) = when(aMock.fetchSubmission(*[SubmissionId])(using *)).thenReturn(Future.successful(submission))
    }

    object SearchOrganisations {
      def willReturn(organisations: List[Organisation]) = when(aMock.searchOrganisations(*)(using *)).thenReturn(Future.successful(organisations))

      def verifyCalled(params: Seq[(String, String)]) = verify(aMock).searchOrganisations(eqTo(params))(using *)
    }

    object FetchByCompanyNumber {
      def willReturn(result: Option[CompaniesHouseCompanyProfile]) = when(aMock.fetchByCompanyNumber(*)(using *)).thenReturn(Future.successful(result))
    }

    object FetchAllOrganisationAllowLists {
      def willReturn(allowLists: List[OrganisationAllowList]) = when(aMock.fetchAllOrganisationAllowLists()(using *)).thenReturn(Future.successful(allowLists))
    }

    object FetchOrganisationAllowList {
      def willReturn(allowList: OrganisationAllowList) = when(aMock.fetchOrganisationAllowList(*[UserId])(using *)).thenReturn(Future.successful(Some(allowList)))

      def willReturnNone() = when(aMock.fetchOrganisationAllowList(*[UserId])(using *)).thenReturn(Future.successful(None))
    }

    object CreateOrganisationAllowList {

      def willReturn(allowList: OrganisationAllowList) =
        when(aMock.createOrganisationAllowList(*[UserId], *, *[OrganisationName])(using *)).thenReturn(Future.successful(Right(allowList)))

      def verifyCalled(userId: UserId, requestedBy: String, organisationName: OrganisationName) =
        verify(aMock).createOrganisationAllowList(eqTo(userId), eqTo(requestedBy), eqTo(organisationName))(using *)

      def verifyNotCalled(userId: UserId, requestedBy: String, organisationName: OrganisationName) =
        verify(aMock, never).createOrganisationAllowList(eqTo(userId), eqTo(requestedBy), eqTo(organisationName))(using *)
    }

    object DeleteOrganisationAllowList {

      def willReturn() =
        when(aMock.deleteOrganisationAllowList(*[UserId])(using *)).thenReturn(Future.successful(Right(true)))
    }
  }
}
