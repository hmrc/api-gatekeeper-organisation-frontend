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
import scala.concurrent.Future.successful

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.SubmissionService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

object DeclineSubmissionController {
  case class DeclineSubmissionViewModel(submissionId: SubmissionId, instanceIndex: Int, organisationName: OrganisationName, requestedBy: String)

  case class DeclineSubmissionForm(comment: Option[String], confirm: Option[String] = Some(""))

  object DeclineSubmissionForm {

    def form: Form[DeclineSubmissionForm] = Form(
      mapping(
        "comment" -> optional(text(maxLength = 500)),
        "confirm" -> optional(text)
          .verifying("declinesubmission.error.confirmation.no.choice.field", _.isDefined)
      )(DeclineSubmissionForm.apply)(DeclineSubmissionForm.unapply)
    )
  }
}

@Singleton
class DeclineSubmissionController @Inject() (
    mcc: MessagesControllerComponents,
    declineSubmissionPage: DeclineSubmissionPage,
    declineSubmissionConfirmPage: DeclineSubmissionConfirmPage,
    service: SubmissionService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  import DeclineSubmissionController._

  val declineSubmissionForm: Form[DeclineSubmissionForm] = DeclineSubmissionForm.form

  def page(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    service.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) if (sr.state.isSubmitted || sr.state.isInProgress) =>
        Ok(declineSubmissionPage(DeclineSubmissionViewModel(submissionId, instanceIndex, sr.organisationName, sr.requestedBy), declineSubmissionForm))
      case _                                                           => BadRequest("Submission review not found or not submitted/in progress")
    }
  }

  def action(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    declineSubmissionForm.bindFromRequest().fold(
      formWithErrors => {
        service.fetchSubmissionReview(submissionId, instanceIndex)
          .map(_ match {
            case Some(sr) if (sr.state.isSubmitted || sr.state.isInProgress) =>
              BadRequest(declineSubmissionPage(DeclineSubmissionViewModel(submissionId, instanceIndex, sr.organisationName, sr.requestedBy), formWithErrors))
            case _                                                           => BadRequest("Submission review not found or not submitted")
          })
      },
      confirmData => {
        confirmData.confirm match {
          case Some("Yes") => {
            service.declineSubmission(submissionId, request.name.get, confirmData.comment.get)
              .map(_ match {
                case Right(sub) => Redirect(routes.DeclineSubmissionController.confirmPage(submissionId, instanceIndex))
                case Left(msg)  => BadRequest(msg)
              })
          }
          case _           => successful(Redirect(routes.ViewSubmissionController.checkAnswersPage(submissionId, instanceIndex)))
        }
      }
    )
  }

  def confirmPage(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    service.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) => Ok(declineSubmissionConfirmPage(DeclineSubmissionViewModel(submissionId, instanceIndex, sr.organisationName, sr.requestedBy)))
      case _        => BadRequest("Submission review not found")
    }
  }

}
