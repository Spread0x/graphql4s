//import StructTypeDerivation._

import defaults.Defaults._
import monix.reactive.Observable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends EncoderTypeDerivation with StructTypeDerivation {

  case class CurrentLocation(coord: String)

  case class Location(x: Int, y: Int, sum: Int => Int = x => x * 2)

  sealed trait Car {
    def model: String
  }

  case class Fiat(model: String, year: Int) extends Car

  case class Query(car: () => Car, name: String, current: CurrentLocation, height: Int => Location, list: List[Location])

  def main(args: Array[String]): Unit = {

    val query: Map[String, String] = Map(
      "query" ->
        """|{
           |  car()
           |  name
           |  current
           | fragment template{
           |  height(50){
           |  }
           | }
           |}
           |""".stripMargin,
      "variables" ->
        """
          |{
          | "var1": 123
          |}
          |""".stripMargin
    )

    import monix.execution.Scheduler.Implicits.global

    val instance: Query = Query(
      () => Fiat("strada", 2020),
      "joaquin",
      CurrentLocation("mdeo"),
      x => Location(x, x + 1),
      List(Location(1, 2), Location(3, 4))
    )


    val intPromise: (Map[String, String], Option[String]) => Observable[String] = GraphQL.buildInterpreter(instance)

    val start = System.currentTimeMillis()
    val r: Observable[String] = intPromise(query, None)

    r

    println(Await.result(r.foldLeftL("")(_ + _).executeAsync.runToFuture, Duration.Inf))
    println((System.currentTimeMillis() - start) * 0.001)

  }
}

