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

import scala.concurrent.ExecutionContext.Implicits.global

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apiplatform.modules.tpd.emailpreferences.domain.models.EmailPreferences
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors.{OrganisationConnectorMockModule, TpdConnectorMockModule}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{AsyncHmrcSpec, OrganisationFixtures}

class AllowListServiceSpec extends AsyncHmrcSpec with OrganisationConnectorMockModule with TpdConnectorMockModule {

  trait Setup extends FixedClock with OrganisationFixtures {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val underTest                  = new AllowListService(OrganisationConnectorMock.aMock, TpdConnectorMock.aMock)

    val userId1       = UserId.random
    val userId2       = UserId.random
    val userId3       = UserId.random
    val email1        = LaxEmailAddress("bob@fleming.com")
    val email2        = LaxEmailAddress("bob@ewing.com")
    val orgAllowList1 = OrganisationAllowList(userId1, OrganisationName("My Org 1"), "requestedBy", instant)
    val orgAllowList2 = OrganisationAllowList(userId2, OrganisationName("My Org 2"), "requestedBy", instant)
    val orgAllowList3 = OrganisationAllowList(userId3, OrganisationName("My Org 3"), "requestedBy", instant)
    val user1         = User(email1, "Bob", "Fleming", instant, instant, true, None, List.empty, None, EmailPreferences.noPreferences, userId1)
    val user2         = User(email2, "Bob", "Ewing", instant, instant, true, None, List.empty, None, EmailPreferences.noPreferences, userId2)
    val allowList1    = AllowList(userId1, OrganisationName("My Org 1"), "Bob", "Fleming", email1)
    val allowList2    = AllowList(userId2, OrganisationName("My Org 2"), "Bob", "Ewing", email2)
  }

  "fetchAllowList" should {
    "fetch allow list" in new Setup {
      OrganisationConnectorMock.FetchAllOrganisationAllowLists.willReturn(List(orgAllowList1, orgAllowList2, orgAllowList3))
      TpdConnectorMock.FetchDevelopers.willReturn(List(user1, user2))

      val result = await(underTest.fetchAllowList())

      result shouldBe List(allowList1, allowList2)
    }
  }
}
