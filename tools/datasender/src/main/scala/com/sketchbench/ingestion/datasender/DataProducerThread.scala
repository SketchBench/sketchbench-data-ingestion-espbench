package com.sketchbench.ingestion.datasender

import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import com.sketchbench.ingestion.commons.util.Logging
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction.decompose
import net.liftweb.json.JsonAST.compactRender

class DataProducerThread(dataProducer: DataProducer,
                         val dataReader: DataReader,
                         topic: String,
                         duration: Long,
                         durationTimeUnit: TimeUnit)
  extends Runnable with Logging {

  var numberOfRecords: Int = 0
  val startTime: Long = currentTime
  val endTime: Long = startTime + durationTimeUnit.toMillis(duration)

  private var topicMsgIdList: Map[String, Int] = Map[String, Int]()

  def getNextMessageId(topic: String): Int = {
    var id = 0
    if (topicMsgIdList.contains(topic)) {
      id = topicMsgIdList(topic)
      id += 1
    }
    topicMsgIdList += (topic -> id)
    id
  }

  def currentTime: Long = System.currentTimeMillis()

  def run() {
    val msg = dataReader.readRecord
    if (currentTime < endTime && msg.nonEmpty) {
      send(msg.get)
    } else {
      logger.info(s"Shut down after $durationTimeUnit: $duration.")
      dataProducer.shutDown()
    }
  }

  def send(message: String): Unit = {
    val msgArray = message.split("\\s")
    val payload = Map(
      "messageID" -> getNextMessageId(topic),
      "ts" -> msgArray(0),
      "index" -> msgArray(1).toLong,
      "mf01" -> msgArray(2).toInt,
      "mf02" -> msgArray(3).toInt,
      "mf03" -> msgArray(4).toInt,
      "pc13" -> msgArray(5).toInt,
      "pc14" -> msgArray(6).toInt,
      "pc15" -> msgArray(7).toInt,
      "pc25" -> msgArray(8).toInt,
      "pc26" -> msgArray(9).toInt,
      "pc27" -> msgArray(10).toInt,
      "res" -> msgArray(11).toInt,
      "mf01" -> msgArray(2).toInt,
      "mf01" -> msgArray(2).toInt
    )
    sendToKafka(topic, createJsonString(payload))
  }
  def createJsonString(message: Map[String, Any]): String = {
    implicit val formats = DefaultFormats
    val jsonString = compactRender(decompose(message))
    jsonString
  }

  def sendToKafka(topic: String, message: String): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    dataProducer.getKafkaProducer.send(record)
    logger.debug(s"Sent value $message to topic $topic.")
  }
}

