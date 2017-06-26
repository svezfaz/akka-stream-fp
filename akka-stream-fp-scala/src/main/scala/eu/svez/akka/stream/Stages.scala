package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Partition, Sink}

import scala.annotation.tailrec

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

  object BroadcastMat {

    def apply[In, Mat](first: Sink[In, Mat], second: Sink[In, Mat], rest: Sink[In, Mat]*): Sink[In, List[Mat]] = {

      def lift(sink: Sink[In, Mat]): Sink[In, List[Mat]] = sink.mapMaterializedValue(List(_))

      @tailrec
      def aux(sinks: List[Sink[In, Mat]], acc: Flow[In, In, List[Mat]]): Flow[In, In, List[Mat]] = sinks match {
        case s :: ss ⇒ aux(ss, acc.alsoToMat(lift(s))(_ ++ _))
        case _       ⇒ acc
      }

      val (middle :+ last) = second :: rest.toList

      aux(middle, Flow[In].alsoToMat(lift(first))(Keep.right))
        .toMat(lift(last))(_ ++ _)
    }
  }

}