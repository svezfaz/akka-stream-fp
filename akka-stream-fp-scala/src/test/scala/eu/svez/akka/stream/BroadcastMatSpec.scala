package eu.svez.akka.stream

import akka.stream.scaladsl.{Sink, Source}
import eu.svez.akka.stream.Stages._

import scala.concurrent.Future

class BroadcastMatSpec extends StageSpec {

  "BroadcastMat" should "broadcast to a bunch of sinks, accumulating their materialized values" in {
    val src = Source(List(1, 2, 3))

    val snk1: Sink[Int, Future[Int]] = Sink.head[Int]
    val snk2: Sink[Int, Future[Int]] = Sink.reduce[Int](_ + _)
    val snk3: Sink[Int, Future[Int]] = Sink.last[Int]

    val broadcastSnk: Sink[Int, List[Future[Int]]] = BroadcastMat(snk1, snk2, snk3)

    val results = Future.sequence(src.runWith(broadcastSnk)).futureValue

    results shouldBe List(1, 6, 3)
  }

}