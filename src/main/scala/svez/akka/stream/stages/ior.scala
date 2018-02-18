package svez.akka.stream.stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import cats.data.Ior

import scala.concurrent.{ExecutionContext, Future}

object ior {

  def mapLeft[L, R, O](f: L ⇒ O): Flow[Ior[L, R], Ior[O, R], NotUsed] =
    Flow[Ior[L, R]].map { _.leftMap(f) }

  def mapRight[L, R, O](f: R ⇒ O): Flow[Ior[L, R], Ior[L, O], NotUsed] =
    Flow[Ior[L, R]].map { _.map(f) }

  def mapAsyncLeft[L, R, O](parallelism: Int)(f: L ⇒ Future[O])(implicit ec: ExecutionContext): Flow[Ior[L, R], Ior[O, R], NotUsed] =
    Flow[Ior[L, R]].mapAsync(parallelism) {
      case Ior.Left(a)    ⇒ f(a).map(Ior.Left(_))
      case Ior.Both(a, b) ⇒ f(a).map(Ior.both(_, b))
      case r@Ior.Right(_) ⇒ Future.successful(r)
    }

  def mapAsyncRight[L, R, O](parallelism: Int)(f: R ⇒ Future[O])(implicit ec: ExecutionContext): Flow[Ior[L, R], Ior[L, O], NotUsed] =
    Flow[Ior[L, R]].mapAsync(parallelism) {
      case Ior.Right(b)   ⇒ f(b).map(Ior.Right(_))
      case Ior.Both(a, b) ⇒ f(b).map(Ior.both(a, _))
      case l@Ior.Left(_)  ⇒ Future.successful(l)
    }



}