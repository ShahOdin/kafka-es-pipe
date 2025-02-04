import ElasticSearchIndexer.ElasticSearchUrl
import FetchRecords.KafkaBootStrapServers
import cats.effect.{IO, IOApp}
import com.sksamuel.elastic4s.cats.effect.instances.*
import org.typelevel.log4cats.Logger
import cats.syntax.all.*
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple:
  given KafkaBootStrapServers = KafkaBootStrapServers.viaEnv
  given ElasticSearchUrl = ElasticSearchUrl.viaEnv
  
  override def run: IO[Unit] = Slf4jLogger.fromName[IO]("myLogger").flatMap: l=>
    given Logger[IO] = l
    ElasticSearchIndexer.ofValue[IO, ESRecord].use: client =>
      FetchRecords.kafkaStream[IO]
        .partitionedRecords
        .evalTap:
          _.traverse:r =>
            IO.println(s"Processing record at index: ${r.associatedIndex}") *> client.publishIndex(r)
        .compile
        .drain