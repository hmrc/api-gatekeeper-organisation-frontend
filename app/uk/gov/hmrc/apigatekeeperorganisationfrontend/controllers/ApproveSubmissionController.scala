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
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.OrganisationService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

object ApproveSubmissionController {
  case class ApproveSubmissionViewModel(submissionId: SubmissionId, instanceIndex: Int, organisationName: OrganisationName)

  case class ApproveSubmissionForm(comment: Option[String], confirm: Option[String] = Some(""))

  object ApproveSubmissionForm {

    def form: Form[ApproveSubmissionForm] = Form(
      mapping(
        "comment" -> optional(text),
        "confirm" -> optional(text)
          .verifying("approvesubmission.error.confirmation.no.choice.field", _.isDefined)
      )(ApproveSubmissionForm.apply)(ApproveSubmissionForm.unapply)
    )
  }
}

@Singleton
class ApproveSubmissionController @Inject() (
    mcc: MessagesControllerComponents,
    approveSubmissionPage: ApproveSubmissionPage,
    approveSubmissionConfirmPage: ApproveSubmissionConfirmPage,
    organisationService: OrganisationService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  import ApproveSubmissionController._

  val approveSubmissionForm: Form[ApproveSubmissionForm] = ApproveSubmissionForm.form

  def page(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    organisationService.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) if (sr.state.isSubmitted || sr.state.isInProgress) =>
        Ok(approveSubmissionPage(ApproveSubmissionViewModel(submissionId, instanceIndex, sr.organisationName), approveSubmissionForm))
      case _                                                           => BadRequest("Submission review not found or not submitted/in progress")
    }
  }

  def action(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    approveSubmissionForm.bindFromRequest().fold(
      formWithErrors => {
        organisationService.fetchSubmissionReview(submissionId, instanceIndex)
          .map(_ match {
            case Some(sr) if (sr.state.isSubmitted) =>
              BadRequest(approveSubmissionPage(ApproveSubmissionViewModel(submissionId, instanceIndex, sr.organisationName), formWithErrors))
            case _                                  => BadRequest("Submission review not found or not submitted")
          })
      },
      confirmData => {
        confirmData.confirm match {
          case Some("Yes") => {
            organisationService.approveSubmission(submissionId, request.name.get, confirmData.comment)
              .map(_ match {
                case Right(sub) => Redirect(routes.ApproveSubmissionController.confirmPage(submissionId, instanceIndex))
                case Left(msg)  => BadRequest(msg)
              })
          }
          case _           => successful(Redirect(routes.SubmissionsController.submissionsView()))
        }
      }
    )
  }

  def confirmPage(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    organisationService.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) => Ok(approveSubmissionConfirmPage(ApproveSubmissionViewModel(submissionId, instanceIndex, sr.organisationName)))
      case _        => BadRequest("Submission review not found")
    }
  }

}
