package gateway

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.io.Source

class DirectSimulation extends Simulation {

  val payload = System.getProperty("payload")

  val httpProtocol = http
    .baseURL("https://localhost")
    .inferHtmlResources()
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .contentTypeHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")

  object TestDirectNoPayload {
    val call = exec(http("direct-no-payload")
      .get("https://localhost:7443/greeting")
      .check(status.is(200)))
  }
  val TestDirectNoPayloadScenario = scenario("direct-no-payload").forever(pace(1 seconds).exec(TestDirectNoPayload.call))

  val nUsers = Integer.parseInt(System.getProperty("nusers"))
  val nRampUsersPerSecond = 5

  setUp(
    TestDirectNoPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)))

    .maxDuration(5 minutes).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100)
    )
}
