package com.maxwaterfall.webcrawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class HttpClient {

  private final java.net.http.HttpClient httpClient;

  public HttpClient() {
    this.httpClient = java.net.http.HttpClient.newBuilder().build();
  }

  /**
   * Makes a HTTP GET request to the given uri.
   *
   * @param uri to GET.
   * @return the HttpResponse.
   */
  public HttpResponse<InputStream> get(URI uri) throws IOException, InterruptedException {
    var request = HttpRequest.newBuilder().GET().uri(uri).build();
    return httpClient.send(request, BodyHandlers.ofInputStream());
  }
}
