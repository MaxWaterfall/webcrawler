package com.maxwaterfall.webcrawler;

/**
 * Entry point for the application.
 *
 * <p>Simply passes cli arguments to the command line interpreter.
 */
public class Main {

  public static void main(String[] args) {
    var cli = new Cli(System.out, System.err);
    System.exit(cli.interpret(args));
  }
}
