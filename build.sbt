import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.bp2s"
ThisBuild / organizationName := "bp2s"

lazy val root = (project in file("."))
  .settings(
    name := "k8-sftp-push",
    libraryDependencies ++= Seq(
      catsEffect,
      sshd,
      scalaTest % Test
    )
  )

fork in run := true