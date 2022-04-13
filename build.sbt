val version = "0.1.0-SNAPSHOT"

val scalaVer = "2.13.5"

val zioVersion = "1.0.13"

lazy val compileDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion
) map (_ % Compile)

lazy val root = (project in file("."))
  .settings(
    name := "zio-play",
    scalaVersion := scalaVer,
    libraryDependencies ++= compileDependencies
  )
