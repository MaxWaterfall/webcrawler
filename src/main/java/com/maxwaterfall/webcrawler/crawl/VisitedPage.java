package com.maxwaterfall.webcrawler.crawl;

import java.util.List;

/**
 * Holds a page that has been visited and its links.
 *
 * <p>Stores the links as 'normalized' uris to try prevent duplicates. E.g. 'https://example.com/'
 * and 'http://example.com/' will both be stored as 'example.com/'.
 */
public class VisitedPage {

  private final String pageLink;
  private final List<String> seenLinks;

  VisitedPage(String pageLink, List<String> seenLinks) {
    this.pageLink = pageLink;
    this.seenLinks = seenLinks;
  }

  public String getPageLink() {
    return pageLink;
  }

  public List<String> getSeenLinks() {
    return seenLinks;
  }
}
