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

import org.mockito.ArgumentMatchersSugar
import org.mockito.scalatest.MockitoSugar

import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationRecord
import uk.gov.hmrc.apigatekeeperorganisationfrontend.services.MigrationService

trait MigrationServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object MigrationServiceMock {
    val aMock = mock[MigrationService]

    object FetchAll {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.fetchAll()).thenReturn(Future.successful(results))
    }

    object LoadData {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.loadData(*)(*)).thenReturn(Future.successful(results))
    }

    object Fetch {
      def willReturn(result: Option[MigrationRecord]): Unit = when(aMock.fetch(*, *)).thenReturn(Future.successful(result))
    }

    object ProcessVat {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.processVat(*)(*)).thenReturn(Future.successful(results))
    }

    object ProcessCompaniesHouse {
      def willReturn(results: List[MigrationRecord]): Unit = when(aMock.processCompaniesHouse(*)(*)).thenReturn(Future.successful(results))
    }
  }
}
