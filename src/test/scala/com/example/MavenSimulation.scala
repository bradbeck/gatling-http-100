package com.example

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer

class MavenSimulation
    extends Simulation
{

  class GContainer(image: String)
      extends GenericContainer[GContainer](image)

  val alpine: GContainer = new GContainer("crccheck/hello-world").withExposedPorts(8000)

  alpine.start()

  val url: String = s"http://${alpine.getContainerIpAddress}:${alpine.getMappedPort(8000)}/"

  val baseProtocol = http
      .disableWarmUp
      .userAgentHeader("Gatling")
      .connectionHeader("Close")
      .acceptEncodingHeader("gzip,deflate")

  val scn = scenario("Simple GET")
      .exec(http("get hello")
          .get(url)
          .check(
            status.is(200)
          )
      )

  setUp(
    scn.inject(
      atOnceUsers(10)
    )
  )
      .assertions(
        details("get hello").successfulRequests.percent.gt(99)
      )
      .protocols(baseProtocol)

  after {
    alpine.stop()
  }
}
