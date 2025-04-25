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

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.models.AnswersViewModel._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.OrganisationService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

@Singleton
class ViewSubmissionController @Inject() (
    mcc: MessagesControllerComponents,
    viewSubmissionSummaryPage: ViewSubmissionSummaryPage,
    viewSubmittedAnswersPage: ViewSubmittedAnswersPage,
    organisationService: OrganisationService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  def summaryPage(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    organisationService.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) => Ok(viewSubmissionSummaryPage(sr))
      case _        => BadRequest("Submission review not found")
    }
  }

  def checkAnswersPage(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    organisationService.fetchSubmission(submissionId).map {
      case Some(extSubmission) =>
        val viewModel = convertSubmissionToViewModel(extSubmission, instanceIndex)
        Ok(viewSubmittedAnswersPage(viewModel))
      case None                => BadRequest("Submission not found")
    }
  }
}
