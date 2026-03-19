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

import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.AllowListService

trait AllowListServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object AllowListServiceMock {
    val aMock = mock[AllowListService]

    object FetchAllowList {
      def succeed(allowLists: List[AllowList]) = when(aMock.fetchAllowList()(*)).thenReturn(Future.successful(allowLists))

      def verifyCalled() = verify(aMock).fetchAllowList()(*)
    }
  }
}
