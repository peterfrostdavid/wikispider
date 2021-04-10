package pfd.wikispider;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

import org.wikipedia.Wiki;

public class WikiSpider {
	public static void main(String args[]) throws Exception {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addRequiredOption("seedfile", "seed-file", true, "File containing seed page titles");
		options.addRequiredOption("outfile", "outfile", true, "File to contain output URLs");
		options.addRequiredOption("cmd", "cmd", true, "Command to perform: spider or links");
		options.addRequiredOption("depth", "depth", false, "Number of hops to follow from seed pages");
		CommandLine cl = null;
		try {
			cl = parser.parse( options, args );
		} catch (ParseException e) {
			System.err.println( "Command Line Error: " + e.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("wikispider", options );
			System.exit(1);
		}

		File seedFile = new File(cl.getOptionValue("seedfile"));
		if (seedFile.canRead() == false) {
			System.err.println("Error: cannot read seedfile: " + seedFile.getAbsolutePath());
			System.exit(1);
		}

		File outFile = new File(cl.getOptionValue("outfile"));
		if (outFile.canWrite() == false) {
			System.err.println("Error: cannot write to output: " + outFile.getAbsolutePath());
			System.exit(1);
		}

		Wiki wiki;
		wiki = new Wiki("en.wikipedia.org");

		String cmd = cl.getOptionValue("cmd");
		if (cmd.equals("spider")) {
			if (cl.hasOption("depth") == false) {
				System.err.println("Error: spider command requires -depth");
				System.exit(1);
			}

			int depth = 0;
			try {
				depth = Integer.parseInt(cl.getOptionValue("depth"));
			} catch (NumberFormatException e) {
				System.err.println("Error: depth argument must specify an integer");
				System.exit(1);
			}

			spider(wiki, seedFile, outFile, depth);
		} else if (cmd.equals("links")) {
			links(wiki, seedFile, outFile);
		} else {
			System.err.println("Error: " + cmd + " is not a valid command.");
			System.exit(1);
		}
	}

	private static void spider(Wiki wiki, File seedFile, File outFile, int depth) throws Exception {
		List<String> seedPages = readSeeds(seedFile);
		PrintStream pages = new PrintStream(new FileOutputStream(outFile));

		Set<String> pagesFetched = new HashSet<>();
		for (String pageName : seedPages) {
			recurseLinks(wiki, pages, pagesFetched, pageName, depth);
		}
		pages.close();


	}

	private static void links(Wiki wiki, File seedFile, File outFile) throws Exception {
	}

	private static List<String> readSeeds(File seedFile) throws Exception {
		List<String> seedPages = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(seedFile));
		String line = null;
		while ((line = br.readLine()) != null) {
			seedPages.add(line.trim());
		}
		return seedPages;
	}

	private static void recurseLinks(
			Wiki wiki,
			PrintStream pages,
			Set<String> pagesFetched,
			String pageName,
			int depth) throws Exception {
		if (pagesFetched.contains(pageName)) {
			return;
		}
		pagesFetched.add(pageName);
		pages.println(pageName);

		// For potential future use:
		//   use wiki.getRenderedText(pageName) to get page text

		System.out.println(pageName);
		if (depth == 0) {
			return;
		}

		String links[] = wiki.getLinksOnPage(pageName);
		for (String link : links) {
			if (link.startsWith("Wikipedia")) continue;
			if (link.startsWith("Help")) continue;
			recurseLinks(wiki, pages, pagesFetched, link, depth-1);
		}
	}
}
