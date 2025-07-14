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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import play.api.http.Status.OK
import play.api.libs.json.Json

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.Organisation
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{ExtendedSubmission, Submission, SubmissionId, SubmissionReview}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.OrganisationConnector.SearchOrganisationRequest

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

  object FetchSubmissionReview {

    def succeeds(submissionId: SubmissionId, instanceIndex: Int, submissionReview: SubmissionReview): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission-review/$submissionId/$instanceIndex"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(submissionReview).toString())
          )
      )
    }

    def fails(submissionId: SubmissionId, instanceIndex: Int, status: Int): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission-review/$submissionId/$instanceIndex"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

  object ApproveSubmission {

    def succeeds(submissionId: SubmissionId, submission: Submission): StubMapping = {
      stubFor(
        post(urlEqualTo(s"/submission/${submissionId}/approve"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(submission).toString())
          )
      )
    }

    def fails(submissionId: SubmissionId, status: Int): StubMapping = {
      stubFor(
        post(urlEqualTo(s"/submission/${submissionId}/approve"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

  object UpdateSubmissionReview {

    def succeeds(submissionId: SubmissionId, instanceIndex: Int, submissionReview: SubmissionReview): StubMapping = {
      stubFor(
        put(urlEqualTo(s"/submission-review/$submissionId/$instanceIndex"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(submissionReview).toString())
          )
      )
    }

    def fails(submissionId: SubmissionId, instanceIndex: Int, status: Int): StubMapping = {
      stubFor(
        put(urlEqualTo(s"/submission-review/$submissionId/$instanceIndex"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

  object FetchSubmission {

    import Submission._

    def succeeds(submissionId: SubmissionId, submission: ExtendedSubmission): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission/$submissionId"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(submission).toString())
          )
      )
    }

    def fails(submissionId: SubmissionId, status: Int): StubMapping = {
      stubFor(
        get(urlEqualTo(s"/submission/$submissionId"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

  object SearchOrganisations {

    def succeedsNoParams(organisations: List[Organisation]): StubMapping = {
      stubFor(
        post(urlEqualTo(s"/organisations"))
          .withRequestBody(equalToJson(Json.toJson(SearchOrganisationRequest(Seq.empty)).toString()))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(organisations).toString())
          )
      )
    }

    def succeedsParams(organisationName: String, organisations: List[Organisation]): StubMapping = {
      stubFor(
        post(urlEqualTo(s"/organisations?organisationName=$organisationName"))
          .withRequestBody(equalToJson(Json.toJson(SearchOrganisationRequest(Seq(("organisationName", organisationName)))).toString()))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(Json.toJson(organisations).toString())
          )
      )
    }

    def fails(status: Int): StubMapping = {
      stubFor(
        post(urlEqualTo(s"/organisations"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )
    }
  }

}
