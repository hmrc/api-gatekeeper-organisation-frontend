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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.connectors

import scala.concurrent.Future
import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.http.NotFoundException

import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.GetRegisteredOrUnregisteredUsersResponse
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.ThirdPartyDeveloperConnector

trait TpdConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object TpdConnectorMock {
    val aMock = mock[ThirdPartyDeveloperConnector]

    object FetchDevelopers {
      def willReturn(users: List[User]) = when(aMock.fetchDevelopers(*)(using *)).thenReturn(Future.successful(users))
      def returnsNone()                 = when(aMock.fetchDevelopers(*)(using *)).thenReturn(successful(List.empty))
    }

    object FetchByEmails {
      def willReturn(users: List[User]) = when(aMock.fetchByEmails(*)(using *)).thenReturn(Future.successful(users))
    }

    object GetRegisteredOrUnregisteredUsers {
      def fails() = when(aMock.getRegisteredOrUnregisteredUsers(*)(using *)).thenReturn(Future.failed(new NotFoundException("")))

      def willReturn(response: GetRegisteredOrUnregisteredUsersResponse) =
        when(aMock.getRegisteredOrUnregisteredUsers(*)(using *)).thenReturn(successful(response))
    }

  }
}
