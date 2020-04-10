package simulations

import io.gatling.http.Predef._
import io.gatling.core.Predef._

import scala.concurrent.duration.DurationInt

class AddPauseTime extends Simulation {
  // Configuration
  val httpConf = http.baseUrl("http://localhost:8080/app/").header("Accept", "application/json")

  // Scenario
  val scn = scenario("Getting Videos from Video Game DB")

    .exec(http("Get all video games")
      .get("videogames"))
    .pause(5) // duration in seconds

    .exec(http("Get specific game")
    .get("videogames/1"))
    .pause(1, 20) // pause can last for random period of time in defined time frame

    .exec(http("Get all videos again")
    .get("videogames"))
    .pause(duration = 3000.milliseconds)

  // Load profile
  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpConf)
}
