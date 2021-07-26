package com.maxwaterfall.webcrawler;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Holds the result of a web crawl.
 */
public class CrawlResult {

  private final URI start;
  private final Collection<URI> visited;
  private final Collection<URI> seen;

  public CrawlResult(URI start, Collection<URI> visited, Collection<URI> seen) {
    this.start = start;
    this.visited = visited;
    this.seen = seen;
  }

  public URI getStart() {
    return start;
  }

  public Collection<URI> getVisited() {
    return visited;
  }

  public Collection<URI> getSeen() {
    return seen;
  }
}
