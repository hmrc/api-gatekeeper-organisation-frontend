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
import play.api.data.Forms._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.OrganisationService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.OrganisationsListPage

object OrganisationsController {

  case class FilterForm(
      organisationName: Option[String] = None
    )

  val filterForm: Form[FilterForm] = Form(
    mapping(
      "organisationName" -> optional(text)
    )(FilterForm.apply)(FilterForm.unapply)
  )
}

@Singleton
class OrganisationsController @Inject() (
    mcc: MessagesControllerComponents,
    organisationsListPage: OrganisationsListPage,
    service: OrganisationService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {
  import OrganisationsController._

  val organisationsView: Action[AnyContent] = loggedInOnly() { implicit request =>
    def doSearch(form: FilterForm) = {
      val params: Seq[(String, String)] = getQueryParamsFromForm(form)
      val queryForm                     = filterForm.fill(form)

      service
        .searchOrganisations(params)
        .map(orgs => Ok(organisationsListPage(queryForm, orgs)))
    }

    def handleValidForm(form: FilterForm) = {
      doSearch(form)
    }

    def handleInvalidForm(form: Form[FilterForm]) = {
      val defaultForm = FilterForm()
      doSearch(defaultForm)
    }

    OrganisationsController.filterForm.bindFromRequest().fold(handleInvalidForm, handleValidForm)
  }

  private def getQueryParamsFromForm(form: FilterForm): Seq[(String, String)] = {
    getQueryParamFromVar("organisationName", form.organisationName)
  }

  private def getQueryParamFromVar(key: String, value: Option[String]): Seq[(String, String)] = {
    if (value.isDefined) {
      Seq(key -> value.get)
    } else {
      Seq.empty
    }
  }
}
