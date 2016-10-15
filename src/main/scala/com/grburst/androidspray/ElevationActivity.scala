package com.grburst.androidspray

import android.app.Activity
import android.os.{ Bundle, AsyncTask }
import android.widget._

import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import scala.concurrent.{ Future, ExecutionContext }
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{ JsonFormat, DefaultJsonProtocol }
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._

import org.scaloid.common._

import macroid._
import macroid.FullDsl._
import macroid.Contexts
import macroid.contrib.Layouts._

case class Elevation(elevation: Double, location: Location)
case class Location(lat: Double, lng: Double)
case class GoogleApiResult[T](results: List[T], status: String)

object ElevationJsonProtocol extends DefaultJsonProtocol {
  implicit val locationFormat = jsonFormat(Location, "lat", "lng")
  implicit val elevationFormat = jsonFormat(Elevation, "elevation", "location")
  implicit def googleApiResultFormat[T: JsonFormat] = jsonFormat(GoogleApiResult.apply[T], "results", "status")
}

case class GoogleElevation(implicit val system: ActorSystem, implicit val ec: ExecutionContext) {
  val log = Logging(system, getClass)

  def getElevation(lat: Double, long: Double): Future[String] = {

    log.info("Requesting the elevation of Mt. Everest from Googles Elevation API...")

    import ElevationJsonProtocol._
    import SprayJsonSupport._
    val pipeline = sendReceive ~> unmarshal[GoogleApiResult[Elevation]]

    val responseFuture = pipeline {
      Get(s"http://maps.googleapis.com/maps/api/elevation/json?locations=$lat,$long&sensor=false")
    }

    responseFuture map (_.results.head.elevation.toString)

  }

  def getMtEverestElevation() = {
    getElevation(27.988056, 86.925278)
  }

}

class ElevationActivity extends SActivity with Contexts[Activity] {

  implicit val ec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
  implicit val system = ActorSystem("elevation-actors")

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    var eleRes = slot[TextView]

    setContentView {
      (l[VerticalLinearLayout](
        w[TextView] <~ text("Hello akka, android and spray"),
        w[Button] <~ text("Get Mt. Everest elevation") <~ On.click(eleRes <~ (GoogleElevation().getMtEverestElevation map (text))),
        w[TextView] <~ wire(eleRes)
      )).get

    }

  }

  override def onDestroy = {
    IO(Http).ask(Http.CloseAll)(3.second).await
    system.shutdown()
    super.onDestroy()
  }

}
