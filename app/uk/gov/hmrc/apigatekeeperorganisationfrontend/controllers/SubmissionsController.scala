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
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

object SubmissionsController {

  case class FilterForm(
      control: String = "true",
      submittedStatus: Option[String] = Some("true"),
      inProgressStatus: Option[String] = Some("true"),
      approvedStatus: Option[String] = None,
      failedStatus: Option[String] = None
    )

  val filterForm: Form[FilterForm] = Form(
    mapping(
      "control"          -> text,
      "submittedStatus"  -> optional(text),
      "inProgressStatus" -> optional(text),
      "approvedStatus"   -> optional(text),
      "failedStatus"     -> optional(text)
    )(FilterForm.apply)(FilterForm.unapply)
  )
}

@Singleton
class SubmissionsController @Inject() (
    mcc: MessagesControllerComponents,
    submissionListPage: SubmissionListPage,
    organisationService: OrganisationService,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {
  import SubmissionsController._

  val submissionsView: Action[AnyContent] = loggedInOnly() { implicit request =>
    def doSearch(form: FilterForm) = {
      val params: Seq[(String, String)] = getQueryParamsFromForm(form)
      val queryForm                     = filterForm.fill(form)

      organisationService
        .searchSubmissionReviews(params)
        .map(subs => Ok(submissionListPage(queryForm, subs)))
    }

    def handleValidForm(form: FilterForm) = {
      doSearch(form)
    }

    def handleInvalidForm(form: Form[FilterForm]) = {
      val defaultForm = FilterForm()
      doSearch(defaultForm)
    }

    SubmissionsController.filterForm.bindFromRequest().fold(handleInvalidForm, handleValidForm)
  }

  private def getQueryParamsFromForm(form: FilterForm): Seq[(String, String)] = {
    getQueryParamFromStatusVar("SUBMITTED", form.submittedStatus) ++
      getQueryParamFromStatusVar("IN_PROGRESS", form.inProgressStatus) ++
      getQueryParamFromStatusVar("APPROVED", form.approvedStatus) ++
      getQueryParamFromStatusVar("FAILED", form.failedStatus)
  }

  private def getQueryParamFromStatusVar(key: String, value: Option[String]): Seq[(String, String)] = {
    if (value == Some("true")) {
      Seq("status" -> key)
    } else {
      Seq.empty
    }
  }
}
