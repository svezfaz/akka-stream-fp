package eu.svez.akka.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

trait StageSpec extends FlatSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}