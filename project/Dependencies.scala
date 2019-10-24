import sbt._

object Dependencies {
  lazy val benji = "com.zengularity" %% "benji-s3" % "2.0.5"

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.0.0"

  lazy val kafkaStream = "com.typesafe.akka" %% "akka-stream-kafka" % "1.1.0"

  lazy val sshd = "org.apache.sshd" % "sshd-sftp" % "2.3.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

  lazy val slf4j = "org.slf4j" % "slf4j-jdk14" % "1.7.28"
}
