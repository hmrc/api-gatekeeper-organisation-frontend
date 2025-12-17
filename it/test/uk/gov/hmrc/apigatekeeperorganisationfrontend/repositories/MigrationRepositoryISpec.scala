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

package uk.gov.hmrc.apigatekeeperorganisationfrontend.repositories

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import uk.gov.hmrc.apigatekeeperorganisationfrontend.MigrationFixtures
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.MigrationRecord

class MigrationRepositoryISpec extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[MigrationRecord]
    with GuiceOneAppPerSuite
    with DefaultAwaitTimeout
    with FutureAwaits
    with MigrationFixtures {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .build()

  override protected val repository: PlayMongoRepository[MigrationRecord] = app.injector.instanceOf[MigrationRepository]
  val underTest: MigrationRepository                                      = app.injector.instanceOf[MigrationRepository]

  "MigrationRespository" should {
    "insert many migrations" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.save(List(recordOne, recordTwo, recordThree)))
      await(repository.collection.find().toFuture()).length shouldBe 3
    }

    "ignore duplicate when inserting" in {
      await(repository.collection.find().toFuture()).length shouldBe 0
      await(underTest.save(List(recordOne, recordOne, recordThree)))
      await(repository.collection.find().toFuture()).length shouldBe 2
    }

    "fetch all migrations" in {
      await(underTest.save(List(recordOne, recordTwo, recordThree)))
      val records = await(underTest.fetchAll())
      records should contain(recordOne)
      records should contain(recordTwo)
      records should contain(recordThree)
    }

    "fetch singular record" in {
      await(repository.collection.insertOne(recordOne).toFuture())
      await(underTest.fetch(recordOne.questionType, recordOne.answer)) shouldBe Some(recordOne)
    }

    "fetch no record" in {
      await(repository.collection.insertOne(recordOne).toFuture())
      await(underTest.fetch(recordOne.questionType, "answerNotFound")) shouldBe None
    }

    "fetch unchecked records only" in {
      await(repository.collection.insertMany(List(recordOne, recordTwo, recordThree)).toFuture())
      await(underTest.fetchUnchecked(recordOne.questionType, 2)) shouldBe List(recordOne)
    }

  }
}
