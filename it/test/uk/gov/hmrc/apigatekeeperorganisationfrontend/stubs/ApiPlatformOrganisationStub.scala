/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import play.api.http.Status.OK
import play.api.libs.json.Json

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.SubmissionReview

object ApiPlatformOrganisationStub {

  object SearchSubmissionReviews {

    def succeeds(status: String, submissionReview: SubmissionReview): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission-reviews?status=$status"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(List(submissionReview)).toString())
          )
      )
    }

    def fails(status: Int): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission-reviews"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

}
