package com.example

import java.io.File

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.DockerComposeContainer

class MavenSimulation
    extends Simulation
{

  class GContainer(files: File*) extends DockerComposeContainer[GContainer](files:_*)

  val alpine: GContainer = new GContainer(new File("src/test/resources/docker-compose.yml"))
      .withExposedService("simpleWebServer_1", 8000)

  alpine.start()

  val url: String = s"http://${alpine.getServiceHost("simpleWebServer_1", 8000)}:${alpine.getServicePort("simpleWebServer_1", 8000)}/"

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
