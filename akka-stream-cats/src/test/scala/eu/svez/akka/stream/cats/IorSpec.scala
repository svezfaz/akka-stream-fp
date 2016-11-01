package eu.svez.akka.stream.cats

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import cats.data.Ior
import eu.svez.akka.stream.cats.Stages._

class IorSpec extends Spec {

  "IorStage" should "emit " in new Test {
    val src = Source(List(
      Ior.Right(1),
      Ior.Right(2),
      Ior.Left("BOOM!"),
      Ior.Right(3),
      Ior.Left("BOOM 2!")
    ))

    src.runWith(testSink)

    rightProbe.request(3)
    leftProbe.request(2)

    rightProbe.expectNext(1)
    rightProbe.expectNext(2)
    rightProbe.expectNext(3)
    leftProbe.expectNext("BOOM!")
    leftProbe.expectNext("BOOM 2!")
    rightProbe.expectComplete()
    leftProbe.expectComplete()
  }

  trait Test {
    val leftProbe = TestSubscriber.probe[String]()
    val rightProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val iorStage = builder.add(IorStage[String, Int]())

      iorStage.left ~> Sink.fromSubscriber(leftProbe)
      iorStage.right ~> Sink.fromSubscriber(rightProbe)

      SinkShape(iorStage.in)
    })
  }
}