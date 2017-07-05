/*
 * Copyright 2017 Datamountaineer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datamountaineer.streamreactor.connect.mongodb.sink

import com.datamountaineer.connector.config.WriteModeEnum
import com.datamountaineer.streamreactor.connect.errors.{ErrorHandler, ErrorPolicyEnum}
import com.datamountaineer.streamreactor.connect.mongodb.config.{MongoConfig, MongoSinkConfigConstants, MongoSinkSettings}
import com.datamountaineer.streamreactor.connect.schemas.ConverterUtil
import com.mongodb.client.model._
import com.mongodb.{MongoClient, MongoClientURI}
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.connect.sink.{SinkRecord, SinkTaskContext}
import org.bson.Document

import scala.collection.JavaConversions._
import scala.util.Failure

/**
  * <h1>MongoJsonWriter</h1>
  * Mongo Json writer for Kafka connect
  * Writes a list of Kafka connect sink records to Mongo using the JSON support.
  */
class MongoWriter(settings: MongoSinkSettings, mongoClient: MongoClient) extends StrictLogging with ConverterUtil with ErrorHandler {
  logger.info(s"Obtaining the database information for ${settings.database}")
  private val database = mongoClient.getDatabase(settings.database)

  private val collectionMap = settings.kcql
    .map(c => c.getSource -> Option(database.getCollection(c.getTarget)).getOrElse {
      logger.info(s"Collection not found. Creating collection ${c.getTarget}")
      database.createCollection(c.getTarget)
      database.getCollection(c.getTarget)
    })
    .toMap

  private val configMap = settings.kcql.map(c => c.getSource -> c).toMap
  //initialize error tracker
  initialize(settings.taskRetries, settings.errorPolicy)

  /**
    * Write SinkRecords to MongoDb.
    *
    * @param records A list of SinkRecords from Kafka Connect to write.
    **/
  def write(records: Seq[SinkRecord]): Unit = {
    if (records.isEmpty) {
      logger.debug("No records received.")
    } else {
      logger.debug(s"Received ${records.size} records.")
      insert(records)
    }
  }

  /**
    * Write SinkRecords to MongoDb
    *
    * @param records A list of SinkRecords from Kafka Connect to write.
    * @return boolean indication successful write.
    **/
  private def insert(records: Seq[SinkRecord]) = {
    try {
      records.groupBy(_.topic()).foreach { case (topic, groupedRecords) =>
        val collection = collectionMap(topic)
        groupedRecords.map { record =>
          val (document, keysAndValues) = SinkRecordToDocument(
            record,
            settings.keyBuilderMap.getOrElse(record.topic(), Set.empty)
          )(settings)

          val config = configMap.getOrElse(record.topic(), sys.error(s"${record.topic()} is not handled by the configuration."))
          config.getWriteMode match {
            case WriteModeEnum.INSERT => new InsertOneModel[Document](document)
            case WriteModeEnum.UPSERT =>
              require(keysAndValues.nonEmpty, "Need to provide keys and values to identify the record to upsert")
              val filter = Filters.and(keysAndValues.map { case (k, v) => Filters.eq(k, v) }.toList: _*)
              new ReplaceOneModel[Document](
                filter,
                document,
                MongoWriter.UpdateOptions.upsert(true))
          }
        }.grouped(settings.batchSize)
          .foreach { batch =>
            collection.bulkWrite(batch.toList)
          }
      }
    }
    catch {
      case t: Throwable =>
        logger.error(s"There was an error inserting the records ${t.getMessage}", t)
        handleTry(Failure(t))
    }
  }

  def close(): Unit = {
    logger.info("Shutting down Mongo connect task.")
  }
}


//Factory to build
object MongoWriter extends StrictLogging {
  private val UpdateOptions = new UpdateOptions().upsert(true)

  def apply(connectorConfig: MongoConfig, context: SinkTaskContext): MongoWriter = {

    val settings = MongoSinkSettings(connectorConfig)
    //if error policy is retry set retry interval
    if (settings.errorPolicy.equals(ErrorPolicyEnum.RETRY)) {
      context.timeout(connectorConfig.getLong(MongoSinkConfigConstants.ERROR_RETRY_INTERVAL_CONFIG))
    }

    val connectionString = new MongoClientURI(settings.connection)
    logger.info(s"Initialising Mongo writer.Connection to $connectionString")
    val mongoClient = new MongoClient(connectionString)
    new MongoWriter(settings, mongoClient)
  }
}
