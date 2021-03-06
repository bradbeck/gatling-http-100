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

import scala.concurrent.duration._
import scala.language.postfixOps

class MavenManualSimulation
    extends Simulation
{
  val nxrmUrl: String = s"http://localhost:8081"

  println(s"NXRM URL: $nxrmUrl")

  setUp(
    MavenCommon.scenarioBuilder.inject(
      rampUsers(4) during (15 seconds)
    )
  )
      .protocols(MavenCommon.baseProtocol(nxrmUrl))
      .assertions(global.successfulRequests.percent.gt(99))
}
