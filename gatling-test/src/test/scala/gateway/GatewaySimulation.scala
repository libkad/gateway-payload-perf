package gateway

import scala.concurrent.duration._
import scala.io.Source
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class GatewaySimulation extends Simulation {

  val payload = System.getProperty("payload")

  val httpProtocol = http
    .baseURL("http://localhost")
    .inferHtmlResources()
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .contentTypeHeader("application/json")
    .userAgentHeader("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")

  object TestGatewayWithPayload {
    val call = exec(http("gateway-" + payload)
      .post("http://localhost:9090/post")
      .body(StringBody(Source.fromFile("payload/" + payload + ".json").getLines.mkString))
      .check(status.is(200)))
  }
  val TestGatewayWithPayloadScenario = scenario("gateway-" + payload).forever(pace(1 seconds).exec(TestGatewayWithPayload.call))

  object TestGatewayNoPayload {
    val call = exec(http("gateway-no-payload")
      .get("http://localhost:9090/greeting")
      .check(status.is(200)))
  }
  val TestGatewayNoPayloadScenario = scenario("gateway-no-payload").forever(pace(1 seconds).exec(TestGatewayNoPayload.call))

  object TestDirectWithPayload {
    val call = exec(http("direct-" + payload)
      .post("http://localhost:8080/post")
      .body(StringBody(Source.fromFile("payload/" + payload + ".json").getLines.mkString))
      .check(status.is(200)))
  }
  val TestDirectWithPayloadScenario = scenario("direct-" + payload).forever(pace(1 seconds).exec(TestDirectWithPayload.call))

  object TestDirectNoPayload {
    val call = exec(http("direct-no-payload")
      .get("http://localhost:8080/greeting")
      .check(status.is(200)))
  }
  val TestDirectNoPayloadScenario = scenario("direct-no-payload").forever(pace(1 seconds).exec(TestDirectNoPayload.call))

  val nUsers = Integer.parseInt(System.getProperty("nusers"))
  val nRampUsersPerSecond = 63

  setUp(
    TestGatewayWithPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)),
    TestGatewayNoPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)),
    TestDirectNoPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)),
    TestDirectWithPayloadScenario.inject(rampUsers(nUsers) over (nUsers / nRampUsersPerSecond seconds)))

    .maxDuration(5 minutes).protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.is(100)
    )
}
