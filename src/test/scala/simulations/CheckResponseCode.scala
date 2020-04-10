package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class CheckResponseCode extends Simulation {

  // Configuration
  val httpConf = http.baseUrl("http://localhost:8080/app/").header("Accept", "application/json")

  // Scenario
  val scn = scenario("Getting Videos from Video Game DB")

    .exec(http("Get all video games")
      .get("videogames")
      .check(status.is(200))) // check status by exact match
    .pause(5)

    .exec(http("Get specific game")
      .get("videogames/1")
      .check(status.in(200 to 210))) // check by using range
    .pause(1, 20)

    .exec(http("Get all videos again")
      .get("videogames")
      .check(status.not(404), status.not(500))) // negative assertions
    .pause(duration = 3000.milliseconds)

  // Load profile
  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpConf)


}
