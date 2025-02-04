import cats.effect.{Resource, Sync}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Executor, Index, Indexable}
import com.sksamuel.elastic4s.cats.effect.instances.*
import com.sksamuel.elastic4s.ElasticDsl.*
import org.typelevel.log4cats.Logger
import cats.syntax.all.*
import com.sksamuel.elastic4s.circe.indexableWithCirce
import io.circe.{Encoder, Json}

trait ElasticSearchIndexer[F[_], K, V]:
  def index(key: K, value: V): F[Unit]

object ElasticSearchIndexer:

  opaque type ElasticSearchUrl <: String = String
  object ElasticSearchUrl:
    val viaEnv: ElasticSearchUrl = System.getenv("ELASTIC_SEARCH_URL")
  
  trait IndexOf[T]:
    def index(value: T): Index

  extension[F[_], V] (indexer: ElasticSearchIndexer[F, V, V])
    def publishIndex(value: V): F[Unit] = indexer.index(value, value)

  def ofValue[F[_]: Sync: Logger: Executor, V: IndexOf: Encoder]: ElasticSearchUrl ?=> Resource[F, ElasticSearchIndexer[F, V, V]]
    = apply[F, V, V]

  def apply[F[_]: Sync: Logger: Executor, K: IndexOf, V: Encoder]: ElasticSearchUrl ?=> Resource[F, ElasticSearchIndexer[F, K, V]] = Resource
    .fromAutoCloseable:
      Sync[F].pure(JavaClient(ElasticProperties(summon[ElasticSearchUrl])))
    .map(ElasticClient(_))
    .map: client =>
      (key: K, value: V) => client
        .execute:
          indexInto(summon[IndexOf[K]].index(key))
            .doc(
              s"""{"value": ${summon[Indexable[V]].json(value)}}"""
            )
        .flatMap: response =>
          if (response.isError) {
            Logger[F].error(s"Failed to index record: ${response.error}")
          } else {
            Logger[F].info(s"Successfully indexed document into $key")
          }
        .handleErrorWith: error =>
          Logger[F].error(error)(s"Exception during indexing")