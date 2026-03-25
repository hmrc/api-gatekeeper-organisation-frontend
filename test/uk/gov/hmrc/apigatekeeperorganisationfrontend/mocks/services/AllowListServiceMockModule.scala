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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models.OrganisationAllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.AllowListService

trait AllowListServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object AllowListServiceMock {
    val aMock = mock[AllowListService]

    object FetchAllowList {
      def succeed(allowLists: List[AllowList]) = when(aMock.fetchAllowList()(*)).thenReturn(Future.successful(allowLists))

      def verifyCalled() = verify(aMock).fetchAllowList()(*)
    }

    object FetchAllowListForUserId {
      def succeed(allowList: AllowList) = when(aMock.fetchAllowListForUserId(*[UserId])(*)).thenReturn(Future.successful(Right(allowList)))
    }

    object CreateAllowList {
      def succeed(allowList: OrganisationAllowList) = when(aMock.createAllowList(*[LaxEmailAddress], *, *[OrganisationName])(*)).thenReturn(Future.successful(Right(allowList)))

      def failed(msg: String) = when(aMock.createAllowList(*[LaxEmailAddress], *, *[OrganisationName])(*)).thenReturn(Future.successful(Left(msg)))
    }

    object DeleteAllowList {
      def succeed() = when(aMock.deleteAllowList(*[UserId])(*)).thenReturn(Future.successful(Right(true)))

      def failed(msg: String) = when(aMock.deleteAllowList(*[UserId])(*)).thenReturn(Future.successful(Left(msg)))
    }
  }
}
