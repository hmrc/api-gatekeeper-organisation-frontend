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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.services

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.{OrganisationConnector, ThirdPartyOrchestratorConnector, VatRegisteredCompaniesConnector}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationRecord
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationStatus.{Unverified, Verified}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.repositories.MigrationRepository

@Singleton
class MigrationService @Inject() (
    tpoConnector: ThirdPartyOrchestratorConnector,
    migrationRepository: MigrationRepository,
    vatRegisteredCompaniesConnector: VatRegisteredCompaniesConnector,
    organisationConnector: OrganisationConnector
  )(implicit val ec: ExecutionContext
  ) {

  def fetchAll(): Future[List[MigrationRecord]] = {
    migrationRepository.fetchAll()
  }

  def loadData(questionType: String)(implicit hc: HeaderCarrier): Future[List[MigrationRecord]] = {
    tpoConnector.fetchApplicationsByAnswer(questionType).map(appsByAnswers => appsByAnswers.map(MigrationRecord.from(_, questionType)))
      .flatMap(records => migrationRepository.save(records))
  }

  def fetch(questionType: String, answer: String): Future[Option[MigrationRecord]] = {
    migrationRepository.fetch(questionType, answer)
  }

  def processVat(count: Int)(implicit hc: HeaderCarrier): Future[List[MigrationRecord]] = {
    migrationRepository.fetchUnchecked("vat-registration-number", count).map(records => records.map(rec => processVatRecord(rec))).flatMap(futureRecords =>
      Future.sequence(futureRecords)
    )
  }

  def processCompaniesHouse(count: Int)(implicit hc: HeaderCarrier): Future[List[MigrationRecord]] = {
    migrationRepository.fetchUnchecked("company-registration-number", count).map(records => records.map(rec => processCompaniesHouseRecord(rec))).flatMap(futureRecords =>
      Future.sequence(futureRecords)
    )
  }

  private def processVatRecord(record: MigrationRecord)(implicit hc: HeaderCarrier): Future[MigrationRecord] = {
    vatRegisteredCompaniesConnector.lookupVatNumber(record.answer).map(_.target).map(_.fold(record.copy(status = Unverified))(company =>
      record.copy(status = Verified, details = Some(company.name))
    )).flatMap(newRecord => migrationRepository.update(newRecord))
  }

  private def processCompaniesHouseRecord(record: MigrationRecord)(implicit hc: HeaderCarrier): Future[MigrationRecord] = {
    organisationConnector.fetchByCompanyNumber(record.answer).map(_.fold(record.copy(status = Unverified))(company =>
      record.copy(status = Verified, details = Some(company.companyName))
    )).flatMap(newRecord => migrationRepository.update(newRecord))
  }
}
