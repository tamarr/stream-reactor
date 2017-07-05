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

package com.datamountaineer.streamreactor.connect.cassandra.source

import java.util

import com.datamountaineer.connector.config.Config
import com.datamountaineer.streamreactor.connect.cassandra.config.{CassandraConfigConstants, CassandraConfigSource}
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.Task
import org.apache.kafka.connect.source.SourceConnector
import org.apache.kafka.connect.util.ConnectorUtils

import scala.collection.JavaConversions._

/**
  * <h1>CassandraSourceConnector</h1>
  * Kafka connect Cassandra Source connector
  *
  * Sets up CassandraSourceTask and configurations for the tasks.
  */
class CassandraSourceConnector extends SourceConnector with StrictLogging {

  private var configProps: Option[util.Map[String, String]] = None
  private val configDef = CassandraConfigSource.sourceConfig

  /**
    * Defines the sink class to use
    *
    * @return
    */
  override def taskClass(): Class[_ <: Task] = classOf[CassandraSourceTask]

  /**
    * Set the configuration for each work and determine the split.
    *
    * @param maxTasks The max number of task workers be can spawn.
    * @return a List of configuration properties per worker.
    **/
  override def taskConfigs(maxTasks: Int): util.List[util.Map[String, String]] = {
    val raw = configProps.get.get(CassandraConfigConstants.ROUTE_QUERY).split(";")

    val tables = raw.map { r => Config.parse(r).getSource }.toList

    val numGroups = Math.min(tables.size, maxTasks)

    logger.info(s"Setting task configurations for $numGroups workers.")
    val groups = ConnectorUtils.groupPartitions(tables, maxTasks)

    //setup the config for each task and set assigned tables
    groups
      .withFilter(g => g.nonEmpty)
      .map { g =>
        val taskConfigs = new java.util.HashMap[String, String](configProps.get)
        taskConfigs.put(CassandraConfigConstants.ASSIGNED_TABLES, g.mkString(","))
        taskConfigs
      }
  }

  /**
    * Start the sink and set to configuration.
    *
    * @param props A map of properties for the connector and worker.
    **/
  override def start(props: util.Map[String, String]): Unit = {
    configProps = Some(props)
  }

  override def stop(): Unit = {}

  /**
    * Gets the version of this sink.
    *
    * @return
    */
  override def version(): String = getClass.getPackage.getImplementationVersion

  override def config(): ConfigDef = configDef
}
