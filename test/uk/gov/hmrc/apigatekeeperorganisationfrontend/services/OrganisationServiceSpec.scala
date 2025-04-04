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

import uk.gov.hmrc.apiplatformorganisationfrontend.AsyncHmrcSpec
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors.OrganisationConnectorMockModule

class OrganisationServiceSpec extends AsyncHmrcSpec with OrganisationConnectorMockModule {

  trait Setup extends FixedClock {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = new OrganisationService(OrganisationConnectorMock.aMock)

    val submissionReviewEvent = SubmissionReview.Event("Submitted", "bob@example.com", instant, None)

    val submissionReview =
      SubmissionReview(SubmissionId.random, 0, OrganisationName("My org"), instant, "bob@example.com", instant, SubmissionReview.State.Submitted, List(submissionReviewEvent))
  }
  "fetchSubmissions" should {
    "fetch all submission reviews" in new Setup {
      OrganisationConnectorMock.SearchSubmissionReviews.willReturn(List(submissionReview))
      val result = await(underTest.searchSubmissionReviews(Seq.empty))
      result shouldBe List(submissionReview)
    }
  }
}
