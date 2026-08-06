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

import org.mockito.scalatest.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.mongo.play.PlayMongoModule

import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}
import uk.gov.hmrc.apigatekeeperorganisationfrontend.mocks.services.MigrationServiceMockModule
import uk.gov.hmrc.apigatekeeperorganisationfrontend.repositories.MigrationRepository
import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.migration.*
import uk.gov.hmrc.apigatekeeperorganisationfrontend.{MigrationFixtures, WithCSRFAddToken}

class MigrationControllerSpec extends HmrcSpec
    with GuiceOneAppPerSuite
    with WithCSRFAddToken
    with MockitoSugar
    with MigrationFixtures {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[PlayMongoModule]
      .overrides(bind[MigrationRepository].toInstance(mock[MigrationRepository]))
      .build()

  trait Setup
      extends MigrationServiceMockModule
      with StrideAuthorisationServiceMockModule
      with LdapAuthorisationServiceMockModule {

    val fakeRequest         = FakeRequest("GET", "/").withCSRFToken
    val controlPage         = app.injector.instanceOf[MigrationControlPage]
    val adminPage           = app.injector.instanceOf[MigrationAdminPage]
    val processedListPage   = app.injector.instanceOf[ProcessedListPage]
    val migrationDetailPage = app.injector.instanceOf[MigrationDetailPage]
    val utrCheckerPage      = app.injector.instanceOf[UtrCheckerPage]
    val mcc                 = app.injector.instanceOf[MessagesControllerComponents]

    val controller = new MigrationController(
      mcc,
      MigrationServiceMock.aMock,
      controlPage,
      adminPage,
      migrationDetailPage,
      processedListPage,
      utrCheckerPage,
      StrideAuthorisationServiceMock.aMock,
      LdapAuthorisationServiceMock.aMock
    )

    val validUtrCheckerFormData = Seq(
      "utr"          -> "1234567890",
      "taxPayerType" -> "Individual",
      "taxPayerName" -> "Bob Smith",
      "addressLine1" -> "1 Test Street",
      "postcode"     -> "AB1 2CD"
    )
  }

  "MigrationController" should {
    "return 200 for controlPage" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val result = controller.controlPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include("Process 10 Vat Records")
      contentAsString(result) should include("Process 10 Companies House Records")
    }

    "return 200 for adminPage" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val result = controller.adminPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include("Load Vat Records")
      contentAsString(result) should include("Load Company Registration Records")
    }

    "return 200 for overviewPage" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      MigrationServiceMock.FetchAll.willReturn(List(recordOne))
      val result = controller.overviewPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include(recordOne.answer)

    }

    "return 200 for load" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      MigrationServiceMock.LoadData.willReturn(List(recordOne))
      val result = controller.load("test")(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 for detailsPage" in new Setup {

      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      MigrationServiceMock.Fetch.willReturn(Some(recordOne))
      val result = controller.detailsPage("test", "answer")(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 for processVat" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      MigrationServiceMock.ProcessVat.willReturn(List(recordOne))
      val result = controller.processVat(1)(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 for processCompaniesHouse" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      MigrationServiceMock.ProcessCompaniesHouse.willReturn(List(recordOne))
      val result = controller.processCompaniesHouse(1)(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 for utrChecker" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val result = controller.utrChecker()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include("utr")
    }

    "return 200 for utrCheckerAction with a match result" in new Setup {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val json = Json.obj("matched" -> true)
      MigrationServiceMock.MatchBySa.willReturn(json)

      val result = controller.utrCheckerAction()(FakeRequest("POST", "/").withCSRFToken.withFormUrlEncodedBody(validUtrCheckerFormData*))
      status(result) shouldBe Status.OK
      contentAsString(result) should include("true")
    }
  }
}
