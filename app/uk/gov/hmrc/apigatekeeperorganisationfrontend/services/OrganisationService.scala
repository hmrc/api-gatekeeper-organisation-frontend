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

import uk.gov.hmrc.apiplatform.modules.common.domain.models.OrganisationId
import uk.gov.hmrc.apiplatform.modules.common.services.EitherTHelper
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.Organisation
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.{OrganisationConnector, ThirdPartyDeveloperConnector, ThirdPartyOrchestratorConnector}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.OrganisationWithAllMembersDetailsAndApplications

@Singleton
class OrganisationService @Inject() (
    orgConnector: OrganisationConnector,
    thirdPartyDeveloperConnector: ThirdPartyDeveloperConnector,
    thirdPartyOrchestratorConnector: ThirdPartyOrchestratorConnector
  )(implicit val ec: ExecutionContext
  ) extends EitherTHelper[String] {

  def searchOrganisations(params: Seq[(String, String)])(using HeaderCarrier): Future[List[Organisation]] = {
    orgConnector.searchOrganisations(params)
  }

  def fetchWithAllDetails(id: OrganisationId)(implicit hc: HeaderCarrier): Future[Either[String, OrganisationWithAllMembersDetailsAndApplications]] = {
    (
      for {
        org     <- fromOptionF(orgConnector.fetchOrganisation(id), "Organisation not found")
        members <- liftF(thirdPartyDeveloperConnector.getRegisteredOrUnregisteredUsers(org.collaborators.map(m => m.userId).toList))
        users   <- liftF(thirdPartyDeveloperConnector.fetchDevelopers(members.users.filter(u => u.isRegistered).map(u => u.userId)))
        apps    <- liftF(thirdPartyOrchestratorConnector.findApplicationsForOrganisation(id))
      } yield OrganisationWithAllMembersDetailsAndApplications.apply(org, members.users, users, apps)
    ).value
  }
}
