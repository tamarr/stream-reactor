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

package com.datamountaineer.streamreactor.connect.mqtt.source

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.UUID

import com.datamountaineer.streamreactor.connect.converters.MsgKey
import com.datamountaineer.streamreactor.connect.converters.source.{AvroConverter, BytesConverter, JsonSimpleConverter}
import com.datamountaineer.streamreactor.connect.mqtt.config.MqttSourceConfigConstants
import com.datamountaineer.streamreactor.connect.serialization.AvroSerializer
import com.sksamuel.avro4s.{RecordFormat, SchemaFor}
import io.confluent.connect.avro.AvroData
import io.moquette.proto.messages.{AbstractMessage, PublishMessage}
import io.moquette.server.Server
import io.moquette.server.config.ClasspathConfig
import org.apache.kafka.connect.data.{Schema, Struct}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.collection.JavaConversions._
import scala.util.Try

class MqttSourceTaskTest extends WordSpec with Matchers with BeforeAndAfter {
  val classPathConfig = new ClasspathConfig()

  val connection = "tcp://0.0.0.0:1883"
  val clientId = "MqttSourceTaskTest"
  val qs = 1
  val connectionTimeout = 1000
  val keepAlive = 1000

  var mqttBroker: Option[Server] = None
  before {
    mqttBroker = Some(new Server())
    mqttBroker.foreach(_.startServer(classPathConfig))
  }

  after {
    mqttBroker.foreach {
      _.stopServer()
    }

    val files = Seq("moquette_store.mapdb", "moquette_store.mapdb.p", "moquette_store.mapdb.t")
    files.map(f => new File(f)).withFilter(_.exists()).foreach { f => Try(f.delete()) }
  }

  private def getSchemaFile(mqttSource: String, schema: org.apache.avro.Schema) = {
    val schemaFile = Paths.get(UUID.randomUUID().toString)

    def writeSchema(schema: org.apache.avro.Schema): File = {

      val bw = new BufferedWriter(new FileWriter(schemaFile.toFile))
      bw.write(schema.toString)
      bw.close()

      val file = schemaFile.toFile
      file.deleteOnExit()
      file
    }

    writeSchema(schema)
  }

  "should start a task and subscribe to the topics provided" in {

    val source1 = "/mqttSource1"
    val source2 = "/mqttSource2"
    val source3 = "/mqttSource3"

    val target1 = "kafkaTopic1"
    val target2 = "kafkaTopic2"
    val target3 = "kafkaTopic3"


    val studentSchema = SchemaFor[Student]()
    val task = new MqttSourceTask
    task.start(Map(
      MqttSourceConfigConstants.CLEAN_SESSION_CONFIG -> "true",
      MqttSourceConfigConstants.CONNECTION_TIMEOUT_CONFIG -> connectionTimeout.toString,
      MqttSourceConfigConstants.KCQL_CONFIG -> s"INSERT INTO $target1 SELECT * FROM $source1;INSERT INTO $target2 SELECT * FROM $source2;INSERT INTO $target3 SELECT * FROM $source3",
      MqttSourceConfigConstants.KEEP_ALIVE_INTERVAL_CONFIG -> keepAlive.toString,
      MqttSourceConfigConstants.CONVERTER_CONFIG -> s"$source1=${classOf[BytesConverter].getCanonicalName};$source2=${classOf[JsonSimpleConverter].getCanonicalName};$source3=${classOf[AvroConverter].getCanonicalName};",
      AvroConverter.SCHEMA_CONFIG -> s"$source3=${getSchemaFile(source3, studentSchema)}",
      MqttSourceConfigConstants.CLIENT_ID_CONFIG -> clientId,
      MqttSourceConfigConstants.THROW_ON_CONVERT_ERRORS_CONFIG -> "true",
      MqttSourceConfigConstants.HOSTS_CONFIG -> connection,
      MqttSourceConfigConstants.QS_CONFIG -> qs.toString
    ))


    val message1 = "message1".getBytes()


    val student = Student("Mike Bush", 19, 9.3)
    val message2 = JacksonJson.toJson(student).getBytes

    val recordFormat = RecordFormat[Student]

    val message3 = AvroSerializer.getBytes(student)

    publishMessage(source1, message1)
    publishMessage(source2, message2)
    publishMessage(source3, message3)
    Thread.sleep(2000)

    val records = task.poll()
    records.size() shouldBe 3

    val avroData = new AvroData(4)

    records.foreach { record =>

      record.keySchema() shouldBe MsgKey.schema
      val source = record.key().asInstanceOf[Struct].get("topic")
      record.topic() match {
        case `target1` =>
          source shouldBe source1
          record.valueSchema() shouldBe Schema.BYTES_SCHEMA
          record.value() shouldBe message1

        case `target2` =>
          source shouldBe source2

          record.valueSchema().field("name").schema() shouldBe Schema.STRING_SCHEMA
          record.valueSchema().field("name").index() shouldBe 0
          record.valueSchema().field("age").schema() shouldBe Schema.INT64_SCHEMA
          record.valueSchema().field("age").index() shouldBe 1
          record.valueSchema().field("note").schema() shouldBe Schema.FLOAT64_SCHEMA
          record.valueSchema().field("note").index() shouldBe 2

          val struct = record.value().asInstanceOf[Struct]
          struct.getString("name") shouldBe student.name
          struct.getInt64("age") shouldBe student.age.toLong
          struct.getFloat64("note") shouldBe student.note

        case `target3` =>
          source shouldBe source3

          record.valueSchema().field("name").schema() shouldBe Schema.STRING_SCHEMA
          record.valueSchema().field("name").index() shouldBe 0
          record.valueSchema().field("age").schema() shouldBe Schema.INT32_SCHEMA
          record.valueSchema().field("age").index() shouldBe 1
          record.valueSchema().field("note").schema() shouldBe Schema.FLOAT64_SCHEMA
          record.valueSchema().field("note").index() shouldBe 2

          val struct = record.value().asInstanceOf[Struct]
          struct.getString("name") shouldBe student.name
          struct.getInt32("age") shouldBe student.age
          struct.getFloat64("note") shouldBe student.note

        case other => fail(s"$other is not a valid topic")
      }

    }

    task.stop()
  }

  private def publishMessage(topic: String, payload: Array[Byte]) = {
    val message = new PublishMessage()
    message.setTopicName(s"$topic")
    message.setRetainFlag(false)
    message.setQos(AbstractMessage.QOSType.EXACTLY_ONCE)
    message.setPayload(ByteBuffer.wrap(payload))
    mqttBroker.foreach(_.internalPublish(message))
  }
}
