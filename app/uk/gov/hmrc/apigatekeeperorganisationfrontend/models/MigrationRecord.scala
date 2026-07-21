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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.models

import play.api.libs.json.{Format, Json, OFormat}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationId
import uk.gov.hmrc.apiplatform.modules.common.domain.services.SimpleEnumJsonFormatting

case class MigrationRecord(answer: String, applicationIds: List[ApplicationId], status: MigrationStatus, questionType: String, details: Option[String])

object MigrationRecord {
  implicit val migrationRecordFormat: OFormat[MigrationRecord] = Json.format[MigrationRecord]

  def from(applicationsByAnswer: ApplicationsByAnswer, questionType: String) =
    MigrationRecord(applicationsByAnswer.answer.replaceAll("/\\s+/", ""), applicationsByAnswer.applicationIds, MigrationStatus.Unchecked, questionType, None)
}

enum MigrationStatus {
  case Unchecked, Verified, Unverified
}

object MigrationStatus {

  def apply(text: String): Option[MigrationStatus] = MigrationStatus.values.find(_.toString.toUpperCase == text.toUpperCase())

  def unsafeApply(text: String): MigrationStatus = apply(text).getOrElse(throw new RuntimeException(s"$text is not a valid MigrationStatus"))

  implicit val format: Format[MigrationStatus] = SimpleEnumJsonFormatting.createStringFormatFor[MigrationStatus]("MigrationStatus", apply)

}
