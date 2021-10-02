package com.maxwaterfall.webcrawler;

import com.maxwaterfall.webcrawler.crawl.Crawler;
import com.maxwaterfall.webcrawler.crawl.VisitedPage;
import java.io.PrintStream;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Cli is responsible for interpreting, validating and executing commands. It is also responsible
 * for showing output to the user.
 */
public class Cli {

  private final PrintStream outputStream;
  private final PrintStream errorStream;
  private final Clock clock;

  public Cli(PrintStream outputStream, PrintStream errorStream) {
    this.outputStream = outputStream;
    this.errorStream = errorStream;
    this.clock = Clock.systemUTC();
  }

  /** Alternative constructor with a clock for testing. */
  Cli(PrintStream outputStream, PrintStream errorStream, Clock clock) {
    this.outputStream = outputStream;
    this.errorStream = errorStream;
    this.clock = clock;
  }

  /**
   * Interprets, validates and executes commands.
   *
   * <p>If the command is valid, the only argument should be the starting link.
   *
   * @param args the arguments passed by the user to the program.
   * @return the system exit code.
   */
  public int interpret(String... args) {
    // Only argument is the starting link.
    if (args.length < 1) {
      log("Missing [starting-link] argument");
      logUsage();
      return 1;
    }

    URI start = null;
    try {
      start = URI.create(args[0]);
    } catch (IllegalArgumentException e) {
      errorLog("'" + args[0] + "' is not a valid uri");
      logUsage();
      return 1;
    }

    if (start.getScheme() == null) {
      errorLog("'" + args[0] + "' must contain a scheme");
      logUsage();
      return 1;
    }

    try {
      log("Starting to crawl " + start.toString() + ", please wait");
      var timerStart = clock.instant();
      var visitedPages = new Crawler(new HttpClient()).crawl(start);
      logResult(start, visitedPages, Duration.between(timerStart, clock.instant()));
      return 0;
    } catch (Exception e) {
      errorLog("An unexpected error occurred");
      e.printStackTrace(errorStream);
      logUsage();
      return 1;
    }
  }

  private void logResult(URI start, Set<VisitedPage> visitedPages, Duration timeTaken) {
    log("Crawl starting from " + start + " complete\n");
    log("Visited:");

    var sorted = sortVisitedPages(visitedPages);

    sorted.forEach(
        vp -> {
          log("    " + vp.getPageLink());
          vp.getSeenLinks()
              .forEach(
                  link -> {
                    log("        - " + link);
                  });
        });

    log("");
    log("Summary:");
    log("    Total visited: " + sorted.size());
    log("    Crawl time: " + timeTaken);
  }

  private void logUsage() {
    errorLog(
        "Usage: java -jar crawler.jar [starting-link]\nExample: java -jar crawler.jar https://example.com/");
  }

  private void errorLog(Object obj) {
    errorStream.println(obj);
  }

  private void log(Object obj) {
    outputStream.println(obj);
  }

  /** Sorts the visited pages using natural order. */
  private List<VisitedPage> sortVisitedPages(Set<VisitedPage> visitedPages) {
    var sorted = new ArrayList<>(visitedPages);

    // First sort all the 'seen' links on each page.
    sorted.forEach(vp -> vp.getSeenLinks().sort(Comparator.naturalOrder()));

    // Now sort the visited page links themselves.
    sorted.sort(Comparator.comparing(VisitedPage::getPageLink));

    return sorted;
  }
}
