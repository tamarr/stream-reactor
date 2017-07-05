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

package com.datamountaineer.streamreactor.connect.rethink.config

import com.datamountaineer.streamreactor.connect.rethink.TestBase
import org.scalatest.mock.MockitoSugar


/**
  * Created by andrew@datamountaineer.com on 21/06/16. 
  * stream-reactor-maven
  */
class TestReThinkSourceSettings extends TestBase with MockitoSugar {
  "should create a ReThinkSourceSetting for all fields with Initialize" in {
    val config = ReThinkSourceConfig(getPropsSource)
    val settings = ReThinkSourceSettings(config)
    val routes = settings.routes.head
    routes.getSource shouldBe TABLE
    routes.getTarget shouldBe TOPIC
    routes.isInitialize shouldBe true
  }

  "should create a ReThinkSourceSetting for all fields without Initialize" in {
    val config = ReThinkSourceConfig(getPropsSourceDelta)
    val settings = ReThinkSourceSettings(config)
    val routes = settings.routes.head
    routes.getSource shouldBe TABLE
    routes.getTarget shouldBe TOPIC
    routes.isInitialize shouldBe false
  }
}
