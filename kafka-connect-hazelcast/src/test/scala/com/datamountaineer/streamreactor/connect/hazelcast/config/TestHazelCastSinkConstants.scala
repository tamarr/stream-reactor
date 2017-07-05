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

package com.datamountaineer.streamreactor.connect.hazelcast.config

import org.scalatest.WordSpec

/**
  * The point of this test is to check that constants keys are not changed after the refactor of the code.
  */
class TestHazelCastSinkConstants extends WordSpec {

  // Constants
  val CLUSTER_MEMBERS = "connect.hazelcast.cluster.members"
  val GROUP_NAME = "connect.hazelcast.group.name"
  val GROUP_PASSWORD = "connect.hazelcast.group.password"
  val PARALLEL_WRITE = "connect.hazelcast.parallel.write"
  val CONNECTION_TIMEOUT = "connect.hazelcast.connection.timeout"
  val CONNECTION_RETRY_ATTEMPTS = "connect.hazelcast.connection.retries"
  val KEEP_ALIVE = "connect.hazelcast.connection.keep.alive"
  val TCP_NO_DELAY = "connect.hazelcast.connection.tcp.no.delay"
  val REUSE_ADDRESS = "connect.hazelcast.connection.reuse.address"
  val LINGER_SECONDS = "connect.hazelcast.connection.linger.seconds"
  val BUFFER_SIZE = "connect.hazelcast.connection.buffer.size"
  val EXPORT_ROUTE_QUERY = "connect.hazelcast.kcql"
  val ERROR_POLICY = "connect.hazelcast.error.policy"
  val ERROR_RETRY_INTERVAL = "connect.hazelcast.retry.interval"
  val NBR_OF_RETRIES = "connect.hazelcast.max.retries"
  val THREAD_POOL_CONFIG = "connect.hazelcast.threadpool.size"

  "CLUSTER_MEMBERS should have the same key in HazelCastSinkConfigConstants" in {
    assert(CLUSTER_MEMBERS.equals(HazelCastSinkConfigConstants.CLUSTER_MEMBERS))
  }
  "GROUP_NAME should have the same key in HazelCastSinkConfigConstants" in {
    assert(GROUP_NAME.equals(HazelCastSinkConfigConstants.GROUP_NAME))
  }
  "GROUP_PASSWORD should have the same key in HazelCastSinkConfigConstants" in {
    assert(GROUP_PASSWORD.equals(HazelCastSinkConfigConstants.GROUP_PASSWORD))
  }
  "PARALLEL_WRITE should have the same key in HazelCastSinkConfigConstants" in {
    assert(PARALLEL_WRITE.equals(HazelCastSinkConfigConstants.PARALLEL_WRITE))
  }
  "CONNECTION_TIMEOUT should have the same key in HazelCastSinkConfigConstants" in {
    assert(CONNECTION_TIMEOUT.equals(HazelCastSinkConfigConstants.CONNECTION_TIMEOUT))
  }
  "CONNECTION_RETRY_ATTEMPTS should have the same key in HazelCastSinkConfigConstants" in {
    assert(CONNECTION_RETRY_ATTEMPTS.equals(HazelCastSinkConfigConstants.CONNECTION_RETRY_ATTEMPTS))
  }
  "KEEP_ALIVE should have the same key in HazelCastSinkConfigConstants" in {
    assert(KEEP_ALIVE.equals(HazelCastSinkConfigConstants.KEEP_ALIVE))
  }
  "TCP_NO_DELAY should have the same key in HazelCastSinkConfigConstants" in {
    assert(TCP_NO_DELAY.equals(HazelCastSinkConfigConstants.TCP_NO_DELAY))
  }
  "REUSE_ADDRESS should have the same key in HazelCastSinkConfigConstants" in {
    assert(REUSE_ADDRESS.equals(HazelCastSinkConfigConstants.REUSE_ADDRESS))
  }
  "LINGER_SECONDS should have the same key in HazelCastSinkConfigConstants" in {
    assert(LINGER_SECONDS.equals(HazelCastSinkConfigConstants.LINGER_SECONDS))
  }
  "BUFFER_SIZE should have the same key in HazelCastSinkConfigConstants" in {
    assert(BUFFER_SIZE.equals(HazelCastSinkConfigConstants.BUFFER_SIZE))
  }
  "EXPORT_ROUTE_QUERY should have the same key in HazelCastSinkConfigConstants" in {
    assert(EXPORT_ROUTE_QUERY.equals(HazelCastSinkConfigConstants.EXPORT_ROUTE_QUERY))
  }
  "ERROR_POLICY should have the same key in HazelCastSinkConfigConstants" in {
    assert(ERROR_POLICY.equals(HazelCastSinkConfigConstants.ERROR_POLICY))
  }
  "ERROR_RETRY_INTERVAL should have the same key in HazelCastSinkConfigConstants" in {
    assert(ERROR_RETRY_INTERVAL.equals(HazelCastSinkConfigConstants.ERROR_RETRY_INTERVAL))
  }
  "NBR_OF_RETRIES should have the same key in HazelCastSinkConfigConstants" in {
    assert(NBR_OF_RETRIES.equals(HazelCastSinkConfigConstants.NBR_OF_RETRIES))
  }
  "THREAD_POOL_CONFIG should have the same key in HazelCastSinkConfigConstants" in {
    assert(THREAD_POOL_CONFIG.equals(HazelCastSinkConfigConstants.THREAD_POOL_CONFIG))
  }
}
