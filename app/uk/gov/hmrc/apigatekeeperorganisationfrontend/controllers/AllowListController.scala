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
import play.api.data.Forms.{mapping, text}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.LaxEmailAddress
import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.FormUtils.emailValidator
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.AllowListService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.{AddAllowListConfirmPage, AddAllowListPage, AllowListPage}

object AllowListController {

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
}

@Singleton
class AllowListController @Inject() (
    mcc: MessagesControllerComponents,
    allowListPage: AllowListPage,
    addAllowListPage: AddAllowListPage,
    addAllowListConfirmPage: AddAllowListConfirmPage,
    allowListService: AllowListService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  import AllowListController._

  val addAllowListForm: Form[AddAllowListForm] = AddAllowListForm.form

  def allowListView: Action[AnyContent] = loggedInOnly() { implicit request =>
    allowListService.fetchAllowList()
      .map(allowList => Ok(allowListPage(allowList)))
  }

  def addAllowListView: Action[AnyContent] = loggedInOnly() { implicit request =>
    Future.successful(Ok(addAllowListPage(addAllowListForm)))
  }

  def addAllowListAction(): Action[AnyContent] = loggedInOnly() { implicit request =>
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

  def addAllowListConfirmView: Action[AnyContent] = loggedInOnly() { implicit request =>
    Future.successful(Ok(addAllowListConfirmPage()))
  }

}
