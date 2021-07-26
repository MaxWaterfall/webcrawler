package com.maxwaterfall.webcrawler;

import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Cli is responsible for interpreting, validating and executing commands.
 * It is also responsible for showing output to the user.
 */
@Service
public class Cli {

  private static Logger LOG = LoggerFactory.getLogger(Cli.class);

  private final Crawler crawler;

  public Cli(Crawler crawler) {
    this.crawler = crawler;
  }

  /**
   * Interprets, validates and executes commands.
   *
   * The only argument is the starting link.
   *
   * @param args the arguments passed by the user to the program.
   */
  public void interpret(String ...args) {
    // Only argument is the starting link.
    if (args.length < 1) {
      log("Missing [starting-link] argument");
      logUsageAndQuit();
    }

    try {
      var startingLink = URI.create(args[0]);
      log("Starting to crawl " + startingLink.toString());
      var result = crawler.crawl(startingLink);
      logResultAndQuit(result);
    } catch (IllegalArgumentException e) {
      log("[starting-link] is not a valid uri");
      LOG.error("Exception occurred", e);
      logUsageAndQuit();
    } catch (Exception e) {
      log("An unexpected error occurred");
      log("Error message: " + e.getMessage());
      LOG.error("Exception occurred", e);
      logUsageAndQuit();
    }

  }

  private void logResultAndQuit(CrawlResult result) {
    log("Crawl starting from " + result.getStart() + " complete\n");

    log("Visited:");
    result.getVisited().forEach(s -> log("    " + s));
    log("Seen:");
    result.getSeen().forEach(s -> log("    " + s));

    System.exit(0);
  }

  private void logUsageAndQuit() {
    log("Usage: java -jar crawler.jar [starting-link]\nExample: java -jar crawler.jar https://monzo.com/");
    System.exit(1);
  }

  private void log(Object obj) {
    System.out.println(obj);
  }

}
