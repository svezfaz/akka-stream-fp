package svez.akka.stream.stages

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import akka.stream.{FanOutShape2, Graph, Outlet}
import cats.data.Ior._
import cats.data.{Ior, NonEmptyList, Validated, ValidatedNel}

import scala.util.Try

object partitions {

  object PartitionEither {
    def apply[A, B](): Graph[FanOutShape2[Either[A, B], A, B], NotUsed] =
      GraphDSL.create[FanOutShape2[Either[A, B], A, B]]() { implicit builder: GraphDSL.Builder[NotUsed] ⇒
        val left      = builder.add(Flow[Either[A, B]].map (_.left.get))
        val right     = builder.add(Flow[Either[A, B]].map (_.right.get))
        val partition = builder.add(Partition[Either[A, B]](2, _.fold(_ ⇒ 0, _ ⇒ 1)))

        partition ~> left
        partition ~> right

        new FanOutShape2[Either[A, B], A, B](partition.in, left.out, right.out)
    }
  }

  implicit class EitherShape[A, B](val shape: FanOutShape2[Either[A, B], A, B]) extends AnyVal {
    def left : Outlet[A] = shape.out0
    def right: Outlet[B] = shape.out1
  }

  object PartitionTry {
    def apply[T](): Graph[FanOutShape2[Try[T], Throwable, T], NotUsed] =
      GraphDSL.create[FanOutShape2[Try[T], Throwable, T]]() { implicit builder ⇒

        val toEither = builder.add(Flow.fromFunction((v: Try[T]) ⇒ v.toEither))
        val either   = builder.add(PartitionEither[Throwable, T]())

        toEither ~> either.in

        new FanOutShape2[Try[T], Throwable, T](toEither.in, either.left, either.right)
    }
  }

  implicit class TryShape[T](val shape: FanOutShape2[Try[T], Throwable, T]) extends AnyVal {
    def failure: Outlet[Throwable] = shape.out0
    def success: Outlet[T] = shape.out1
  }

  object PartitionValidated {
    def apply[E, A](): Graph[FanOutShape2[Validated[E, A], E, A], NotUsed] =
      GraphDSL.create[FanOutShape2[Validated[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] ⇒

        val toEither = builder.add(Flow.fromFunction((v: Validated[E, A]) ⇒ v.toEither))
        val either   = builder.add(PartitionEither[E, A]())

        toEither ~> either.in

        new FanOutShape2[Validated[E, A], E, A](toEither.in, either.left, either.right)
      }
  }

  implicit class ValidatedShape[E, A](val shape: FanOutShape2[Validated[E, A], E, A]) extends AnyVal{
    def invalid: Outlet[E] = shape.out0
    def valid  : Outlet[A] = shape.out1
  }

  object PartitionValidatedNel {
    def apply[E, A](): Graph[FanOutShape2[ValidatedNel[E, A], E, A], NotUsed] =
      GraphDSL.create[FanOutShape2[ValidatedNel[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] ⇒

        val validated = builder.add(PartitionValidated[NonEmptyList[E], A]())

        new FanOutShape2[ValidatedNel[E, A], E, A](
          validated.in,
          validated.invalid.mapConcat(_.toList).outlet,
          validated.valid
        )
      }
  }

  implicit class ValidatedNelShape[E, A](val shape: FanOutShape2[ValidatedNel[E, A], E, A]) extends AnyVal{
    def invalid: Outlet[E] = shape.out0
    def valid  : Outlet[A] = shape.out1
  }

  object PartitionIor {
    def apply[A, B](): Graph[FanOutShape2[Ior[A, B], A, B], NotUsed] =
      GraphDSL.create[FanOutShape2[Ior[A, B], A, B]]() { implicit builder: GraphDSL.Builder[NotUsed] ⇒

        val flatten = builder.add(Flow[Ior[A, B]]
          .mapConcat(_.fold(a ⇒ List(Left(a)), b ⇒ List(Right(b)), (a, b) ⇒ List(Left(a), Right(b))))
          .map(_.onlyLeftOrRight.get)
        )
        val either = builder.add(PartitionEither[A, B]())

        flatten ~> either.in

        new FanOutShape2[Ior[A, B], A, B](flatten.in, either.left, either.right)
      }
  }

  implicit class IorShape[A, B](val shape: FanOutShape2[Ior[A, B], A, B]) extends AnyVal{
    def left : Outlet[A] = shape.out0
    def right: Outlet[B] = shape.out1
  }


}