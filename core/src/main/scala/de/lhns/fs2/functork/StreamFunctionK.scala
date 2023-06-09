package de.lhns.fs2.functork

import cats.arrow.FunctionK
import cats.~>
import fs2.Stream

trait StreamFunctionK[F[_], G[_]] extends FunctionK[F, G] {
  def stream[A](stream: Stream[F, A]): Stream[G, A]
}

object StreamFunctionK {
  def from[F[_], G[_]](fk: F ~> G): StreamFunctionK[F, G] = fk match {
    case fk: StreamFunctionK[F, G] => fk
    case fk                        =>
      new StreamFunctionK[F, G] {
        override def stream[A](stream: Stream[F, A]): Stream[G, A] = stream.translate(fk)

        override def apply[A](fa: F[A]): G[A] = fk.apply(fa)
      }
  }
}
