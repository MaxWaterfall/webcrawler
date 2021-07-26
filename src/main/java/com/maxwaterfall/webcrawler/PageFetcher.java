package com.maxwaterfall.webcrawler;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class PageFetcher {

  private final WebClient webClient;

  public PageFetcher(WebClient.Builder builder) {
    this.webClient = builder.build();
  }

  /**
   * Can return null.
   * @param uri
   * @return
   */
  public String fetch(URI uri) {

    try {
      return webClient.get()
          .uri(uri)
          .retrieve()
          //.onStatus(HttpStatus::is3xxRedirection, cr -> Mono.error(new Exception("Redirections are not allowed")))
          .bodyToMono(String.class)
          .block();
    } catch (Exception e) {
      return "";
    }
  }

}
