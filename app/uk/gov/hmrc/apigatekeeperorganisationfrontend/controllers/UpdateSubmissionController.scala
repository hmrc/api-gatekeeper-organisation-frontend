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

import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionId
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.SubmissionService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

object UpdateSubmissionController {
  case class UpdateSubmissionViewModel(submissionId: SubmissionId, instanceIndex: Int, organisationName: OrganisationName)

  case class UpdateSubmissionForm(comment: String)

  object UpdateSubmissionForm {

    def form: Form[UpdateSubmissionForm] = Form(
      mapping(
        "comment" -> nonEmptyText
      )(UpdateSubmissionForm.apply)(UpdateSubmissionForm.unapply)
    )
  }
}

@Singleton
class UpdateSubmissionController @Inject() (
    mcc: MessagesControllerComponents,
    updateSubmissionPage: UpdateSubmissionPage,
    updateSubmissionConfirmPage: UpdateSubmissionConfirmPage,
    service: SubmissionService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  import UpdateSubmissionController._

  val updateSubmissionForm: Form[UpdateSubmissionForm] = UpdateSubmissionForm.form

  def page(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    service.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) if (sr.state.isSubmitted || sr.state.isInProgress) =>
        Ok(updateSubmissionPage(UpdateSubmissionViewModel(submissionId, instanceIndex, sr.organisationName), updateSubmissionForm))
      case _                                                           => BadRequest("Submission review not found or not submitted/in progress")
    }
  }

  def action(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    updateSubmissionForm.bindFromRequest().fold(
      formWithErrors => {
        service.fetchSubmissionReview(submissionId, instanceIndex)
          .map(_ match {
            case Some(sr) if (sr.state.isSubmitted) =>
              BadRequest(updateSubmissionPage(UpdateSubmissionViewModel(submissionId, instanceIndex, sr.organisationName), formWithErrors))
            case _                                  => BadRequest("Submission review not found or not submitted")
          })
      },
      confirmData => {
        service.updateSubmissionReview(submissionId, instanceIndex, request.name.get, confirmData.comment)
          .map(_ match {
            case Right(sub) => Redirect(routes.UpdateSubmissionController.confirmPage(submissionId, instanceIndex))
            case Left(msg)  => BadRequest(msg)
          })
      }
    )
  }

  def confirmPage(submissionId: SubmissionId, instanceIndex: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    service.fetchSubmissionReview(submissionId, instanceIndex) map {
      case Some(sr) => Ok(updateSubmissionConfirmPage(UpdateSubmissionViewModel(submissionId, instanceIndex, sr.organisationName)))
      case _        => BadRequest("Submission review not found")
    }
  }

}
