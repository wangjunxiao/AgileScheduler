package cn.dlut.core.main;

import cn.dlut.core.cmd.CmdLineSettings;
import cn.dlut.exceptions.CoordinatorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public final class Main {

	public static final String VERSION = "Coordinator-0.1.0";
	private static Logger log = LogManager.getLogger(Main.class.getName());

	/**
	 * Main method to start the Coordinator, Parses command line arguments.
	 * 
	 * @param args
	 *            string of command line parameters
	 */
	public static void main(final String[] args) throws CoordinatorException {
		final CmdLineSettings settings = new CmdLineSettings();
		final CmdLineParser parser = new CmdLineParser(settings);
		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			parser.printUsage(System.out);
			System.exit(1);
		}

		final Coordinator coordinator = new Coordinator(settings);
		Main.log.info("Starting Coordinator...");
		coordinator.run();
	}

}
