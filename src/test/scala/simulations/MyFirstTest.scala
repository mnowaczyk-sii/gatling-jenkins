package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MyFirstTest extends Simulation {

  // HTTP conf
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header(name="Accept", value="application/json")
    .proxy(Proxy("localhost", 8888 ))

  // Scenario def
  val scn = scenario("List out Games")
    .exec((http("Get All Games")).get("videogames"))

  // Load Profile
  setUp(
    scn.inject(atOnceUsers(users = 1)).protocols(httpConf)
  )
}