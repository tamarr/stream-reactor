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

package com.datamountaineer.streamreactor.connect.elastic

import com.datamountaineer.streamreactor.connect.elastic.config.{ElasticSettings, ElasticSinkConfig, ElasticSinkConfigConstants}
import com.sksamuel.elastic4s.xpack.security.XPackElasticClient
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.apache.kafka.connect.sink.SinkTaskContext
import org.elasticsearch.common.settings.Settings

object ElasticWriter {
  /**
    * Construct a JSONWriter.
    *
    * @param config An elasticSinkConfig to extract settings from.
    * @return An ElasticJsonWriter to write records from Kafka to ElasticSearch.
    **/
  def apply(config: ElasticSinkConfig, context: SinkTaskContext): ElasticJsonWriter = {
    val hostNames = config.getString(ElasticSinkConfigConstants.URL)
    val esClusterName = config.getString(ElasticSinkConfigConstants.ES_CLUSTER_NAME)
    val esPrefix = config.getString(ElasticSinkConfigConstants.URL_PREFIX)
    val essettings = Settings
      .builder()
      .put("cluster.name", esClusterName)
      .build()
    val uri = ElasticsearchClientUri(s"$esPrefix://$hostNames")

    val settings = ElasticSettings(config)

    val client = if (settings.xpackSettings.nonEmpty) {
      XPackElasticClient(essettings, uri, settings.xpackPluggins: _*)
    } else TcpClient.transport(essettings, uri)

    new ElasticJsonWriter(client, settings)
  }
}
