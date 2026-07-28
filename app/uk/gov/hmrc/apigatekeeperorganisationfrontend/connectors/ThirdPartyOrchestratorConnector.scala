/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{SessionId as _, StringContextOps, *}

import uk.gov.hmrc.apiplatform.modules.applications.core.domain.models.ApplicationWithCollaborators
import uk.gov.hmrc.apiplatform.modules.applications.query.domain.models.ApplicationQuery
import uk.gov.hmrc.apiplatform.modules.applications.query.domain.models.Param.*
import uk.gov.hmrc.apiplatform.modules.applications.query.domain.services.QueryParamsToQueryStringMap
import uk.gov.hmrc.apiplatform.modules.common.domain.models.*
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.ApplicationsByAnswer

@Singleton
class ThirdPartyOrchestratorConnector @Inject() (
    http: HttpClientV2,
    config: ThirdPartyOrchestratorConnector.Config
  )(using ExecutionContext
  ) extends Logging {

  def fetchApplicationsByAnswer(questionType: String)(using HeaderCarrier): Future[List[ApplicationsByAnswer]] = {
    http.get(url"${config.serviceBaseUrl}/submissions/answers/${questionType}")
      .execute[List[ApplicationsByAnswer]]
  }

  def findApplicationsForOrganisation(organisationId: OrganisationId)(implicit hc: HeaderCarrier): Future[List[ApplicationWithCollaborators]] = {
    query[List[ApplicationWithCollaborators]](ApplicationQuery.GeneralOpenEndedApplicationQuery(
      OrganisationIdQP(organisationId) :: ExcludeDeletedQP :: Nil
    ))
  }

  private def query[T](qry: ApplicationQuery)(implicit rds: HttpReads[T], hc: HeaderCarrier): Future[T] = {
    val params                                 = QueryParamsToQueryStringMap.toQuery(qry)
    val singleValueParams: Map[String, String] = params.map {
      case (k, vs) => k.text -> vs.mkString
    }

    http.get(url"${config.serviceBaseUrl}/query?$singleValueParams")
      .execute[T]
  }

}

object ThirdPartyOrchestratorConnector {
  case class Config(serviceBaseUrl: String)
}
