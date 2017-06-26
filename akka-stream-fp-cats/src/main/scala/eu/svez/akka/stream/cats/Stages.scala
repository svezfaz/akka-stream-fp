package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Flow, GraphDSL}
import cats.data.Ior.{Left, Right}
import cats.data.{Ior, NonEmptyList, Validated, ValidatedNel}
import eu.svez.akka.stream.Stages._

object Stages {

  object PartitionValidated {
    def apply[E, A]() = GraphDSL.create[FanOutShape2[Validated[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val toEither = builder.add(Flow[Validated[E, A]].map(_.toEither))
      val either = builder.add(PartitionEither[E, A]())

      toEither ~> either.in

      new FanOutShape2[Validated[E, A], E, A](toEither.in, either.left, either.right)
    }
  }

  implicit class ValidatedShape[E, A](val shape: FanOutShape2[Validated[E, A], E, A]) extends AnyVal{
    def invalid = shape.out0
    def valid = shape.out1
  }

  object PartitionValidatedNel {
    def apply[E, A]() = GraphDSL.create[FanOutShape2[ValidatedNel[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val validated = builder.add(PartitionValidated[NonEmptyList[E], A]())

      new FanOutShape2[ValidatedNel[E, A], E, A](
        validated.in,
        validated.invalid.mapConcat(_.toList).outlet,
        validated.valid
      )
    }
  }

  implicit class ValidatedNelShape[E, A](val shape: FanOutShape2[ValidatedNel[E, A], E, A]) extends AnyVal{
    def invalid = shape.out0
    def valid = shape.out1
  }

  object PartitionIor {
    def apply[A, B]() = GraphDSL.create[FanOutShape2[Ior[A, B], A, B]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

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
    def left = shape.out0
    def right = shape.out1
  }

}