lazy val scalaVersions = Seq("3.3.7", "2.13.17", "2.12.20")

ThisBuild / scalaVersion := scalaVersions.head
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / organization := "de.lhns"
ThisBuild / version := (core.projectRefs.head / version).value
name := (core.projectRefs.head / name).value

val V = new {
  val betterMonadicFor = "0.3.1"
  val catsTagless = "0.16.3"
  val doobie = "1.0.0-RC10"
  val fs2 = "3.12.2"
  val logbackClassic = "1.5.18"
  val munit = "1.2.1"
}

lazy val commonSettings: SettingsDefinition = Def.settings(
  version := {
    val Tag = "refs/tags/v?([0-9]+(?:\\.[0-9]+)+(?:[+-].*)?)".r
    sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
      .getOrElse("0.0.1-SNAPSHOT")
  },

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),

  homepage := scmInfo.value.map(_.browseUrl),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lhns/fs2-functork"),
      "scm:git@github.com:lhns/fs2-functork.git"
    )
  ),
  developers := List(
    Developer(id = "lhns", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/lhns/"))
  ),

  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % V.logbackClassic % Test,
    "org.scalameta" %%% "munit" % V.munit % Test,
  ),

  testFrameworks += new TestFramework("munit.Framework"),

  libraryDependencies ++= virtualAxes.?.value.getOrElse(Seq.empty).collectFirst {
    case VirtualAxis.ScalaVersionAxis(version, _) if version.startsWith("2.") =>
      compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
  },

  Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),

  Compile / doc / sources := Seq.empty,

  publishMavenStyle := true,

  publishTo := sonatypePublishToBundle.value,

  sonatypeCredentialHost := Sonatype.sonatypeCentralHost,

  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    sonatypeCredentialHost.value,
    username,
    password
  )).toList,
)

lazy val root: Project = project.in(file("."))
    .settings(commonSettings)
    .settings(
      publishArtifact := false,
      publish / skip := true
    )
    .aggregate(core.projectRefs: _*)
    .aggregate(doobie.projectRefs: _*)

lazy val core = projectMatrix.in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "fs2-functork",

    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % V.fs2,
      "org.typelevel" %%% "cats-tagless-core" % V.catsTagless,
    ),
  )
  .jvmPlatform(scalaVersions)
  .jsPlatform(scalaVersions)

lazy val doobie = projectMatrix.in(file("doobie"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(
    name := "fs2-functork-doobie",

    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % V.doobie,
    ),
  )
  .jvmPlatform(scalaVersions)
