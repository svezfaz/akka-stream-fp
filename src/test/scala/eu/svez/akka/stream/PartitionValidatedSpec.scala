package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import cats.syntax.validated._

class PartitionValidatedSpec extends StageSpec {

  "PartitionValidated" should "partition a flow of Validated[E, A] in two flows of E and A" in new Test {
    val src = Source(List(
      1.valid[String],
      2.valid[String],
      "BOOM!".invalid[Int],
      3.valid[String],
      "BOOM 2!".invalid[Int]
    ))

    src.runWith(testSink)

    successProbe.request(3)
    failureProbe.request(2)

    successProbe.expectNext(1)
    successProbe.expectNext(2)
    successProbe.expectNext(3)
    failureProbe.expectNext("BOOM!")
    failureProbe.expectNext("BOOM 2!")
    successProbe.expectComplete()
    failureProbe.expectComplete()
  }

  trait Test {
    val failureProbe = TestSubscriber.probe[String]()
    val successProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] ⇒
      import GraphDSL.Implicits._
      import partitions._

      val valStage = builder.add(PartitionValidated[String, Int]())

      valStage.invalid ~> Sink.fromSubscriber(failureProbe)
      valStage.valid ~> Sink.fromSubscriber(successProbe)

      SinkShape(valStage.in)
    })
  }
}