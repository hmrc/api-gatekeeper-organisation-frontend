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

import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{OrganisationId, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.{SubmissionId, SubmissionReview}
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.utils.SubmissionsTestData
import uk.gov.hmrc.apigatekeeperorganisationfrontend.OrganisationFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.stubs.ApiPlatformOrganisationStub

class OrganisationConnectorIntegrationSpec extends BaseConnectorIntegrationSpec with GuiceOneAppPerSuite {

  private val stubConfig = Configuration(
    "microservice.services.api-platform-organisation.port" -> stubPort
  )

  trait Setup extends SubmissionsTestData with OrganisationFixtures {
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

  "fetchSubmissionReview" should {
    "successfully get one" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmissionReview.succeeds(submissionReview.submissionId, submissionReview.instanceIndex, submissionReview)

      val result = await(underTest.fetchSubmissionReview(submissionReview.submissionId, submissionReview.instanceIndex))

      result shouldBe Some(submissionReview)
    }

    "return None when not found" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmissionReview.fails(submissionReview.submissionId, submissionReview.instanceIndex, NOT_FOUND)

      val result = await(underTest.fetchSubmissionReview(submissionReview.submissionId, submissionReview.instanceIndex))

      result shouldBe None
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmissionReview.fails(submissionReview.submissionId, submissionReview.instanceIndex, INTERNAL_SERVER_ERROR)

      intercept[UpstreamErrorResponse] {
        await(underTest.fetchSubmissionReview(submissionReview.submissionId, submissionReview.instanceIndex))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "approveSubmission" should {
    "successfully approve" in new Setup {
      ApiPlatformOrganisationStub.ApproveSubmission.succeeds(aSubmission.id, aSubmission)

      val result = await(underTest.approveSubmission(aSubmission.id, "approvedBy", Some("some comment")))

      result shouldBe Right(aSubmission)
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.ApproveSubmission.fails(aSubmission.id, INTERNAL_SERVER_ERROR)

      val result = await(underTest.approveSubmission(aSubmission.id, "approvedBy", Some("some comment")))

      result shouldBe Left(s"Failed to approve submission ${aSubmission.id}")
    }
  }

  "updateSubmissionReview" should {
    "successfully update" in new Setup {
      ApiPlatformOrganisationStub.UpdateSubmissionReview.succeeds(submissionReview.submissionId, submissionReview.instanceIndex, submissionReview)

      val result = await(underTest.updateSubmissionReview(submissionReview.submissionId, submissionReview.instanceIndex, "updatedBy", "some comment"))

      result shouldBe Right(submissionReview)
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.UpdateSubmissionReview.fails(submissionReview.submissionId, submissionReview.instanceIndex, INTERNAL_SERVER_ERROR)

      val result = await(underTest.updateSubmissionReview(submissionReview.submissionId, submissionReview.instanceIndex, "updatedBy", "some comment"))

      result shouldBe Left(s"Failed to update submission review ${submissionReview.submissionId}, index ${submissionReview.instanceIndex}")
    }
  }

  "fetchSubmission" should {
    "successfully get one" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmission.succeeds(submissionReview.submissionId, completelyAnswerExtendedSubmission)

      val result = await(underTest.fetchSubmission(submissionReview.submissionId))

      result shouldBe Some(completelyAnswerExtendedSubmission)
    }

    "return None when not found" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmission.fails(submissionReview.submissionId, NOT_FOUND)

      val result = await(underTest.fetchSubmission(submissionReview.submissionId))

      result shouldBe None
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.FetchSubmission.fails(submissionReview.submissionId, INTERNAL_SERVER_ERROR)

      intercept[UpstreamErrorResponse] {
        await(underTest.fetchSubmission(submissionReview.submissionId))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "searchOrganisations" should {
    "successfully get all" in new Setup {
      val standardOrg2 = standardOrg.copy(id = OrganisationId.random, organisationName = OrganisationName("Organisation 2"))

      ApiPlatformOrganisationStub.SearchOrganisations.succeedsNoParams(List(standardOrg, standardOrg2))

      val result = await(underTest.searchOrganisations(Seq.empty))

      result shouldBe List(standardOrg, standardOrg2)
    }

    "successfully get results when doing filtered search" in new Setup {
      ApiPlatformOrganisationStub.SearchOrganisations.succeedsParams(standardOrg.organisationName.value, List(standardOrg))

      val result = await(underTest.searchOrganisations(Seq(("organisationName", standardOrg.organisationName.value))))

      result shouldBe List(standardOrg)
    }

    "fail when the call returns an error" in new Setup {
      ApiPlatformOrganisationStub.SearchOrganisations.fails(INTERNAL_SERVER_ERROR)

      intercept[UpstreamErrorResponse] {
        await(underTest.searchOrganisations(Seq.empty))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
