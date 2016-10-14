name := "SprayInAndroid"
organization := "com.grburst"
version := "0.0.1"
scalaVersion := "2.11.8"

import android.Keys._
android.Plugin.androidBuild

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions in Compile ++= ("-target:jvm-1.7" ::
  "-encoding" :: "UTF-8" ::
  "-unchecked" ::
  "-deprecation" ::
  "-explaintypes" ::
  "-feature" ::
  "-language:_" ::
  "-Xlint:_" ::
  "-Ywarn-unused" ::
  Nil)

platformTarget := "android-16"
updateCheck in Android := {}
proguardCache in Android ++= Seq("org.scaloid")
packagingOptions in Android := PackagingOptions(
  excludes = Seq(
    "META-INF/LICENSE.txt",
    "META-INF/NOTICE.txt",
    "META-INF/LICENSE",
    "META-INF/NOTICE",
    "META-INF/DEPENDENCIES",
    "rootdoc.txt"),
  merges = Seq("reference.conf"))
proguardOptions in Android ++= Seq("@project/proguard.cfg")
// proguardOptions in Android ++= Seq("-dontwarn **", "-dontnote **")

// proguardScala := true
// useProguard := false
dexMulti := true
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe" % "config" % "1.2.1",
  "io.spray" %% "spray-client" % "1.3.4",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.scaloid" %% "scaloid" % "4.2",
  aar("org.macroid" %% "macroid" % "2.0.0-M5"),
  aar("org.macroid" %% "macroid-viewable" % "2.0.0-M5"))

run <<= run in Android
install <<= install in Android

initialCommands in console := """
import com.grburst.androidspray._

import scala.concurrent.{Future, Await, ExecutionContext }
import scala.concurrent.duration._
import scala.util.{ Try, Success, Failure }

import spray.client.pipelining._
import spray.http.{ FormData, HttpCookie, HttpRequest, HttpResponse }
import spray.http.Uri
import spray.http.HttpHeaders.{ Cookie, `Set-Cookie` }
import spray.httpx.encoding.Gzip

import akka.actor.ActorSystem
"""
