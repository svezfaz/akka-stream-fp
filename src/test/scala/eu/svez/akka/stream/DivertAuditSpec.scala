package eu.svez.akka.stream

import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.TestSubscriber
import eu.svez.akka.stream.diverters._

class DivertAuditSpec extends StageSpec {

  val auditProbe  = TestSubscriber.probe[String]()
  val resultProbe = TestSubscriber.probe[Int]()

  val auditTestSink = Sink.fromSubscriber(auditProbe)
  val testSink     = Sink.fromSubscriber(resultProbe)

  "DivertAudit" should "divert the Audits to the provided Sink, while passing on the results" in {
    val src = Source(List(
      (Nil, 1),
      (List("event1"), 2),
      (List("event2", "event3"), 3),
      (Nil, 4)
    ))

    src.via(DivertAudit(to = auditTestSink)).runWith(testSink)

    resultProbe.request(5)
    auditProbe.request(4)

    resultProbe.expectNext(1)
    resultProbe.expectNext(2)
    resultProbe.expectNext(3)
    resultProbe.expectNext(4)
    auditProbe.expectNext("event1")
    auditProbe.expectNext("event2")
    auditProbe.expectNext("event3")
    resultProbe.expectComplete()
    auditProbe.expectComplete()
  }

}