package com.maxwaterfall.webcrawler.crawl;

import com.maxwaterfall.webcrawler.HttpClient;
import com.maxwaterfall.webcrawler.TaskExecutor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.yaml.snakeyaml.util.UriEncoder;

/**
 * Crawls web pages. NOT safe for concurrent use. Can only be used once.
 *
 * <p>To start the crawl, call start().
 */
class Crawl {

  private final TaskExecutor taskExecutor;
  private final HttpClient httpClient;
  private final URI start;
  /** A set of normalized uris that are scheduled to be visited. */
  private final Set<String> scheduledVisits;

  private final Set<VisitedPage> visitedPages;
  private boolean crawled;

  Crawl(URI start, HttpClient httpClient, ExecutorService executorService) {
    this.httpClient = httpClient;
    this.taskExecutor = new TaskExecutor(executorService);
    this.start = start;
    this.scheduledVisits = ConcurrentHashMap.newKeySet();
    this.visitedPages = ConcurrentHashMap.newKeySet();
  }

  /**
   * Initiates a crawl, starting with the start uri.
   *
   * @return all pages that have been visited and all links on those pages.
   * @throws CrawlFailedException if the crawl fails.
   */
  Set<VisitedPage> start() throws CrawlFailedException {
    if (crawled) {
      throw new IllegalStateException("A crawl must only be used once");
    }
    crawled = true;
    scheduleVisit(start);

    try {
      taskExecutor.waitForCompletion();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CrawlFailedException(e);
    }

    return this.visitedPages;
  }

  /**
   * Visits a page.
   *
   * <p>Extracts all links ('href' attribute from '<a>' tags) from the visited page. Schedules a
   * visit to all links on the page that are on the start domain and haven't already been visited.
   *
   * @param pageUri the uri of the page to visit.
   */
  private void visit(URI pageUri) {
    try {
      var response = httpClient.get(pageUri);

      if (response.statusCode() >= 300 && response.statusCode() < 400) {
        var redirectUri = URI.create(response.headers().firstValue("Location").orElseThrow());
        // Consider this uri visited and schedule a visit to the redirect uri.
        visitedPages.add(new VisitedPage(normalizeUri(pageUri), new ArrayList<>()));
        scheduleVisit(redirectUri);
        return;
      }

      var html = response.body();
      var hrefs = extractHrefFromATags(html, pageUri);
      var pageUris = hrefToUri(pageUri, hrefs);

      // Schedule a visit to each uri on the page.
      pageUris.forEach(this::scheduleVisit);

      // This uri is considered visited now.
      visitedPages.add(
          new VisitedPage(
              normalizeUri(pageUri),
              pageUris.stream().map(this::normalizeUri).collect(Collectors.toList())));
    } catch (Exception e) {
      System.err.println("An error occurred whilst visiting " + pageUri);
      e.printStackTrace();
    }
  }

  /**
   * Schedules a visit to the given uri.
   *
   * <p>A visit is only scheduled if the uri has the same full domain as the start domain and has
   * not already been visited.
   *
   * <p>Synchronized so each uri is only visited once.
   */
  private void scheduleVisit(URI uri) {
    // Edge case where the uri has no host e.g. 'http://www.s%C3%B8kbar.no', ignore it.
    if (uri.getHost() == null) return;
    if (!uri.getHost().equals(start.getHost())) return;

    var normalizedUri = normalizeUri(uri);

    synchronized (this) {
      if (!scheduledVisits.contains(normalizedUri)) {
        taskExecutor.scheduleTask(() -> visit(uri));
        scheduledVisits.add(normalizedUri);
      }
    }
  }

  /**
   * Converts a list of 'href' attribute values to a set of usable, absolute uris.
   *
   * <p>Does a best attempt at parsing the href value by encoding it and removing leading
   * whitespaces.
   *
   * <p>If a href does not resolve to a uri that has a http or https scheme, it will not be included
   * in the output.
   */
  private Set<URI> hrefToUri(URI baseUri, List<String> links) {
    return links.stream()
        .filter(s -> !s.isEmpty())
        // Edge case where uri contains whitespace at the beginning.
        .map(String::stripLeading)
        .map(UriEncoder::encode)
        .map(URI::create)
        .map(
            uri -> {
              if (uri.isAbsolute()) {
                return uri;
              }

              return baseUri.resolve(uri);
            })
        // Only use links that use http/https scheme.
        .filter(uri -> uri.getScheme().equals("http") || uri.getScheme().equals("https"))
        .collect(Collectors.toSet());
  }

  /** Extracts the 'href' attribute value from every '<a>' tag in the given html. */
  private List<String> extractHrefFromATags(InputStream html, URI baseUri) throws IOException {
    var doc = Jsoup.parse(html, null, baseUri.toString());
    return doc.body().getElementsByTag("a").stream()
        .map(e -> e.attr("href"))
        .collect(Collectors.toList());
  }

  /**
   * 'Normalizes' a uri to try prevent duplicates. Only keeps the authority, path, and query parts
   * of the uri. E.g. 'https://example.com/' and 'http://example.com/' will both become
   * 'example.com/'.
   */
  private String normalizeUri(URI uri) {
    var normalized = uri.getAuthority();
    if (uri.getPath() != null) {
      normalized += uri.getPath();
    }
    if (uri.getQuery() != null) {
      normalized += uri.getQuery();
    }

    return normalized;
  }
}
