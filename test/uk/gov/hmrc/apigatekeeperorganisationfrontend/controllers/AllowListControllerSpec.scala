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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.controllers

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._

import uk.gov.hmrc.apiplatform.modules.common.domain.models.{LaxEmailAddress, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.OrganisationName
import uk.gov.hmrc.apigatekeeperorganisationfrontend.WithCSRFAddToken
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.AllowListServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.AllowList
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html._

class AllowListControllerSpec extends HmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  trait Setup
      extends AllowListServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val fakeRequest = FakeRequest("GET", "/allow-list")
    val page        = app.injector.instanceOf[AllowListPage]
    val mcc         = app.injector.instanceOf[MessagesControllerComponents]
    val controller  = new AllowListController(mcc, page, AllowListServiceMock.aMock, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock)

    val userId    = UserId.random
    val allowList = AllowList(userId, OrganisationName("My Org"), "Bob", "Fleming", LaxEmailAddress("bob@fleming.com"))
  }

  "GET /" should {
    "return 200 with all organisations for no filter and Stride auth" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)
      AllowListServiceMock.FetchAllowList.succeed(List(allowList))

      val result = controller.allowListView(fakeRequest)

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Organisation allow list")
      contentAsString(result) should include("Add a user to the allow list")
      contentAsString(result) should include("Users on the allow list")
      contentAsString(result) should include(allowList.organisationName.value)
      contentAsString(result) should include(allowList.email.text)

      AllowListServiceMock.FetchAllowList.verifyCalled()
    }
  }
}
