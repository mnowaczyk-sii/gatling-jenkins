package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class FixedTimeLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
  //    .proxy(Proxy("localhost", 8888)) use only with Fidler

  def getAllVideoGames() = {
    exec(http("Get all video games")
      .get("videogames")
      .check(status.is(200)))
  }

  def getSpecificGame() = {
    exec(http("Get specific game")
      .get("videogames/1")
      .check(status.is(200)))
  }

  val scn = scenario("Basic Load Simulation")
    .forever() {
      exec(getAllVideoGames())
        .pause(5)
        .exec(getSpecificGame())
        .pause(5)
        .exec(getAllVideoGames())
    }


  setUp(
    scn.inject(
      nothingFor(5 seconds),
      atOnceUsers(5), // this seems to be optional
      rampUsers(50) during (30 seconds)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(1 minute)
}
