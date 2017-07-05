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

package com.datamountaineer.streamreactor.connect.jms.sink.writer

import javax.jms._

import com.datamountaineer.streamreactor.connect.errors.ErrorHandler
import com.datamountaineer.streamreactor.connect.jms.JMSSessionProvider
import com.datamountaineer.streamreactor.connect.jms.config.{JMSSetting, JMSSettings}
import com.datamountaineer.streamreactor.connect.jms.sink.converters.{JMSMessageConverter, JMSMessageConverterFn}
import com.datamountaineer.streamreactor.connect.schemas.ConverterUtil
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.connect.sink.SinkRecord

import scala.util.{Failure, Success, Try}

case class JMSWriter(settings: JMSSettings) extends AutoCloseable with ConverterUtil with ErrorHandler with StrictLogging {

  val provider = JMSSessionProvider(settings, sink = true)
  provider.start()
  val producers: Map[String, MessageProducer] = provider.queueProducers ++ provider.topicProducers
  val converterMap: Map[String, JMSMessageConverter] = settings.settings.map(s => (s.source, JMSMessageConverterFn(s.format))).toMap
  val settingsMap: Map[String, JMSSetting] = settings.settings.map(s => (s.source, s)).toMap

  //initialize error tracker
  initialize(settings.retries, settings.errorPolicy)

  /**
    * Convert to a JMS record from a SinkRecord based
    * on the specified format in KCQL
    * */
  def createJMSRecord(record: SinkRecord): (String, Message) = {
    val converter = converterMap(record.topic())
    converter.convert(record, provider.session, settingsMap(record.topic()))
  }

  /**
    * Write the records
    * */
  def write(records: Seq[SinkRecord]): Option[Unit] = {
    //convert and send, commit the session if good
    val sent = Try({
      val messages = records.map(createJMSRecord)
      send(messages)
      provider.session.commit()
    })

    //rollback on failure
    sent match {
      case Failure(f) =>
        logger.error(s"Error processing messages, ${f.getMessage}")
        provider.session.rollback()
        //handle error tracking for redelivery for Connect
        handleTry(sent)
      case _ => handleTry(Success())
    }
  }

  /**
    * Send the messages to the JMS destination
    * */
  def send(messages: Seq[(String, Message)]): Unit = {
    messages.foreach({ case (name, message) => producers(name).send(message)})
  }

  override def close(): Unit = provider.close()
}