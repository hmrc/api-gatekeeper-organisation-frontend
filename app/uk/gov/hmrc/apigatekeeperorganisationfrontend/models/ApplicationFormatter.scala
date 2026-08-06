/*
 * Copyright 2026 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneOffset}

import uk.gov.hmrc.apiplatform.modules.applications.core.domain.models.ApplicationWithCollaborators

object ApplicationFormatter {
  val dateFormatter         = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val initialLastAccessDate = LocalDateTime.of(2019, 6, 25, 0, 0)

  // Caution: defaulting now = LocalDateTime.now() will not use UTC
  def getLastAccess(app: ApplicationWithCollaborators)(now: LocalDateTime): String = {
    app.details.lastAccess match {
      case Some(lastAccess) =>
        val lastAccessDate = lastAccess.atOffset(ZoneOffset.UTC).toLocalDate()
        if (ChronoUnit.SECONDS.between(app.details.createdOn, lastAccess) == 0) {
          "No API called"
        } else if (ChronoUnit.DAYS.between(initialLastAccessDate, lastAccessDate.atStartOfDay()) > 0) {
          dateFormatter.format(lastAccessDate)
        } else {
          s"More than ${ChronoUnit.MONTHS.between(lastAccessDate, now)} months ago"
        }
      case None             => "No API called"
    }
  }
}
