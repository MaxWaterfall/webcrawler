package com.maxwaterfall.webcrawler;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the application.
 *
 * Simply passes cli arguments to the command line interpreter.
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

	private final Cli cli;

	public Main(Cli cli) {
		this.cli = cli;
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		cli.interpret(args);
	}
}
