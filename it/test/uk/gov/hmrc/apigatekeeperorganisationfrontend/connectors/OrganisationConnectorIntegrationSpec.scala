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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application => PlayApplication, Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apigatekeeperorganisationfrontend.stubs.ApiPlatformOrganisationStub

class OrganisationConnectorIntegrationSpec extends BaseConnectorIntegrationSpec with GuiceOneAppPerSuite {

  private val stubConfig = Configuration(
    "microservice.services.api-platform-organisation.port" -> stubPort
  )

  trait Setup extends SubmissionsTestData {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = app.injector.instanceOf[OrganisationConnector]

    val submissionReviewEvent = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)

    val submissionReview =
      SubmissionReview(SubmissionId.random, 0, OrganisationName("My org"), instant, "bob@example.com", instant, SubmissionReview.State.Submitted, List(submissionReviewEvent))
  }

  override def fakeApplication(): PlayApplication =
    GuiceApplicationBuilder()
      .configure(stubConfig)
      .in(Mode.Test)
      .build()

  "searchSubmissionReviews" should {
    "successfully get all" in new Setup {
      val params: Seq[(String, String)] = Seq(("status", "SUBMITTED"))
      ApiPlatformOrganisationStub.SearchSubmissionReviews.succeeds("SUBMITTED", submissionReview)

      val result = await(underTest.searchSubmissionReviews(params))

      result shouldBe List(submissionReview)
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.SearchSubmissionReviews.fails(INTERNAL_SERVER_ERROR)

      intercept[UpstreamErrorResponse] {
        await(underTest.searchSubmissionReviews(Seq.empty))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
