/*
 * Copyright (c) 2019-present Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.example

import java.util.concurrent.LinkedBlockingDeque

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.testcontainers.containers.{BindMode, GenericContainer, Network}

import scala.concurrent.duration._
import scala.language.postfixOps

class MavenDirectSimulation
    extends Simulation
{
  class GContainer(image: String)
      extends GenericContainer[GContainer](image)

  val nxrm: String = "nxrm"

  val nxrmPort: Integer = 8081

  val nxrmContainer: GContainer = new GContainer("sonatype/nexus3")
      .withNetworkAliases(nxrm)
      .withExposedPorts(nxrmPort)

  nxrmContainer.start()

  val nxrmUrl: String = s"http://${nxrmContainer.getContainerIpAddress}:${nxrmContainer.getMappedPort(nxrmPort)}"

  println(s"NXRM URL: $nxrmUrl")

  val nginxConfig: String = NginxConfig.generate(nxrmContainer.getMappedPort(nxrmPort)+1)

  println(s"nginx config: $nginxConfig")

  val baseProtocol: HttpProtocolBuilder = http
      .baseUrl(nxrmUrl)
      .inferHtmlResources()
      .disableAutoReferer
      .acceptHeader("*/*")
      .userAgentHeader("Apache-Maven/3.5.0 (Java 1.8.0_121; Mac OS X 10.12.4)")

  val releaseQueue: LinkedBlockingDeque[Map[String, String]] = new LinkedBlockingDeque[Map[String, String]]

  val scn: ScenarioBuilder = scenario("Content Populator").during(30 seconds) {
    exec(PublishReleases.release(releaseQueue))
  }

  setUp(
    scn.inject(
      rampUsers(4) during (15 seconds)
    )
  )
      .protocols(baseProtocol)
      .assertions(global.successfulRequests.percent.gt(99))

  after {
    nxrmContainer.stop()
  }
}
