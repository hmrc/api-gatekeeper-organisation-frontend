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
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{SessionId => _, StringContextOps, _}

case class VatRegisteredCompany(
    name: String,
    vatNumber: String
  )

object VatRegisteredCompany {

  implicit val vatRegisteredCompanyFormat: OFormat[VatRegisteredCompany] =
    Json.format[VatRegisteredCompany]
}

case class LookupResponse(
    target: Option[VatRegisteredCompany]
  )

object LookupResponse {
  implicit val lookupResponseFormat: OFormat[LookupResponse] = Json.format[LookupResponse]
}

@Singleton
class VatRegisteredCompaniesConnector @Inject() (
    http: HttpClientV2,
    config: VatRegisteredCompaniesConnector.Config
  )(implicit val ec: ExecutionContext
  ) extends Logging {

  def lookupVatNumber(vatNumber: String)(implicit hc: HeaderCarrier): Future[LookupResponse] = {
    http.get(url"${config.serviceBaseUrl}/vat-registered-companies/lookup/${vatNumber}")
      .execute[LookupResponse]
  }

}

object VatRegisteredCompaniesConnector {
  case class Config(serviceBaseUrl: String)
}
