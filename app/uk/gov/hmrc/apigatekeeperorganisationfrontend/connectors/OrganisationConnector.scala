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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.{Inject, Singleton}

import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview

@Singleton
class OrganisationConnector @Inject() (http: HttpClientV2, config: OrganisationConnector.Config)(implicit ec: ExecutionContext) {

  def searchSubmissionReviews(params: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[List[SubmissionReview]] = {
    http.get(url"${config.serviceBaseUrl}/submission-reviews?$params")
      .execute[List[SubmissionReview]]
  }
}

object OrganisationConnector {
  case class Config(serviceBaseUrl: String)
}
