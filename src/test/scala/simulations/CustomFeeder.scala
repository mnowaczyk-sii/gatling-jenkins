package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.http.Predef._
import io.gatling.core.Predef._

import scala.util.Random

class CustomFeeder extends Simulation {


  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8888))

  var idNumbers = (11 to 20).iterator
  val rnd = new Random()
  val now = LocalDate.now()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def randomDate(statDate: LocalDate, random: Random): String = {
    statDate.minusDays(random.nextInt(30)).format(pattern)
  }

  val customFeeder = Iterator.continually(Map(
    "gameId" -> idNumbers.next(),
    "name" -> ("Game-" + randomString(5)),
    "releaseDate" -> randomDate(now, rnd),
    "reviewScore" -> rnd.nextInt(100),
    "category" -> ("Category-" + randomString(6)),
    "rating" -> ("Rating-" + randomString(4))
  )) // here we create random data

  def postNewGame() = {
    repeat(5) {
      feed(customFeeder)
        .exec(http("Post newvideo game")
          .post("videogames") // assertion on generated gameId
          .body(
          // we can pass body as String
          //                    StringBody(
          //                      "{" +
          //                        "\"id\": ${gameId}," +
          //                        "\"name\": \"${name}\"," +
          //                        "\"releaseDate\": \"${releaseDate}\"," +
          //                        "\"reviewScore\": ${reviewScore}," +
          //                        "\"category\": \"${category}\"," +
          //                        "\"rating\": \"${rating}\"" +
          //                        "}")
          // Or JSON template file
          ElFileBody("bodies/NewGameTemplate.json")
        ).asJson
          .check(status.in(200 to 210)))
        .pause(1)
    }
  }

  val scn = scenario("Post new games")
    .exec(postNewGame())

  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpConf)

}
