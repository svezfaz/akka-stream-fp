package svez.akka.stream.examples

import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Sink}
import cats.data.Ior

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ReactiveKafkaExample {

  final case class MyRecord(stock: String, price: BigDecimal)

  def main(args: Array[String]): Unit = {

    val consumerSettings: ConsumerSettings[Long, MyRecord] = ???

    val kafkaSource = Consumer.committableSource(consumerSettings, Subscriptions.topics("my-topic"))
    val commitSink  = Flow[CommittableOffset].mapAsync(3)(_.commitScaladsl()).to(Sink.ignore)

    import svez.akka.stream.syntax.metadata._

    kafkaSource
      .map(msg ⇒ Ior.both(msg.committableOffset, msg.record.value()))
      .mapAsyncData(3)(storePrice)
      .divertMetaTo(commitSink)

  }

  def storePrice(record: MyRecord): Future[Unit] = ???
}