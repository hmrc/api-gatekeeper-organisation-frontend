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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import uk.gov.hmrc.apiplatform.modules.gkauth.controllers.GatekeeperBaseController
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationService, StrideAuthorisationService}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers.actions.GatekeeperRoleActions
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.MigrationService
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.migration._

@Singleton
class MigrationController @Inject() (
    mcc: MessagesControllerComponents,
    migrationService: MigrationService,
    migrationControlPage: MigrationControlPage,
    migrationAdminPage: MigrationAdminPage,
    migrationDetailPage: MigrationDetailPage,
    processedListPage: ProcessedListPage,
    strideAuthorisationService: StrideAuthorisationService,
    val ldapAuthorisationService: LdapAuthorisationService
  )(implicit ec: ExecutionContext
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
}
