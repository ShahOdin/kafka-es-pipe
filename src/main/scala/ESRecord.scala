import ElasticSearchIndexer.IndexOf
import com.sksamuel.elastic4s.Index
import fs2.kafka.CommittableConsumerRecord
import io.circe.{Encoder, Json}

import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant, ZoneId}
import scala.util.chaining.*

case class ESRecord(value: Json, topicName: String, timestamp: Instant)

object ESRecord:
  
  def apply[F[_], K, V: Encoder](r: CommittableConsumerRecord[F, K, V]): ESRecord = ESRecord(
    value = summon[Encoder[V]](r.record.value),
    topicName = r.record.topic,
    timestamp = Instant.ofEpochMilli(
      r.record.timestamp.pipe: ts =>
        ts.logAppendTime
          .orElse(ts.createTime)
          .orElse(ts.unknownTime)
          .get
    )
  )
  
  given IndexOf[ESRecord] with
    def index(value: ESRecord): Index = Index(value.associatedIndex)
  
  given Encoder[ESRecord] = Encoder.encodeJson.contramap(_.value)
  
  extension (value: Instant)
    def rotatingEvery15Mins: Instant = value
      .minusMillis(value.toEpochMilli % Duration.ofMinutes(15).toMillis)
  
  extension (value: ESRecord)
    private def formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd-HH-mm")
      .withZone(ZoneId.systemDefault())
    def associatedIndex: String = s"${value.topicName}_${formatter.format(value.timestamp.rotatingEvery15Mins)}"