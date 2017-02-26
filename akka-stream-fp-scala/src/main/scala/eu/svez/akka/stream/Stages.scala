package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}

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

}