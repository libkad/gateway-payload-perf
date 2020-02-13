package gateway

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.io.Source

class DirectPayloadSimulation extends Simulation {

  val payload = System.getProperty("payload")

  val httpProtocol = http
    .baseURL("https://localhost")
    .inferHtmlResources()
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .contentTypeHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")

  object TestDirectWithPayload {
    val call = exec(http("direct-" + payload)
      .post("https://localhost:7443/post")
      .body(StringBody(Source.fromFile("payload/" + payload + ".json").getLines.mkString))
      .check(status.is(200)))
  }
  val TestDirectWithPayloadScenario = scenario("direct-" + payload).forever(pace(1 seconds).exec(TestDirectWithPayload.call))

  val nUsers = Integer.parseInt(System.getProperty("nusers"))
  val nRampUsersPerSecond = 5

  setUp(
    TestDirectWithPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)))

    .maxDuration(5 minutes).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100)
    )
}
