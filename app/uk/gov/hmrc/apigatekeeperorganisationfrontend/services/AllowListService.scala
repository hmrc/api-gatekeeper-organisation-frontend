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

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.{Inject, Singleton}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.services.EitherTHelper
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.{OrganisationConnector, ThirdPartyDeveloperConnector}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList

@Singleton
class AllowListService @Inject() (
    orgConnector: OrganisationConnector,
    thirdPartyDeveloperConnector: ThirdPartyDeveloperConnector
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] {

  def fetchAllowList()(implicit hc: HeaderCarrier): Future[List[AllowList]] = {
    for {
      orgAllowList <- orgConnector.fetchAllOrganisationAllowLists()
      userList     <- thirdPartyDeveloperConnector.fetchDevelopers(orgAllowList.map(a => a.userId))
    } yield orgAllowList.map(o => AllowList.applyFromMaybeUser(o, userList.find(u => u.userId == o.userId))).flatten
  }

  def fetchAllowListForUserId(userId: UserId)(implicit hc: HeaderCarrier): Future[Either[String, AllowList]] = {
    (
      for {
        orgAllowList <- fromOptionF(orgConnector.fetchOrganisationAllowList(userId), "User not found in allow list")
        userList     <- liftF(thirdPartyDeveloperConnector.fetchDevelopers(List(userId)))
        user         <- fromOption(userList.find(u => u.userId == orgAllowList.userId), "User not found in Developer Hub")
      } yield AllowList.applyFromUser(orgAllowList, user)
    ).value
  }

  def createAllowList(email: LaxEmailAddress, requestedBy: String, organisationName: OrganisationName)(implicit hc: HeaderCarrier)
      : Future[Either[String, OrganisationAllowList]] = {
    (
      for {
        user      <- fromOptionF(getUserByEmail(email), s"Developer Hub user not found with email address ${email.text}")
        _         <- cond(user.verified, (), "Developer Hub user is not verified")
        allowList <- fromEitherF(orgConnector.createOrganisationAllowList(user.userId, requestedBy, organisationName))
      } yield allowList
    ).value
  }

  private def getUserByEmail(email: LaxEmailAddress)(implicit hc: HeaderCarrier): Future[Option[User]] = {
    thirdPartyDeveloperConnector.fetchByEmails(Set(email)).map(users => users.headOption)
  }

  def deleteAllowList(userId: UserId)(implicit hc: HeaderCarrier): Future[Either[String, Boolean]] = {
    orgConnector.deleteOrganisationAllowList(userId)
  }
}
