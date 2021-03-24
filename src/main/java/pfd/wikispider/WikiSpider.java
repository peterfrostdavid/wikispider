package pfd.wikispider;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.util.Set;
import java.util.HashSet;

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
		options.addRequiredOption("depth", "depth", true, "Number of hops to follow from seed pages");
		CommandLine cl = null;
		try {
			cl = parser.parse( options, args );
		} catch (ParseException e) {
			System.err.println( "Command Line Error: " + e.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("wikispider", options );
		}

		File seedFile = new File(cmd.getOptionValue("seedfile"));
		if (seedFile.canRead() == false) {
			System.err.println("Error: cannot read seedfile: " + seedFile.getAbsolutePath());
			System.exit(1);
		}

		File outFile = new File(cmd.getOptionValue("outfile"));
		if (outFile.canWrite() == false) {
			System.err.println("Error: cannot write to output: " + outFile.getAbsolutePath());
			System.exit(1);
		}

		int depth = 0;
		try {
			depth = Integer.parseInt(cmd.getOptionValue("depth"));
		} catch (NumberFormatException e) {
			System.err.println("Error: depth argument must specify an integer");
			System.exit(1);
		}

		List<String> seedPages = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(seedFile));
		String line = null;
		while ((line = br.readline) != null) {
			seedPages.add(line.trim());
		}

		PrintStream urls = new PrintStream(new FileOutputStream(outFile));

		Wiki wiki;
		wiki = new Wiki("en.wikipedia.org");

		Set<String> pagesFetched = new HashSet<>();
		for (String pageName : seedPages) {
			recurseLinks(wiki, urls, pagesFetched, pageName, depth);
		}
		urls.close();
	}

	private static void recurseLinks(
			Wiki wiki,
			PrintStream urls,
			Set<String> pagesFetched,
			String pageName,
			int depth) throws Exception {
		if (pagesFecthed.contains(pageName)) {
			return;
		}
		pagesFetched.add(pageName);

		// For potential future use:
		//   use wiki.getRenderedText(pageName) to get page text

		System.out.println(wiki.getPageInfo(pageName));
		if (depth == 0) {
			return;
		}

		String links[] = wiki.getLinksOnPage(pageName);
		for (String link : links) {
			if (link.startsWith("Wikipedia")) continue;
			if (link.startsWith("Help")) continue;
			recurse(wiki, urls, pagesFetched, link, depth-1);
		}
	}
}
