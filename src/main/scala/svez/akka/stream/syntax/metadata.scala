package svez.akka.stream.syntax

import akka.stream.scaladsl.{Keep, Sink, Source}
import cats.data.Ior
import svez.akka.stream.stages.diverters.DivertLeftIor
import svez.akka.stream.stages.ior

import scala.concurrent.{ExecutionContext, Future}

object metadata {

  final implicit class SourceMetaOps[M, O, Mat](val source: Source[Ior[M, O], Mat]) extends AnyVal {

    import ior._

    def mapData[T](f: O ⇒ T): Source[Ior[M, T], Mat] = source.via(mapRight(f))

    def mapAsyncData[T](parallelism: Int)(f: O ⇒ Future[T])(implicit ec: ExecutionContext): Source[Ior[M, T], Mat] =
      source.via(mapAsyncRight(parallelism)(f))

    def mapMeta[T](f: M ⇒ T): Source[Ior[T, O], Mat] = source.via(mapLeft(f))

    def divertMetaToMat[MatM, Mat2](metadataSink: Sink[M, MatM])(combine: (Mat, MatM) ⇒ Mat2): Source[O, Mat2] =
      source.viaMat(DivertLeftIor(metadataSink))(combine)

    def divertMetaTo[MatM, Mat2](metadataSink: Sink[M, MatM]): Source[O, Mat] = divertMetaToMat(metadataSink)(Keep.left)
  }
}
