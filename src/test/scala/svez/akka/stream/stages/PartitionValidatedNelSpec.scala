package svez.akka.stream.stages

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import cats.data.NonEmptyList
import cats.syntax.validated._

class PartitionValidatedNelSpec extends StageSpec {

  "PartitionValidatedNel" should "partition a flow of ValidatedNel[E, A] in two flows of E and A" in new Test {
    val src = Source(List(
      1.valid[NonEmptyList[String]],
      2.valid[NonEmptyList[String]],
      NonEmptyList.of("BOOM!", "KABOOM!").invalid[Int],
      3.valid[NonEmptyList[String]],
      NonEmptyList.of("BOOM 2!").invalid[Int]
    ))

    src.runWith(testSink)

    successProbe.request(3)
    failureProbe.request(3)

    successProbe.expectNext(1)
    successProbe.expectNext(2)
    successProbe.expectNext(3)
    failureProbe.expectNext("BOOM!")
    failureProbe.expectNext("KABOOM!")
    failureProbe.expectNext("BOOM 2!")
    successProbe.expectComplete()
    failureProbe.expectComplete()
  }

  trait Test {
    val failureProbe = TestSubscriber.probe[String]()
    val successProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] ⇒
      import GraphDSL.Implicits._
      import svez.akka.stream.stages.partitions._

      val valStage = builder.add(PartitionValidatedNel[String, Int]())

      valStage.invalid ~> Sink.fromSubscriber(failureProbe)
      valStage.valid ~> Sink.fromSubscriber(successProbe)

      SinkShape(valStage.in)
    })
  }
}