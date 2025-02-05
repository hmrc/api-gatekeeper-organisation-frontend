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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.apiplatform.modules.organisations.submissions.domain.models._
import uk.gov.hmrc.apigatekeeperorganisationfrontend.connectors.OrganisationConnector

trait OrganisationConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  object OrganisationConnectorMock {
    val aMock = mock[OrganisationConnector]

    object FetchAll {
      def willReturn(sub: Submission) = when(aMock.fetchAll()(*)).thenReturn(Future.successful(List(sub)))
    }
  }
}
