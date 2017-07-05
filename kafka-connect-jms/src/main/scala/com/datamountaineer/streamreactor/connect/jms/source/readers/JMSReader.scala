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

package com.datamountaineer.streamreactor.connect.jms.source.readers

import javax.jms.{Message, MessageConsumer}

import com.datamountaineer.streamreactor.connect.converters.source.Converter
import com.datamountaineer.streamreactor.connect.jms.JMSSessionProvider
import com.datamountaineer.streamreactor.connect.jms.config.JMSSettings
import com.datamountaineer.streamreactor.connect.jms.source.domain.JMSStructMessage
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.connect.source.SourceRecord

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Created by andrew@datamountaineer.com on 10/03/2017. 
  * stream-reactor
  */
class JMSReader(settings: JMSSettings) extends StrictLogging {

  val provider = JMSSessionProvider(settings)
  provider.start()
  val consumers: Map[String, MessageConsumer] = provider.queueConsumers ++ provider.topicsConsumers
  val convertersMap: Map[String, Option[Converter]] = settings.settings.map(s => (s.source, s.sourceConverters)).toMap
  val topicsMap: Map[String, String] = settings.settings.map(s => (s.source, s.target)).toMap

  def poll(): Map[Message, SourceRecord] = {

    val messages = consumers
                      .flatMap({ case (source, consumer)=>
                        (1 to settings.batchSize)
                          .flatMap(_ => Option(consumer.receiveNoWait()))
                          .map(m => (m, convert(source, topicsMap(source), m)))
                      })

    messages
  }

  def convert(source: String, target: String,  message: Message): SourceRecord = {
    convertersMap(source).getOrElse(None) match {
      case c: Converter => c.convert(target, source, message.getJMSMessageID, JMSStructMessage.getPayload(message))
      case None => JMSStructMessage.getStruct(target, message)
    }
  }

  def stop: Try[Unit] = provider.close()
}

object JMSReader {
  def apply(settings: JMSSettings): JMSReader = new JMSReader(settings)
}
