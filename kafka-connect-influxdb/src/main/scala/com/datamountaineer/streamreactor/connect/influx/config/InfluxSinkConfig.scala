/*
 * *
 *   * Copyright 2016 Datamountaineer.
 *   *
 *   * Licensed under the Apache License, Version 2.0 (the "License");
 *   * you may not use this file except in compliance with the License.
 *   * You may obtain a copy of the License at
 *   *
 *   * http://www.apache.org/licenses/LICENSE-2.0
 *   *
 *   * Unless required by applicable law or agreed to in writing, software
 *   * distributed under the License is distributed on an "AS IS" BASIS,
 *   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   * See the License for the specific language governing permissions and
 *   * limitations under the License.
 *   *
 */

package com.datamountaineer.streamreactor.connect.influx.config

import java.util

import org.apache.kafka.common.config.ConfigDef.{Importance, Type}
import org.apache.kafka.common.config.{AbstractConfig, ConfigDef}

object InfluxSinkConfig {

  val INFLUX_URL_CONFIG = "connect.influx.connection.url"
  val INFLUX_URL_DOC =
    """
      |The InfluxDB database url.
    """.stripMargin

  val INFLUX_DATABASE_CONFIG = "connect.influx.connection.database"
  val INFLUX_DATABASE_DOC =
    """
      |The database to store the values to.
    """.stripMargin

  val INFLUX_CONNECTION_USER_CONFIG = "connect.influx.connection.user"
  val INFLUX_CONNECTION_USER_DOC =
    """
      |The user to connect to the influx database.
    """.stripMargin

  val INFLUX_CONNECTION_PASSWORD_CONFIG = "connect.influx.connection.password"
  val INFLUX_CONNECTION_PASSWORD_DOC =
    """
      |The password for the influxdb user.
    """.stripMargin

  val RETENTION_POLICY_CONFIG = "connect.influx.retention.policy"
  val RETENTION_POLICY_DEFAULT = "autogen"
  val RETENTION_POLICY_DOC =
    """
      |Determines how long InfluxDB keeps the data. The minimum retention period is one hour.
      |The default RP (Retention Period) is 'autogen' in Influx 1.0 onwards (and 'default' for previous versions),
      |and means an infinite retention period.
      |
      |To override the default retention period, enter a number followed by one character i.e. 1w - for one week retention
      |
      | m   - for minutes
      | h   - for hours
      | d   - for days
      | w   - for weeks
      | INF - for infinite
    """.stripMargin

  val KCQL_CONFIG = "connect.influx.sink.kcql"
  val KCQL_DOC =
    """
      |KCQL expression describing field selection and routes.
    """.stripMargin

  val ERROR_POLICY_CONFIG = "connect.influx.error.policy"
  val ERROR_POLICY_DEFAULT = "THROW"
  val ERROR_POLICY_DOC =
    """
      |Specifies the action to be taken if an error occurs while inserting the data.
      |
      |The following actions are available:
      |  NOOP - the error is swallowed.
      |  THROW - the error is allowed to propagate.
      |  RETRY - The exception causes the Connect framework to retry the message. The number of retries is based on 'connect.redis.max.retires'
      |
      |Errors are always logged
    """.stripMargin

  val ERROR_RETRY_INTERVAL_CONFIG = "connect.influx.retry.interval"
  val ERROR_RETRY_INTERVAL_DEFAULT = "60000"
  val ERROR_RETRY_INTERVAL_DOC =
    """
      |The time in milliseconds between retries.
    """.stripMargin

  val NBR_OF_RETRIES_CONFIG = "connect.influx.max.retires"
  val NBR_OF_RETIRES_DEFAULT = 20
  val NBR_OF_RETRIES_DOC =
    """
      |The maximum number of times to try the write again.
    """.stripMargin

  val config: ConfigDef = new ConfigDef()
    .define(INFLUX_URL_CONFIG, Type.STRING, Importance.HIGH, INFLUX_URL_DOC,
      "Connection", 1, ConfigDef.Width.MEDIUM, INFLUX_URL_CONFIG)
    .define(INFLUX_DATABASE_CONFIG, Type.STRING, Importance.HIGH, INFLUX_DATABASE_DOC,
      "Connection", 2, ConfigDef.Width.MEDIUM, INFLUX_DATABASE_CONFIG)
    .define(INFLUX_CONNECTION_USER_CONFIG, Type.STRING, Importance.HIGH, INFLUX_CONNECTION_USER_DOC,
      "Connection", 3, ConfigDef.Width.MEDIUM, INFLUX_CONNECTION_USER_CONFIG)
    .define(INFLUX_CONNECTION_PASSWORD_CONFIG, Type.PASSWORD, "", Importance.HIGH, INFLUX_CONNECTION_PASSWORD_DOC,
      "Connection", 4, ConfigDef.Width.MEDIUM, INFLUX_CONNECTION_PASSWORD_CONFIG)
    .define(KCQL_CONFIG, Type.STRING, Importance.HIGH, KCQL_DOC,
      "Connection", 5, ConfigDef.Width.MEDIUM, KCQL_CONFIG)
    .define(ERROR_POLICY_CONFIG, Type.STRING, ERROR_POLICY_DEFAULT, Importance.HIGH, ERROR_POLICY_DOC,
      "Connection", 6, ConfigDef.Width.MEDIUM, ERROR_POLICY_CONFIG)
    .define(ERROR_RETRY_INTERVAL_CONFIG, Type.INT, ERROR_RETRY_INTERVAL_DEFAULT, Importance.MEDIUM, ERROR_RETRY_INTERVAL_DOC,
      "Connection", 7, ConfigDef.Width.MEDIUM, ERROR_RETRY_INTERVAL_CONFIG)
    .define(NBR_OF_RETRIES_CONFIG, Type.INT, NBR_OF_RETIRES_DEFAULT, Importance.MEDIUM, NBR_OF_RETRIES_DOC,
      "Connection", 8, ConfigDef.Width.MEDIUM, NBR_OF_RETRIES_CONFIG)
    .define(RETENTION_POLICY_CONFIG, Type.STRING, RETENTION_POLICY_DEFAULT, Importance.HIGH, RETENTION_POLICY_DOC,
      "Connection", 9, ConfigDef.Width.MEDIUM, RETENTION_POLICY_DOC)
}

/**
  * <h1>InfluxSinkConfig</h1>
  *
  * Holds config, extends AbstractConfig.
  **/
case class InfluxSinkConfig(props: util.Map[String, String]) extends AbstractConfig(InfluxSinkConfig.config, props)
