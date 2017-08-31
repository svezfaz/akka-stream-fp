package eu.svez.akka.stream

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Unzip}
import eu.svez.akka.stream.partitions.PartitionEither

import scala.collection.immutable.Seq

object diverters {

  object DivertLeft {

    def apply[T, L, M](to: Sink[L, M]): Flow[Either[L, T], T, M] = {
      Flow.fromGraph(GraphDSL.create(to) { implicit b ⇒ sink ⇒
        val partition = b.add(PartitionEither[L, T]())
        partition.out0 ~> sink
        FlowShape(partition.in, partition.out1)
      })
    }
  }

  object DivertAudit {

    def apply[T, A, M](to: Sink[A, M]): Flow[(Seq[A], T), T, M] = {
      Flow.fromGraph(GraphDSL.create(to) { implicit b ⇒ sink ⇒
        val unzip = b.add(Unzip[Seq[A], T]())
        unzip.out0.mapConcat(identity) ~> sink
        FlowShape(unzip.in, unzip.out1)
      })
    }
  }

}