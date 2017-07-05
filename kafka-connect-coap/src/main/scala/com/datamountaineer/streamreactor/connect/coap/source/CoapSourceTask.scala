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

package com.datamountaineer.streamreactor.connect.coap.source

import java.util
import java.util.concurrent.LinkedBlockingQueue

import com.datamountaineer.streamreactor.connect.coap.configs.{CoapConstants, CoapSettings, CoapSourceConfig}
import com.datamountaineer.streamreactor.connect.queues.QueueHelpers
import com.datamountaineer.streamreactor.connect.utils.ProgressCounter
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.connect.source.{SourceRecord, SourceTask}

import scala.collection.JavaConversions._

/**
  * Created by andrew@datamountaineer.com on 27/12/2016. 
  * stream-reactor
  */
class CoapSourceTask extends SourceTask with StrictLogging {
  private var readers : Set[CoapReader] = _
  private var progressCounter = new ProgressCounter
  private var enableProgress: Boolean = false
  private val queue = new LinkedBlockingQueue[SourceRecord]()
  private var batchSize : Int = CoapConstants.BATCH_SIZE_DEFAULT

  override def start(props: util.Map[String, String]): Unit = {
    logger.info(scala.io.Source.fromInputStream(getClass.getResourceAsStream("/coap-source-ascii.txt")).mkString)
    val config = CoapSourceConfig(props)
    val settings = CoapSettings(config)
    batchSize = config.getInt(CoapConstants.BATCH_SIZE)
    enableProgress = config.getBoolean(CoapConstants.PROGRESS_COUNTER_ENABLED)
    readers = CoapReaderFactory(settings, queue)
  }

  override def poll(): util.List[SourceRecord] = {

    val records = new util.ArrayList[SourceRecord]()

    //wait for batch size
    while (queue.size() > 0 && records.size() <= batchSize) {
      records.addAll(QueueHelpers.drainQueue(queue, batchSize))
    }

    if (enableProgress) {
      progressCounter.update(records.toVector)
    }
    records
  }

  override def stop(): Unit = {
    logger.info("Stopping Coap source and closing connections.")
    readers.foreach(_.stop)
    progressCounter.empty
  }

  override def version(): String = getClass.getPackage.getImplementationVersion
}
