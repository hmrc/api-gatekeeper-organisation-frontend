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

import play.api.libs.json.{Json, OFormat}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User

case class AllowList(userId: UserId, organisationName: OrganisationName, firstName: String, lastName: String, email: LaxEmailAddress)

object AllowList {

  def apply(orgAllowList: OrganisationAllowList, maybeUser: Option[User]): Option[AllowList] = {
    maybeUser match {
      case Some(user) => Some(AllowList(user.userId, orgAllowList.organisationName, user.firstName, user.lastName, user.email))
      case _          => None
    }
  }

  implicit val allowListFormat: OFormat[AllowList] = Json.format[AllowList]
}
