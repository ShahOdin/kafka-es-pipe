import cats.effect.Async
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, KafkaConsumer}
import io.circe.Encoder
import org.typelevel.log4cats.Logger

import scala.util.chaining.*
import scala.concurrent.duration.*

/**
 * The records bundled together are from the same partition and are ordered.
 */
trait FetchRecords[F[_]]:
  def partitionedRecords: F[Vector[ESRecord]]

object FetchRecords:
  val kafkaTopic: String = "clicks"

  opaque type KafkaBootStrapServers <: String = String
  object KafkaBootStrapServers:
    val viaEnv: KafkaBootStrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")

  //todo: make this generic over key, value types, make topic an env var.
  def kafkaStream[F[+_]: Async: Logger]: KafkaBootStrapServers ?=> FetchRecords[[A] =>> fs2.Stream[F, A]] =
    new FetchRecords[[A] =>> fs2.Stream[F, A]]:
      override def partitionedRecords: fs2.Stream[F, Vector[ESRecord]] = ConsumerSettings(
        keyDeserializer = Deserializer.string[F],
        valueDeserializer = Deserializer.string[F]
      )
        .withBootstrapServers(summon[KafkaBootStrapServers])
        .withGroupId("scala-es-consumer-group")
        .withClientId("consumer-1")
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .pipe(KafkaConsumer.stream(_))
        .evalTap(_ => Logger[F].info("Connected to Kafka"))
        .subscribeTo(kafkaTopic)
        .evalTap(_ => Logger[F].info(s"Subscribed to topic: $kafkaTopic"))
        .flatMap(_.partitionedRecords)
        .map:
          _.groupWithin(3, 3.seconds)
            .filter(_.nonEmpty)
            .map(_.toVector)
            .evalTap(v => Logger[F].info(s"received new messages from${v.map(_.record.value).mkString}, and partition: ${v.map(_.record.partition).mkString}"))
            .map(_.map(ESRecord(_)))
        .parJoinUnbounded
