package modux.graphql.defaults

import io.circe.{Json, JsonObject}
import modux.graphql.model.{Accessor, Link}

import scala.concurrent.{ExecutionContext, Future}

case object LinkImpl extends Link {

  override def build(queryParams: Map[String, String], body: Option[String])(implicit executionContext: ExecutionContext): Future[Accessor] = Future {

    val parsedObj: Option[JsonObject] = body.flatMap(x => io.circe.parser.parse(x).toOption).flatMap(_.asObject)
    val vs1: Map[String, Json] = queryParams.get("variables").flatMap(x => io.circe.parser.parse(x).toOption).flatMap(_.asObject).fold(Map.empty[String, io.circe.Json]) { obj => obj.toMap }
    val variableStore: Map[String, io.circe.Json] = parsedObj.map(obj => obj.toMap ++ vs1).getOrElse(vs1)
    val maybeQuery: Option[String] = parsedObj.flatMap(x => x("query")).flatMap(_.asString)

    new Accessor {
      override def apply(key: String): Option[String] = variableStore.get(key).map(_.toString())

      override def query: Option[String] = queryParams.get("query").orElse(maybeQuery)
    }
  }
}
