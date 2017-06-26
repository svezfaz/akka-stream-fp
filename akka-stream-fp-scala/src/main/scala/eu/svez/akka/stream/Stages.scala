package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}

import scala.util.{Failure, Success, Try}

object Stages {

  object PartitionEither {
    def apply[A, B]() = GraphDSL.create[FanOutShape2[Either[A, B], A, B]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val left = builder.add(Flow[Either[A, B]].map (_.left.get))
      val right = builder.add(Flow[Either[A, B]].map (_.right.get))
      val partition = builder.add(Partition[Either[A, B]](2, _.fold(_ ⇒ 0, _ ⇒ 1)))

      partition ~> left
      partition ~> right

      new FanOutShape2[Either[A, B], A, B](partition.in, left.out, right.out)
    }
  }

  implicit class EitherShape[A, B](val shape: FanOutShape2[Either[A, B], A, B]) extends AnyVal {
    def left = shape.out0
    def right = shape.out1
  }

  object PartitionTry {
    def apply[T]() = GraphDSL.create[FanOutShape2[Try[T], Throwable, T]]() { implicit builder ⇒
      import GraphDSL.Implicits._

      val success = builder.add(Flow[Try[T]].collect { case Success(a) ⇒ a })
      val failure = builder.add(Flow[Try[T]].collect { case Failure(t) ⇒ t })
      val partition = builder.add(Partition[Try[T]](2, _.map(_ ⇒ 1).getOrElse(0)))

      partition ~> failure
      partition ~> success

      new FanOutShape2[Try[T], Throwable, T](partition.in, failure.out, success.out)
    }
  }

  implicit class TryShape[T](val shape: FanOutShape2[Try[T], Throwable, T]) extends AnyVal {
    def failure = shape.out0
    def success = shape.out1
  }

}