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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, InsertManyOptions}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.{MigrationRecord, MigrationStatus}

@Singleton
class MigrationRepository @Inject() (mongo: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[MigrationRecord](
      collectionName = "migration",
      mongoComponent = mongo,
      domainFormat = MigrationRecord.migrationRecordFormat,
      indexes = Seq(
        IndexModel(
          ascending("answer", "questionType"),
          IndexOptions()
            .name("answerIndex")
            .unique(true)
            .background(true)
        ),
        IndexModel(
          ascending("questionType", "status"),
          IndexOptions()
            .name("typeAndStatus")
            .unique(false)
            .background(true)
        )
      ),
      replaceIndexes = true
    ) {
  override lazy val requiresTtlIndex: Boolean = false

  def save(records: List[MigrationRecord]): Future[List[MigrationRecord]] = {
    if (records.isEmpty) { Future.successful(List.empty) }
    else {
      collection.insertMany(records, InsertManyOptions().ordered(false)).toFuture().recover { case _ => }.map(_ => records)
    }
  }

  def fetchAll(): Future[List[MigrationRecord]] = {
    collection.find.toFuture().map(_.toList)
  }

  def update(record: MigrationRecord): Future[MigrationRecord] = {
    collection.replaceOne(and(equal("answer", record.answer), equal("answer", record.answer)), record).toFuture().map(_ => record)
  }

  def fetch(questionType: String, answer: String): Future[Option[MigrationRecord]] = {
    collection.find(and(equal("questionType", questionType), equal("answer", answer))).headOption()
  }

  def fetchUnchecked(questionType: String, count: Int): Future[List[MigrationRecord]] = {
    collection.find(and(equal("questionType", questionType), equal("status", Codecs.toBson(MigrationStatus.Unchecked.toString)))).limit(count).toFuture().map(_.toList)
  }
}
