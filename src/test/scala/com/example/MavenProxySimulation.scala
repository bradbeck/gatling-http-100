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
import org.testcontainers.containers.{BindMode, Network}

import scala.concurrent.duration._
import scala.language.postfixOps

class MavenProxySimulation
    extends Simulation
{
  val nxrm: String = "nxrm"

  val nxrmPort: Integer = 8081

  val nginx: String = "nginx"

  val nginxPort: Integer = 80

  val network: Network = Network.newNetwork()

  val nxrmContainer: GContainer = new GContainer("sonatype/nexus3")
      .withNetwork(network)
      .withNetworkAliases(nxrm)
      .withExposedPorts(nxrmPort)

  nxrmContainer.start()

  val nginxConfig: String = NginxConfig.generate(nxrmContainer.getMappedPort(nxrmPort)+1)

  println(s"nginx config: $nginxConfig")

  val nginxContainer: GContainer = new GContainer("nginx:alpine")
      .withNetwork(network)
      .withNetworkAliases(nginx)
      .withExposedPorts(nginxPort)
      .withClasspathResourceMapping(nginxConfig, "/etc/nginx/conf.d/default.conf", BindMode.READ_ONLY)

  nginxContainer.start()

  val nginxUrl: String = s"http://${nginxContainer.getContainerIpAddress}:${nginxContainer.getMappedPort(nginxPort)}"

  println(s"NGINX URL: $nginxUrl")

  setUp(
    MavenCommon.scenarioBuilder.inject(
      rampUsers(4) during (15 seconds)
    )
  )
      .protocols(MavenCommon.baseProtocol(nginxUrl))
      .assertions(global.successfulRequests.percent.gt(99))

  after {
    nxrmContainer.stop()
    nginxContainer.stop()
    network.close()
  }
}
