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

package com.datamountaineer.streamreactor.connect.blockchain.data

import com.datamountaineer.streamreactor.connect.blockchain.json.JacksonJson
import org.scalatest.{Matchers, WordSpec}

class BlockchainMessageTest extends WordSpec with Matchers {
  "BlockChainMessage" should {
    "be parseable for a status message" in {
      val msg = JacksonJson.fromJson[BlockchainMessage]("{\"op\":\"status\", \"msg\": \"Connected, Subscribed, Welcome etc...\"}")
      msg.msg shouldBe Some("Connected, Subscribed, Welcome etc...")
      msg.op shouldBe "status"
    }
  }
}
