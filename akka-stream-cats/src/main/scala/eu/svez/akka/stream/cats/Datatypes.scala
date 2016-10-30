package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.scaladsl.{Flow, GraphDSL, Partition}
import akka.stream.FanOutShape2
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object PartitionValidated {
  def apply[E, A]() = GraphDSL.create[FanOutShape2[Validated[E, A], E, A]](){ implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val valid = builder.add(Flow[Validated[E, A]].collect{ case Valid(x) => x })
    val invalid = builder.add(Flow[Validated[E, A]].collect{ case Invalid(x) => x })
    val partition = builder.add(Partition[Validated[E, A]](2, _.map(_ => 1).getOrElse(0)))

    partition.out(0) ~> invalid.in
    partition.out(1) ~> valid.in

    new FanOutShape2(partition.in, invalid.out, valid.out)
  }
}

