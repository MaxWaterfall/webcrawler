package com.maxwaterfall.webcrawler;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.UriEncoder;

/**
 * Crawls web pages. NOT safe for concurrent use.
 */
@Service
public class Crawler {

  private final PageFetcher pageFetcher;

  public Crawler(PageFetcher pageFetcher) {
    this.pageFetcher = pageFetcher;
  }

  public CrawlResult crawl(URI startingLink) {
    Set<URI> visited = ConcurrentHashMap.newKeySet();
    Set<URI> seen = ConcurrentHashMap.newKeySet();

    seen.add(startingLink);
    visit(startingLink, visited, seen);

    return new CrawlResult(startingLink, visited, seen);
  }

  private void visit(URI currentUri, Set<URI> visited, Set<URI> seen) {
    String html = pageFetcher.fetch(currentUri);

    // TODO: don't add uri that we haven't actually visited.
    visited.add(currentUri);

    var hrefs = extractHrefFromATags(html);
    var uris = hrefToUri(currentUri, hrefs);

    // We've seen all these links now.
    seen.addAll(uris);

    uris.stream()
        // Do not visit links we've already visited.
        .filter(uri -> !visited.contains(uri))
        // Visit all links on the same domain.
        .filter(uri -> {

          if (uri.getHost() == null || currentUri.getHost() == null) {
            System.out.println("here!");
          }
          return uri.getHost().equals(currentUri.getHost());
        })
        .forEach(uri -> visit(uri, visited, seen));
  }


  /**
   * Converts a list of 'href' attribute values to a list of usable, absolute uris.
   */
  private List<URI> hrefToUri(URI baseUri, List<String> links) {
    return links.stream()
        .map(s -> {
          // Don't include hrefs to page locations.
          if (s.contains("#")) {
            return s.substring(0, s.indexOf("#"));
          }

          return s;
        })
        .filter(s -> !s.isEmpty())
        .map(UriEncoder::encode)
        .map(URI::create)
        .map(uri -> {
          if (uri.isAbsolute()) {
            return uri;
          }

          return baseUri.resolve(uri);
        })
        // Only use links that use http/https scheme.
        .filter(uri -> uri.getScheme().equals("http") || uri.getScheme().equals("https"))
        .collect(Collectors.toList());
  }

  /**
   * Finds all the '<a>' tags then the 'href' attribute in the given html.
   */
  private List<String> extractHrefFromATags(String html) {
    var doc = Jsoup.parse(html);
    return doc.body()
        .getElementsByTag("a")
        .stream()
        .map(e -> e.attr("href"))
        .collect(Collectors.toList());
  }

}
