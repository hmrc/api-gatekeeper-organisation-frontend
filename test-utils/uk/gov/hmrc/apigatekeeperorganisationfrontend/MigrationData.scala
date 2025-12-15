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

package uk.gov.hmrc.apigatekeeperorganisationfrontend

import uk.gov.hmrc.apiplatform.modules.common.domain.models.ApplicationIdFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationStatus.{Unchecked, Unverified, Verified}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.{ApplicationsByAnswer, MigrationRecord}

object MigrationRecordData extends ApplicationIdFixtures {
  val vatType                = "vat-registration-number"
  val one: MigrationRecord   = MigrationRecord("123456789", List(applicationIdOne, applicationIdTwo, applicationIdThree), Unchecked, vatType, None)
  val two: MigrationRecord   = MigrationRecord("987654321", List(applicationIdTwo, applicationIdThree), Unverified, vatType, None)
  val three: MigrationRecord = MigrationRecord("12345", List(applicationIdThree), Verified, vatType, None)
}

trait MigrationFixtures {
  val vatType: String              = MigrationRecordData.vatType
  val recordOne: MigrationRecord   = MigrationRecordData.one
  val recordTwo: MigrationRecord   = MigrationRecordData.two
  val recordThree: MigrationRecord = MigrationRecordData.three
}

object AppsByAnswerData extends ApplicationIdFixtures {
  val one: ApplicationsByAnswer   = ApplicationsByAnswer("123456789", List(applicationIdOne, applicationIdTwo))
  val two: ApplicationsByAnswer   = ApplicationsByAnswer("987654321", List(applicationIdTwo))
  val three: ApplicationsByAnswer = ApplicationsByAnswer("FISH", List(applicationIdOne, applicationIdTwo, applicationIdThree))
}

trait AppsByAnswerFixtures {
  val appsByAnswersOne: ApplicationsByAnswer   = AppsByAnswerData.one
  val appsByAnswersTwo: ApplicationsByAnswer   = AppsByAnswerData.two
  val appsByAnswersThree: ApplicationsByAnswer = AppsByAnswerData.three
}
