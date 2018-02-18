package svez.akka.stream.stages

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

trait StageSpec extends FlatSpecLike with Matchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  implicit val ec     = system.dispatcher

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}