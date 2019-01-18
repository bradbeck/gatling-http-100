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

import java.io.{File, PrintWriter}
import java.nio.file.{Path, Paths}

object NginxConfig
{
  def generate(port: Int): String = {
    val testClassesDir: Path = Paths.get(System.getProperty("user.dir"), "target", "test-classes")
    val file = File.createTempFile("proxy-", ".nginx", testClassesDir.toFile)

    val config =
      raw"""upstream backend_hosts {
        |  server nxrm:8081;
        |}
        |
        |server {
        |  listen 80;
        |
        |  location / {
        |    proxy_pass http://backend_hosts/;
        |    proxy_set_header Host $$host:$port;
        |    proxy_set_header X-Real-IP $$remote_addr;
        |    proxy_set_header X-Forwarded-For $$proxy_add_x_forwarded_for;
        |  }
        |}
      """.stripMargin

    val writer = new PrintWriter(file)
    writer.write(config)
    writer.close()

    println(s"nginx.config\n $config")

    file.getName
  }
}
