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

import scala.concurrent.Future

import com.google.inject.{Inject, Singleton}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{ExtendedSubmission, Submission, SubmissionId, SubmissionReview}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.OrganisationConnector

@Singleton
class OrganisationService @Inject() (orgConnector: OrganisationConnector) {

  def searchSubmissionReviews(params: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[List[SubmissionReview]] = {
    orgConnector.searchSubmissionReviews(params)
  }

  def fetchSubmissionReview(submissionId: SubmissionId, instanceIndex: Int)(implicit hc: HeaderCarrier): Future[Option[SubmissionReview]] = {
    orgConnector.fetchSubmissionReview(submissionId, instanceIndex)
  }

  def fetchSubmission(submissionId: SubmissionId)(implicit hc: HeaderCarrier): Future[Option[ExtendedSubmission]] = {
    orgConnector.fetchSubmission(submissionId)
  }

  def approveSubmission(submissionId: SubmissionId, approvedBy: String, comment: Option[String])(implicit hc: HeaderCarrier): Future[Either[String, Submission]] = {
    orgConnector.approveSubmission(submissionId, approvedBy, comment)
  }
}
