package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class ChallengeAssignment extends Simulation {

  private def getParameters(paramName: String, defaultVal: String) = {
    Option(System.getenv(paramName))
      .orElse(Option(System.getProperty(paramName)))
      .getOrElse(defaultVal)
  }

  def userCount: Int = getParameters("USERS", "5").toInt

  def rampDuration: Int = getParameters("RAMP_DURATION", "10").toInt

  def testDuration: Int = getParameters("DURATION", "60").toInt


  before {
    println(s"users: ${userCount}")
    println(s"ramp duration: ${rampDuration}")
    println(s"test duration: ${testDuration}")
  }


  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
    .proxy(Proxy("localhost", 8888)) // use only with Fidler


  var idNumbers = (11 to 50000).iterator // how to predict how many ids will be required?
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


  def getAllGames() = {
    exec((http("Get All video games"))
      .get("videogames")
      .check(status.is(200)))
  }

  def createNewGame() = {
    feed(customFeeder)
      .exec(http("Post newvideo game")
        .post("videogames") // assertion on generated gameId
        .body(
        ElFileBody("bodies/NewGameTemplate.json")
      ).asJson
        .check(status.in(200 to 210)))
  }

  def wholeScenario() = {
    feed(customFeeder)
      .exec(http("Post new video game get and delete it")
        .post("videogames") // assertion on generated gameId
        .body(
        ElFileBody("bodies/NewGameTemplate.json")
      ).asJson
        .check(status.in(200 to 210)))
      .exec(http("Get specific video game")
        .get("videogames/${gameId}") // assertion on generated gameId
        .check(status.is(200))
      ).exec(http("Delete specific video game")
      .delete("videogames/${gameId}") // assertion on generated gameId
      .check(status.is(200)))

  }

  def getSingleGame() = {
    exec(http("Get specific video game")
      .get("videogames/${gameId}") // assertion on generated gameId
      .check(status.is(200)))
  }

  def deleteGame() = {
    exec(http("Get specific video game")
      .delete("videogames/${gameId}") // assertion on generated gameId
      .check(status.is(200)))
  }


  val scn = scenario("Post and delete")
    .forever() {
      //      feed(customFeeder) feeder can be moved to scenario as well
      exec(createNewGame())
        .pause(5)
        .exec(getSingleGame())
        .pause(5)
        .exec(deleteGame())
        .pause(5)
    }

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      atOnceUsers(5), // this seems to be optional
      rampUsers(userCount) during (rampDuration seconds)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(testDuration seconds)
    .assertions(
      global.responseTime.max.lt(3000), // response time for all requests should b lower than 3s
      global.successfulRequests.percent.gt(99) // 99 requests needs to pass
    )
}
