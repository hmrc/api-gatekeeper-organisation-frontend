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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.OrganisationConnector.{SaMatchingAddress, SaMatchingRequest}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.MigrationController.UtrCheckerForm
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.MigrationService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.migration.*

object MigrationController {

  case class UtrCheckerForm(utr: String, taxPayerType: String, taxPayerName: String, addressLine1: String, postcode: String)

  object UtrCheckerForm {

    def form: Form[UtrCheckerForm] = Form(
      mapping(
        "utr"          -> text,
        "taxPayerType" -> text,
        "taxPayerName" -> text,
        "addressLine1" -> text,
        "postcode"     -> text
      )(UtrCheckerForm.apply)(f => Some((f.utr, f.taxPayerType, f.taxPayerName, f.addressLine1, f.postcode)))
    )
  }
}

@Singleton
class MigrationController @Inject() (
    mcc: MessagesControllerComponents,
    migrationService: MigrationService,
    migrationControlPage: MigrationControlPage,
    migrationAdminPage: MigrationAdminPage,
    migrationDetailPage: MigrationDetailPage,
    processedListPage: ProcessedListPage,
    utrCheckerPage: UtrCheckerPage,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(using ExecutionContext
  ) extends GatekeeperBaseController(strideAuthorisationService, mcc) with GatekeeperRoleActions {

  def controlPage(): Action[AnyContent] = loggedInOnly() { implicit request =>
    Future.successful(Ok(migrationControlPage()))
  }

  def adminPage(): Action[AnyContent] = loggedInOnly() { implicit request =>
    Future.successful(Ok(migrationAdminPage()))
  }

  def overviewPage(): Action[AnyContent] = loggedInOnly() { implicit request =>
    migrationService.fetchAll().map(data => Ok(processedListPage(s"${data.length} migration records", data)))
  }

  def load(questionType: String): Action[AnyContent] = loggedInOnly() { implicit request =>
    migrationService.loadData(questionType).map(data => Ok(processedListPage(s"${data.length} Organisations loaded", data)))
  }

  def detailsPage(questionType: String, answer: String) = loggedInOnly() { implicit request =>
    migrationService.fetch(questionType, answer).map(maybeRecord => maybeRecord.fold(NotFound(""))(record => Ok(migrationDetailPage(record))))
  }

  def processVat(count: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    migrationService.processVat(count).map(data => Ok(processedListPage(s"${data.length} checked", data)))
  }

  def processCompaniesHouse(count: Int): Action[AnyContent] = loggedInOnly() { implicit request =>
    migrationService.processCompaniesHouse(count).map(data => Ok(processedListPage(s"${data.length} checked", data)))
  }

  def utrChecker(): Action[AnyContent] = loggedInOnly() { implicit request =>
    Future.successful(Ok(utrCheckerPage(UtrCheckerForm.form, None)))
  }

  def utrCheckerAction(): Action[AnyContent] = loggedInOnly() { implicit request =>
    val data    = UtrCheckerForm.form.bindFromRequest().get
    val request = SaMatchingRequest(data.utr, data.taxPayerType, data.taxPayerName, SaMatchingAddress(data.addressLine1, data.postcode))
    migrationService.matchBySa(request).map(json => Ok(utrCheckerPage(UtrCheckerForm.form.fill(data), Some(Json.prettyPrint(json)))))
  }
}
