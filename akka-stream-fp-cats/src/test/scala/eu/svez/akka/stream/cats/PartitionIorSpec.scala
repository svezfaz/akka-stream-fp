package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import cats.data.Ior
import eu.svez.akka.stream.cats.Stages._

class PartitionIorSpec extends StageSpec {

  "PartitionIor" should "partition a flow of Ior[A, B] into two flows of A and B" in new Test {
    val src = Source(List(
      Ior.Right(1),
      Ior.Right(2),
      Ior.Left("One"),
      Ior.Right(3),
      Ior.Left("Two"),
      Ior.Both("Three", 4)
    ))

    src.runWith(testSink)

    rightProbe.request(4)
    leftProbe.request(3)

    rightProbe.expectNext(1)
    rightProbe.expectNext(2)
    rightProbe.expectNext(3)
    rightProbe.expectNext(4)
    leftProbe.expectNext("One")
    leftProbe.expectNext("Two")
    leftProbe.expectNext("Three")
    rightProbe.expectComplete()
    leftProbe.expectComplete()
  }

  trait Test {
    val leftProbe = TestSubscriber.probe[String]()
    val rightProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val iorStage = builder.add(PartitionIor[String, Int]())

      iorStage.left ~> Sink.fromSubscriber(leftProbe)
      iorStage.right ~> Sink.fromSubscriber(rightProbe)

      SinkShape(iorStage.in)
    })
  }
}