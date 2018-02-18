package svez.akka.stream.stages

import akka.NotUsed
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContext, Future}

object tuple {

  def map1[I1, I2, O](f: I1 ⇒ O): Flow[(I1, I2), (O, I2), NotUsed] =
    Flow[(I1, I2)].map { case (i1, i2) ⇒ (f(i1), i2) }

  def mapAsync1[I1, I2, O](parallelism: Int)(f: I1 ⇒ Future[O])(implicit ec: ExecutionContext): Flow[(I1, I2), (O, I2), NotUsed] =
    Flow[(I1, I2)].mapAsync(parallelism){ case (i1, i2) ⇒ f(i1).map(_ → i2) }

  def map2[I1, I2, O](f: I2 ⇒ O): Flow[(I1, I2), (I1, O), NotUsed] =
    Flow[(I1, I2)].map { case (i1, i2) ⇒ (i1, f(i2)) }

  def mapAsync2[I1, I2, O](parallelism: Int)(f: I2 ⇒ Future[O])(implicit ec: ExecutionContext): Flow[(I1, I2), (I1, O), NotUsed] =
    Flow[(I1, I2)].mapAsync(parallelism){ case (i1, i2) ⇒ f(i2).map(i1 → _) }
}
