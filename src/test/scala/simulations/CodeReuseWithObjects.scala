package simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

class CodeReuseWithObjects extends Simulation {

  // Configuration
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  // Define steps
  def getAllVideoGames() = {
    repeat(3) {
      exec(http("Get all video games")
        .get("videogames")
        .check(status.is(200)))
    }
  }

  def getSpecificVideoGame() = {
    exec(http("Get specific game")
      .get("videogames/1")
      .check(status.in(200 to 210)))
  }

  // Scenario
  val scn = scenario("Getting Videos from Video Game DB")
    .exec(getAllVideoGames()) // call it 3 times
    .pause(5) // then wait 5 seconds
    .exec(getSpecificVideoGame()) // call it 1 time
    .pause(3) // then wait 3 seconds
    .exec(getAllVideoGames()) // call it 3 times again

  // Load profile
  setUp(scn.inject(atOnceUsers(1)))
    .protocols(httpConf)
}
