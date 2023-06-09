package de.lhns.fs2.functork

import cats.tagless.FunctorK
import cats.~>
import fs2.Stream

object StreamFunctorK {
  implicit def streamFunctorK[A]: FunctorK[({type H[F[_]] = Stream[F, A]})#H] =
    new FunctorK[({type H[F[_]] = Stream[F, A]})#H] {
      def mapK[F[_], G[_]](af: Stream[F, A])(fk: F ~> G): Stream[G, A] = fk match {
        case streamFk: StreamFunctionK[F, G] => streamFk.stream(af)
        case _ => throw new IllegalArgumentException("fk is not a StreamFunctionK")
      }
    }
}
