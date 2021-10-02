package com.maxwaterfall.webcrawler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

  @DisplayName("Correct output for small website")
  @Test
  void smallWebsiteTest() throws Exception {
    WireMockServer wireMockServer = new WireMockServer();
    wireMockServer.start();

    stubFor(get(urlEqualTo("/page1.html")).willReturn(aResponse().withBodyFile("page1.html")));

    stubFor(get(urlEqualTo("/page2")).willReturn(aResponse().withBodyFile("page2.html")));

    stubFor(get(urlEqualTo("/page3.html")).willReturn(aResponse().withBodyFile("page3.html")));

    stubFor(get(urlEqualTo("/page4.html")).willReturn(aResponse().withBodyFile("page4.html")));

    // 5 redirects to 6.
    stubFor(
        get(urlEqualTo("/page5.html"))
            .willReturn(
                aResponse()
                    .withStatus(301)
                    .withHeader("Location", "http://localhost:8080/page6.html")));

    stubFor(get(urlEqualTo("/page6.html")).willReturn(aResponse().withBodyFile("page6.html")));

    stubFor(get(urlEqualTo("/page7")).willReturn(aResponse().withBodyFile("page7.html")));

    stubFor(
        get(urlEqualTo("/page8/inner-page/inner-inner-page"))
            .willReturn(aResponse().withBodyFile("page8.html")));

    var outputBuffer = new ByteArrayOutputStream();
    var outputPrinter = new PrintStream(outputBuffer);
    var clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneOffset.UTC);

    // errorStream is null as this test does not expect any errors.
    int output = new Cli(outputPrinter, null, clock).interpret("http://localhost:8080/page1.html");

    Assertions.assertEquals(0, output);

    outputPrinter.flush();

    var actual = outputBuffer.toString();
    var expected =
        Files.readString(Paths.get(getClass().getResource("/integration-expected.txt").toURI()));

    wireMockServer.stop();

    Assertions.assertEquals(expected, actual);
  }
}
