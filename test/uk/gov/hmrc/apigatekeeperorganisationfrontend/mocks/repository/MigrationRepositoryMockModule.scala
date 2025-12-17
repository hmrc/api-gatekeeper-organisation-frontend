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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.repository

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationRecord
import uk.gov.hmrc.apigatekeeperorganisationfrontend.repositories.MigrationRepository

trait MigrationRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object MigrationRepositoryMock {
    val aMock = mock[MigrationRepository]

    object FetchAll {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.fetchAll()).thenReturn(Future.successful(results))
    }

    object Fetch {
      def willReturn(result: Option[MigrationRecord]): Unit = when(aMock.fetch(*, *)).thenReturn(Future.successful(result))
    }

    object FetchUnchecked {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.fetchUnchecked(*, *)).thenReturn(Future.successful(results))
    }

    object Save {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.save(*)).thenReturn(Future.successful(results))
    }

    object Update {
      def willReturn(result: MigrationRecord): Unit = when(aMock.update(*)).thenReturn(Future.successful(result))
    }
  }
}
