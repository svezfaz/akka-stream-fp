package svez.akka.stream.stages

import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber
import svez.akka.stream.stages.diverters._

class DivertLeftSpec extends StageSpec {

  val leftProbe  = TestSubscriber.probe[String]()
  val rightProbe = TestSubscriber.probe[Int]()

  val leftTestSink = Sink.fromSubscriber(leftProbe)
  val testSink     = Sink.fromSubscriber(rightProbe)

  "DivertLeft" should "divert the Lefts to the provided Sink, while passing on the Rights" in {
    val src = Source(List(
      Right(1),
      Right(2),
      Left("One"),
      Right(3),
      Left("Two")
    ))

    src.via(DivertLeft(to = leftTestSink)).runWith(testSink)

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

}