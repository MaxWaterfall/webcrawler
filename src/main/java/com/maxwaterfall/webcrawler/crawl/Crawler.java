package com.maxwaterfall.webcrawler.crawl;

import com.maxwaterfall.webcrawler.HttpClient;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Crawler {

  private final HttpClient httpClient;
  private final ExecutorService executorService;

  public Crawler(HttpClient httpClient) {
    this.httpClient = httpClient;
    this.executorService = Executors.newFixedThreadPool(32);
  }

  /**
   * Initiates a crawl, starting with the start uri.
   *
   * @return all pages that have been visited and all links on those pages.
   * @throws CrawlFailedException if the crawl fails.
   */
  public Set<VisitedPage> crawl(URI startUri) throws CrawlFailedException {
    return new Crawl(startUri, httpClient, executorService).start();
  }
}
