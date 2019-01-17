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

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import org.testcontainers.containers.{BindMode, GenericContainer, Network}

class MavenSimulation
    extends Simulation
{

  class GContainer(image: String)
      extends GenericContainer[GContainer](image)

  val nxrm: String = "nxrm"

  val nxrmPort: Integer = 8081

  val nginx: String = "nginx_1"

  val nginxPort: Integer = 80

  val network: Network = Network.newNetwork()

  val nxrmContainer: GContainer = new GContainer("sonatype/nexus3")
      .withNetwork(network)
      .withNetworkAliases("nxrm")
      .withExposedPorts(nxrmPort)

  nxrmContainer.start()

    val nxrmUrl: String = s"http://${nxrmContainer.getContainerIpAddress}:${nxrmContainer.getMappedPort(nxrmPort)}/"

    println(s"NXRM URL: $nxrmUrl")

  val nginxContainer: GContainer = new GContainer("nginx:alpine")
      .withNetwork(network)
      .withExposedPorts(nginxPort)
      .withClasspathResourceMapping("proxy.nginx", "/etc/nginx/conf.d/default.conf", BindMode.READ_ONLY)

  nginxContainer.start()

  val nginxUrl: String = s"http://${nginxContainer.getContainerIpAddress}:${nginxContainer.getMappedPort(nginxPort)}/"

  println(s"NGINX URL: $nginxUrl")

  val baseProtocol: HttpProtocolBuilder = http
      .disableWarmUp
      .userAgentHeader("Gatling")
      .connectionHeader("Close")
      .acceptEncodingHeader("gzip,deflate")

  val scn: ScenarioBuilder = scenario("Simple GET")
      .exec(http("get hello")
          .get(nginxUrl)
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
    nxrmContainer.stop()
    nginxContainer.stop()
    network.close()
  }
}
