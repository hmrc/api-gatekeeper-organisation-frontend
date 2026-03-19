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
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{SessionId => _, StringContextOps, _}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.UserId
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.GetUsersRequest

@Singleton
class ThirdPartyDeveloperConnector @Inject() (
    http: HttpClientV2,
    config: ThirdPartyDeveloperConnector.Config
  )(implicit val ec: ExecutionContext
  ) extends Logging {

  def fetchDevelopers(users: List[UserId])(implicit hc: HeaderCarrier): Future[List[User]] = {
    http.post(url"${config.serviceBaseUrl}/developers/get-users")
      .withBody(Json.toJson(GetUsersRequest(users)))
      .execute[List[User]]
  }
}

object ThirdPartyDeveloperConnector {
  case class Config(serviceBaseUrl: String)
}
