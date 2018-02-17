package eu.svez.akka.stream

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source}
import akka.stream.testkit.TestSubscriber
import eu.svez.akka.stream.partitions._

import scala.util.{Failure, Success}

class PartitionTrySpec extends StageSpec {

  "PartitionTry" should "partition a flow of Try[T] into two flows of Throwable and T" in new Test {
    val src = Source(List(
      Success(1),
      Success(2),
      Failure(new IllegalArgumentException("error 1")),
      Success(3),
      Failure(new ArrayIndexOutOfBoundsException("error 2"))
    ))

    src.runWith(testSink)

    successProbe.request(4)
    failureProbe.request(3)

    successProbe.expectNext(1)
    successProbe.expectNext(2)
    successProbe.expectNext(3)

    val t1 = failureProbe.expectNext()
    t1 shouldBe an[IllegalArgumentException]
    t1 should have message "error 1"

    val t2 = failureProbe.expectNext()
    t2 shouldBe an[ArrayIndexOutOfBoundsException]
    t2 should have message "error 2"

    successProbe.expectComplete()
    failureProbe.expectComplete()
  }

  trait Test {
    val failureProbe = TestSubscriber.probe[Throwable]()
    val successProbe = TestSubscriber.probe[Int]()

    val testSink = Sink.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] ⇒
      import GraphDSL.Implicits._

      val tryStage = builder.add(PartitionTry[Int]())

      tryStage.failure ~> Sink.fromSubscriber(failureProbe)
      tryStage.success ~> Sink.fromSubscriber(successProbe)

      SinkShape(tryStage.in)
    })
  }
}