package com.example

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.testcontainers.containers.GenericContainer

import scala.concurrent.duration._

class MavenSimulation extends Simulation
{
  class GContainer(image: String) extends GenericContainer[GContainer] {
    setDockerImageName(image)
  }

  val alpine: GContainer = new GContainer("crccheck/hello-world").withExposedPorts(8000)
  alpine.start()
  val url: String = "http://" + alpine.getContainerIpAddress + ":" + alpine.getMappedPort(8000)
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

  setUp (
    scn.inject(
      constantConcurrentUsers(4) during (30 seconds)
    )
  ).protocols(baseProtocol)

  after {
    alpine.stop()
  }
}
