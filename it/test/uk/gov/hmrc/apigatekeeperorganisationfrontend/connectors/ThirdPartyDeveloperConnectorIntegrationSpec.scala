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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application => PlayApplication, Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.GetUsersRequest
import uk.gov.hmrc.apiplatform.modules.tpd.emailpreferences.domain.models.EmailPreferences
import uk.gov.hmrc.apigatekeeperorganisationfrontend.AppsByAnswerFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.stubs.ThirdPartyDeveloperStub

class ThirdPartyDeveloperConnectorIntegrationSpec extends BaseConnectorIntegrationSpec with GuiceOneAppPerSuite {

  private val stubConfig = Configuration(
    "microservice.services.third-party-developer.port" -> stubPort
  )

  trait Setup extends AppsByAnswerFixtures with FixedClock {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = app.injector.instanceOf[ThirdPartyDeveloperConnector]

    val userId1 = UserId.random
    val userId2 = UserId.random
    val email1  = LaxEmailAddress("bob@fleming.com")
    val email2  = LaxEmailAddress("bob@ewing.com")
    val user1   = User(email1, "Bob", "Fleming", instant, instant, true, None, List.empty, None, EmailPreferences.noPreferences, userId1)
    val user2   = User(email2, "Bob", "Ewing", instant, instant, true, None, List.empty, None, EmailPreferences.noPreferences, userId2)

  }

  override def fakeApplication(): PlayApplication =
    GuiceApplicationBuilder()
      .configure(stubConfig)
      .in(Mode.Test)
      .build()

  "TPD Connector" should {
    "fetchDevelopers successfully" in new Setup {
      ThirdPartyDeveloperStub.FetchDevelopers.succeeds(GetUsersRequest(List(userId1, userId2)), response = List(user1, user2))
      val result = await(underTest.fetchDevelopers(List(userId1, userId2)))
      result shouldBe List(user1, user2)
    }

    "fetchDevelopers fails" in new Setup {
      ThirdPartyDeveloperStub.FetchDevelopers.fails(INTERNAL_SERVER_ERROR)
      intercept[UpstreamErrorResponse] {
        await(underTest.fetchDevelopers(List(userId1, userId2)))
      }.statusCode shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
