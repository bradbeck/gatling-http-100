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
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.body.StringBody
import io.gatling.http.Predef._
import org.apache.commons.codec.digest.DigestUtils

object MavenClient
{
  val expect: Map[String, String] = Map(
    "Cache-control" -> "no-cache",
    "Cache-store" -> "no-store",
    "Expect" -> "100-continue",
    "Expires" -> "0",
    "Pragma" -> "no-cache")

  def publishRelease(jarSize: Int): ActionBuilder = {
    val bytes = new Array[Byte](jarSize)
    scala.util.Random.nextBytes(bytes)
    val jarSha1 = DigestUtils.sha1Hex(bytes)
    val jarMd5 = DigestUtils.md5Hex(bytes)

    http("PUT release jar")
        .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.jar")
        .headers(expect)
        .body(ByteArrayBody(bytes))
        .basicAuth("${username}", "${password}")
        .check(status.is(201))
        .resources(
          http("PUT release jar.sha1")
              .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.jar.sha1")
              .headers(expect)
              .body(StringBody(jarSha1))
              .basicAuth("${username}", "${password}")
              .check(status.is(201)),
          http("PUT release jar.md5")
              .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.jar.md5")
              .headers(expect)
              .body(StringBody(jarMd5))
              .basicAuth("${username}", "${password}")
              .check(status.is(201)),
          http("PUT release pom")
              .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.pom")
              .headers(expect)
              .body(ElFileBody("com/example/sample-pom-release.xml"))
              .basicAuth("${username}", "${password}")
              .check(status.is(201)),
          http("PUT release pom.sha1")
              .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.pom.sha1")
              .headers(expect)
              .body(StringBody("55a170f9498ed9aa8061fba963fe282aeb14aad7"))
              .basicAuth("${username}", "${password}")
              .check(status.is(201)),
          http("PUT release pom.md5")
              .put("${repositoryPath}/${groupPath}/${artifact}/${version}.${index}/${artifact}-${version}.${index}.pom.md5")
              .headers(expect)
              .body(StringBody("d92a34e11573c9c74cdf0ca457333296"))
              .basicAuth("${username}", "${password}")
              .check(status.is(201))
        )
  }
}
