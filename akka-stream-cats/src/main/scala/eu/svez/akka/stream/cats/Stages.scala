package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition}
import cats.data.{Ior, Validated}
import cats.data.Validated.{Invalid, Valid}
import cats.data.Ior.{Both, Left, Right}

object Stages {

  object ValidatedStage {
    def apply[E, A]() = GraphDSL.create[FanOutShape2[Validated[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val valid = builder.add(Flow[Validated[E, A]].map (x => (x: @unchecked) match { case Valid(v) => v }))
      val invalid = builder.add(Flow[Validated[E, A]].map (x => (x: @unchecked) match { case Invalid(i) => i }))
      val partition = builder.add(Partition[Validated[E, A]](2, _.map(_ => 1).getOrElse(0)))

      partition ~> invalid
      partition ~> valid

      new FanOutShape2[Validated[E, A], E, A](partition.in, invalid.out, valid.out)
    }
  }

  implicit class ValidatedShape[E, A](shape: FanOutShape2[Validated[E, A], E, A]) {
    val invalid = shape.out0
    val valid = shape.out1
  }

  object IorStage {
    def apply[A, B]() = GraphDSL.create[FanOutShape2[Ior[A, B], A, B]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      def left = builder.add(Flow[Ior[A, B]].map (x => (x: @unchecked) match { case Left(l) => l }))
      def right = builder.add(Flow[Ior[A, B]].map (x => (x: @unchecked) match { case Right(r) => r }))
      val partitionIor = builder.add(Partition[Ior[A, B]](3, {
        case Left(_) => 0
        case Both(_, _) => 1
        case Right(_) => 2
      }))
      val broadcastBoth = builder.add(Broadcast[Ior[A, B]](2))
      val mergeLeft = builder.add(Merge[A](2))
      val mergeRight = builder.add(Merge[B](2))

      partitionIor ~> left                   ~> mergeLeft
      partitionIor ~> broadcastBoth ~> left  ~> mergeLeft
                      broadcastBoth ~> right ~> mergeRight
      partitionIor ~> right                  ~> mergeRight

      new FanOutShape2[Ior[A, B], A, B](partitionIor.in, mergeLeft.out, mergeRight.out)
    }
  }

  implicit class IorShape[A, B](shape: FanOutShape2[Ior[A, B], A, B]) {
    val left = shape.out0
    val right = shape.out1
  }

}