# fs2-functork

[![build](https://github.com/lhns/fs2-functork/actions/workflows/build.yml/badge.svg)](https://github.com/lhns/fs2-functork/actions/workflows/build.yml)
[![Release Notes](https://img.shields.io/github/release/lhns/fs2-functork.svg?maxAge=3600)](https://github.com/lhns/fs2-functork/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lhns/fs2-functork_2.13)](https://search.maven.org/artifact/de.lhns/fs2-functork_2.13)
[![Apache License 2.0](https://img.shields.io/github/license/lhns/fs2-functork.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

## Usage

### build.sbt

```sbt
libraryDependencies += "de.lhns" %% "fs2-functork" % "0.2.5"
libraryDependencies += "de.lhns" %% "fs2-functork-doobie" % "0.2.5"
```

## Example

```scala
import cats.effect.Async
import cats.tagless._
import de.lhns.fs2.functork._
import de.lhns.fs2.functork.doobie.transactK
import doobie._
import doobie.implicits._
import fs2.Stream

trait Repo[F[_]] {
  def get(id: Long): F[Option[String]]

  def stream: Stream[F, (Long, String)]
}

object Repo {
  implicit val functorK: FunctorK[Repo] = Derive.functorK

  val impl: Repo[ConnectionIO] = new Repo[ConnectionIO] {
    override def get(id: Long): F[Option[String]] =
      sql"select value from table where id = $id"
        .query[String]
        .option

    override def stream: Stream[F, (Long, String)] =
      sql"select id, value from table"
        .query[(Long, String)]
        .stream
  }
}

object Main {
  def repo[F[_] : Async](xa: Transactor[F]): Repo[F] = {
    val transact = transactK(xa)
    Repo.impl.mapK(transact)
  }
}
```

## License

This project uses the Apache 2.0 License. See the file called LICENSE.
