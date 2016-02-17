package org.zalando.spearheads.innkeeper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

import scala.collection.immutable.Seq
import scala.language.implicitConversions

/**
 * @author dpersa
 */
object AcceptanceSpecsHelper extends ScalaFutures {

  private val routesUri = "http://localhost:8080/routes"
  override implicit val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))
  implicit val system = ActorSystem("main-actor-system")
  implicit val materializer = ActorMaterializer()

  def routeUri(id: Long) = s"$routesUri/$id"
  def routeByNameUri(name: String) = s"$routesUri?name=$name"

  def entityString(response: HttpResponse): String = {
    response.entity.dataBytes
      .map(bs => bs.utf8String)
      .runFold("")(_ + _)
      .futureValue
  }

  def postSlashRoutes(routeType: String)(routeName: String, token: String = ""): HttpResponse = {
    val route =
      s"""{
          |  "name": "${routeName}",
          |  "description": "this is a route",
          |  "activate_at": "2015-10-10T10:10:10",
          |  "route": {
          |    "matcher": {
          |      "path_matcher": {
          |        "match": "/hello-*",
          |        "type": "${routeType}"
          |      }
          |    }
          |  }
          |}""".stripMargin

    val entity = HttpEntity(ContentType(MediaTypes.`application/json`), route)

    val headers = headersForToken(token)

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = routesUri,
      entity = entity,
      headers = headers)

    val futureResponse = Http().singleRequest(request)
    futureResponse.futureValue
  }

  def getSlashRoutes(token: String = ""): HttpResponse = {
    val futureResponse = Http().singleRequest(HttpRequest(
      uri = routesUri,
      headers = headersForToken(token)))
    futureResponse.futureValue
  }

  def getSlashRoutesByName(name: String, token: String): HttpResponse = {
    val futureResponse = Http().singleRequest(HttpRequest(
      uri = routeByNameUri(name),
      headers = headersForToken(token)))
    futureResponse.futureValue
  }

  def getSlashRoute(id: Long, token: String = ""): HttpResponse = {
    slashRoute(id, token)
  }

  def deleteSlashRoute(id: Long, token: String = ""): HttpResponse = {
    slashRoute(id, token, HttpMethods.DELETE)
  }

  private def slashRoute(id: Long, token: Option[String] = None, method: HttpMethod = HttpMethods.GET): HttpResponse = {
    val futureResponse = Http().singleRequest(
      HttpRequest(
        uri = routeUri(id),
        method = method,
        headers = headersForToken(token)
      )
    )
    futureResponse.futureValue
  }

  private def headersForToken(token: Option[String]): Seq[HttpHeader] = {
    val headers = token match {
      case Some(token) => Seq[HttpHeader](Authorization(OAuth2BearerToken(token)))
      case None        => Seq()
    }
    headers
  }

  implicit def stringToOption(string: String): Option[String] = {
    string match {
      case "" | null => None
      case str       => Option(str)
    }
  }
}

object AcceptanceSpecTokens {
  val READ_TOKEN = "token-user~1-employees-route.read"
  val WRITE_STRICT_TOKEN = "token-user~1-employees-route.write_strict"
  val WRITE_REGEX_TOKEN = "token-user~1-employees-route.write_regex"
  val INVALID_TOKEN = "invalid"
}