package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._


class RuntimeParameters extends Simulation {

  private def getProperty(propertyName: String, defaultValue: String)= {
    Option(System.getenv(propertyName)) // use options passed by flag
      .orElse(Option(System.getProperty(propertyName))) // or system properties
      .getOrElse(defaultValue) // finally try to apply default values
  }

  // propertyName is case sensitive - define default values here
  def userCount: Int = getProperty("USERS", "5").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt
  def testDuration: Int = getProperty("DURATION", "60").toInt


  before{
    println(s"users: ${userCount}")
    println(s"ramp duration: ${rampDuration}")
    println(s"test duration: ${testDuration}")
  }

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")


  def getAllGames() = {
    exec((http("Get All video games"))
      .get("videogames")
      .check(status.is(200)))
  }

  val scn = scenario("Get all games")
    .forever() {
      exec(getAllGames())
    }

  setUp(
    scn.inject(
      nothingFor(5),
      rampUsers(userCount) during (rampDuration seconds)
    ).protocols(httpConf)
  ).maxDuration(testDuration seconds)

}
