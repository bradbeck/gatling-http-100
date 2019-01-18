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
package com.example;

import com.mashape.unirest.http.Unirest;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import static org.junit.Assert.assertEquals;

public class SimpleGet {
  @ClassRule
  public static final GenericContainer alpine = new GenericContainer("alpine:3.2")
      .withExposedPorts(80)
      .withCommand("/bin/sh", "-c", "while true; do echo \"HTTP/1.1 200 OK\n\nHello World!\" | nc -l -p 80; done");

  @Test
  public void testEmpty() throws Exception {
    String address = "http://"
        + alpine.getContainerIpAddress()
        + ":" + alpine.getMappedPort(80);
    String response = Unirest.get(address).asString().getBody();

    assertEquals("Hello World!\n", response);
  }
}
