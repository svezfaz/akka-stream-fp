package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import Stages._

class PartitionEitherSpec extends StageSpec {

  "PartitionEither" should "partition a flow of Either[A, B] into two flows of A and B" in new Test {
    val src = Source(List(
      Right(1),
      Right(2),
      Left("One"),
      Right(3),
      Left("Two")
    ))

    src.runWith(testSink)

    rightProbe.request(4)
    leftProbe.request(3)

    rightProbe.expectNext(1)
    rightProbe.expectNext(2)
    rightProbe.expectNext(3)
    leftProbe.expectNext("One")
    leftProbe.expectNext("Two")
    rightProbe.expectComplete()
    leftProbe.expectComplete()
  }

  trait Test {
    val leftProbe = TestSubscriber.probe[String]()
    val rightProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val eitherStage = builder.add(PartitionEither[String, Int]())

      eitherStage.left ~> Sink.fromSubscriber(leftProbe)
      eitherStage.right ~> Sink.fromSubscriber(rightProbe)

      SinkShape(eitherStage.in)
    })
  }
}