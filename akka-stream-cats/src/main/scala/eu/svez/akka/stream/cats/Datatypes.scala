package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.FanOutShape2
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object Datatypes {

  object PartitionValidated {
    def apply[E, A]() = GraphDSL.create[FanOutShape2[Validated[E, A], E, A]]() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val valid = builder.add(Flow[Validated[E, A]].collect { case Valid(x) => x })
      val invalid = builder.add(Flow[Validated[E, A]].collect { case Invalid(x) => x })
      val partition = builder.add(Partition[Validated[E, A]](2, _.map(_ => 1).getOrElse(0)))

      partition ~> invalid
      partition ~> valid

      new FanOutShape2[Validated[E, A], E, A](partition.in, invalid.out, valid.out)
    }
  }

  implicit class ValidatedShapeOps[E, A](shape: FanOutShape2[Validated[E, A], E, A]) {
    val invalid = shape.out0
    val valid = shape.out1
  }

}