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
import scala.concurrent.{ExecutionContext, Future}

import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.FormUtils.emailValidator
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.AllowListService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

object AllowListController {

  case class RemoveAllowListViewModel(userId: UserId, email: LaxEmailAddress, organisationName: OrganisationName)

  case class AddAllowListForm(email: String, organisation: String)

  object AddAllowListForm {

    def form: Form[AddAllowListForm] = Form(
      mapping(
        "email"        -> emailValidator(),
        "organisation" -> text
          .verifying("organisation.error.required.field", !_.isBlank())
      )(AddAllowListForm.apply)(AddAllowListForm.unapply)
    )
  }

  case class RemoveAllowListForm(confirm: Option[String] = Some(""))

  object RemoveAllowListForm {

    def form: Form[RemoveAllowListForm] = Form(
      mapping(
        "confirm" -> optional(text)
          .verifying("removeallowlist.no.choice.field", _.isDefined)
      )(RemoveAllowListForm.apply)(RemoveAllowListForm.unapply)
    )
  }
}

@Singleton
class AllowListController @Inject() (
    mcc: MessagesControllerComponents,
    allowListPage: AllowListPage,
    addAllowListPage: AddAllowListPage,
    addAllowListConfirmPage: AddAllowListConfirmPage,
    removeAllowListPage: RemoveAllowListPage,
    removeAllowListConfirmPage: RemoveAllowListConfirmPage,
    allowListService: AllowListService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  import AllowListController._

  val addAllowListForm: Form[AddAllowListForm]       = AddAllowListForm.form
  val removeAllowListForm: Form[RemoveAllowListForm] = RemoveAllowListForm.form

  def allowListView: Action[AnyContent] = loggedInOnly() { implicit request =>
    allowListService.fetchAllowList()
      .map(allowList => Ok(allowListPage(allowList)))
  }

  def addAllowListView: Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    Future.successful(Ok(addAllowListPage(addAllowListForm)))
  }

  def addAllowListAction(): Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    addAllowListForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(addAllowListPage(formWithErrors)))
      },
      allowListAddData => {
        allowListService.createAllowList(LaxEmailAddress(allowListAddData.email), request.name.get, OrganisationName(allowListAddData.organisation))
          .map(_ match {
            case Right(org) => Redirect(routes.AllowListController.addAllowListConfirmView())
            case Left(msg)  => BadRequest(addAllowListPage(AddAllowListForm.form.fill(allowListAddData).withError("email", "emailAddress.error.creation", msg)))
          })
      }
    )
  }

  def addAllowListConfirmView: Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    Future.successful(Ok(addAllowListConfirmPage()))
  }

  def removeAllowListView(userId: UserId): Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    allowListService.fetchAllowListForUserId(userId)
      .map(_ match {
        case Right(allowList) => Ok(removeAllowListPage(removeAllowListForm, RemoveAllowListViewModel(userId, allowList.email, allowList.organisationName)))
        case Left(msg)        => BadRequest(msg)
      })
  }

  def removeAllowListAction(userId: UserId): Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    removeAllowListForm.bindFromRequest().fold(
      formWithErrors => {
        allowListService.fetchAllowListForUserId(userId)
          .map(_ match {
            case Right(allowList) => BadRequest(removeAllowListPage(formWithErrors, RemoveAllowListViewModel(userId, allowList.email, allowList.organisationName)))
            case Left(msg)        => BadRequest(msg)
          })
      },
      allowListRemoveData => {
        allowListRemoveData.confirm match {
          case Some("Yes") => {
            allowListService.deleteAllowList(userId)
              .map(_ match {
                case Right(res) => Redirect(routes.AllowListController.removeAllowListConfirmView())
                case Left(msg)  => BadRequest(msg)
              })
          }
          case _           => Future.successful(Redirect(routes.AllowListController.allowListView()))
        }
      }
    )
  }

  def removeAllowListConfirmView: Action[AnyContent] = atLeastSuperUserAction { implicit request =>
    Future.successful(Ok(removeAllowListConfirmPage()))
  }

}
