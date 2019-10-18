import sbt._

object Dependencies {
  lazy val sshj = "com.hierynomus" % "sshj" % "0.27.0"

  lazy val sshd = "org.apache.sshd" % "sshd-sftp" % "2.3.0"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
}
