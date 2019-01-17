package com.example

import java.io.File

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.Wait

class MavenSimulation
    extends Simulation
{
  class GContainer(files: File*) extends DockerComposeContainer[GContainer](files:_*)

  val nxrm: String = "nxrm_1"

  val nxrmPort: Integer = 8081

  val alpine: GContainer = new GContainer(new File("src/test/resources/docker-compose.yml"))
      .withExposedService(nxrm, nxrmPort, Wait.forListeningPort())

  alpine.start()

  val url: String = s"http://${alpine.getServiceHost(nxrm, nxrmPort)}:${alpine.getServicePort(nxrm, nxrmPort)}/"

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
