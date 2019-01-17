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
