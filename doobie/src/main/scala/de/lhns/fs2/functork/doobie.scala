package de.lhns.fs2.functork

import cats.effect.MonadCancelThrow
import _root_.doobie.{ConnectionIO, Transactor}

object doobie {
  def transactK[F[_] : MonadCancelThrow](xa: Transactor[F]): StreamFunctionK[ConnectionIO, F] =
    new StreamFunctionK[ConnectionIO, F] {
      override def stream[A](stream: fs2.Stream[ConnectionIO, A]): fs2.Stream[F, A] =
        xa.transP.apply(stream)

      override def apply[A](fa: ConnectionIO[A]): F[A] =
        xa.trans.apply(fa)
    }
}
