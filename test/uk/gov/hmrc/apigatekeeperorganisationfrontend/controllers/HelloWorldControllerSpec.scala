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

import uk.gov.hmrc.apigatekeeperorganisationfrontend.views.html.HelloWorldPage
import uk.gov.hmrc.apiplatform.modules.common.utils.HmrcSpec
import uk.gov.hmrc.apiplatform.modules.gkauth.domain.models.GatekeeperRoles
import uk.gov.hmrc.apiplatform.modules.gkauth.services.{LdapAuthorisationServiceMockModule, StrideAuthorisationServiceMockModule}

class HelloWorldControllerSpec extends HmrcSpec with GuiceOneAppPerSuite with StrideAuthorisationServiceMockModule with LdapAuthorisationServiceMockModule {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  private val fakeRequest = FakeRequest("GET", "/")
  val page                = app.injector.instanceOf[HelloWorldPage]
  val mcc                 = app.injector.instanceOf[MessagesControllerComponents]
  private val controller  = new HelloWorldController(mcc, StrideAuthorisationServiceMock.aMock, LdapAuthorisationServiceMock.aMock, page)

  "GET /" should {
    "return 200 for Stride auth" in {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val result = controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 200 for Ldap auth" in {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.succeeds

      val result = controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return 403 for incorrect auth" in {
      StrideAuthorisationServiceMock.Auth.hasInsufficientEnrolments()
      LdapAuthorisationServiceMock.Auth.notAuthorised

      val result = controller.helloWorld(fakeRequest)
      status(result) shouldBe Status.FORBIDDEN
    }

    "return HTML" in {
      StrideAuthorisationServiceMock.Auth.succeeds(GatekeeperRoles.USER)

      val result = controller.helloWorld(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }
}
