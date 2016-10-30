package eu.svez.akka.stream.cats

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

trait Spec extends FlatSpecLike with Matchers with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}