/**
 * ==============================================
 *  PMX : Performance Model eXtractor
 * ==============================================
 *
 * (c) Copyright 2014-2015, by Juergen Walter and Contributors.
 *
 * Project Info:   http://descartes.tools/pmx
 *
 * All rights reserved. This software is made available under the terms of the
 * Eclipse Public License (EPL) v1.0 as published by the Eclipse Foundation
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse Public License (EPL)
 * for more details.
 *
 * You should have received a copy of the Eclipse Public License (EPL)
 * along with this software; if not visit http://www.eclipse.org or write to
 * Eclipse Foundation, Inc., 308 SW First Avenue, Suite 110, Portland, 97204 USA
 * Email: license (at) eclipse.org
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */
package tools.descartes.pmx.console;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;

import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.select.TimestampFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.common.util.filesystem.FSUtil;
import tools.descartes.pmx.PMXController;
import tools.descartes.pmx.builder.IModelBuilder;

public class PMXCommandLine {

	private static final Logger log = Logger.getLogger(PMXCommandLine.class);
	private CommandLine commandLine;
	private static final CommandLineParser commmandLineParser = new BasicParser();
	private static final Options options = new Options();
	private static final String CMD_LONG_OPT_INPUT_DIR = "trace-file";
	private static final String CMD_LONG_OPT_CONFIG_FILE = "pmx-config";
	private static final String CMD_LONG_OPT_OUTPUT_DIR = "output-dir";
	private static final String CMD_OPT_NUM_CORE = "cores";

	private static final HelpFormatter commandLineFormatter = new HelpFormatter();
	private static final String toolName = "Performance Model eXtractor(PMX)";
	private static IModelBuilder modelBuilder;

	static {
		Option configOption;
		options.addOption("h", "help", false, "Show this help.");
		options.addOption("r", "reduced", false, "reduces the recursion depth for the extracted model");

		OptionBuilder.withArgName("input");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(CMD_LONG_OPT_INPUT_DIR);
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("input dir of Kieker trace file");
		OptionBuilder.withValueSeparator(' ');
		Option traceFielOption = OptionBuilder.create("i");
		options.addOption(traceFielOption);

		OptionBuilder.withArgName("output");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(CMD_LONG_OPT_OUTPUT_DIR);
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("output dir");
		OptionBuilder.withValueSeparator(' ');
		configOption = OptionBuilder.create("o");
		options.addOption(configOption);

		OptionBuilder.withArgName("file");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(CMD_LONG_OPT_CONFIG_FILE);
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("configuration file");
		OptionBuilder.withValueSeparator(' ');
		configOption = OptionBuilder.create("c");
		options.addOption(configOption);

		OptionBuilder.withArgName("long");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_BEFORE_TIMESTAMP);
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("ignores logs before timestamp");
		OptionBuilder.withValueSeparator(' ');
		Option ignoreBeforeOption = OptionBuilder.create("b");
		options.addOption(ignoreBeforeOption);

		OptionBuilder.withArgName("long");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_AFTER_TIMESTAMP);
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("ignores logs after timestamp");
		OptionBuilder.withValueSeparator(' ');
		Option ignoreAfterOption = OptionBuilder.create("a");
		options.addOption(ignoreAfterOption);

		OptionBuilder.withArgName("cores");
		OptionBuilder.hasArg();
		OptionBuilder.withLongOpt(PMXCommandLine.CMD_OPT_NUM_CORE);
		OptionBuilder.isRequired(false);
		OptionBuilder.withDescription("specifies number of CPU cores for hosts");
		OptionBuilder.withValueSeparator(' ');
		Option numCoresOption = OptionBuilder.create("n");
		options.addOption(numCoresOption);
	}

	public static void main(String[] args) {
		run(args);
	}

	public static boolean run(String[] args) {
		initConsoleLogging();
		PMXCommandLine cmd = PMXCommandLine.parse(args);
		PMXController pmx = cmd.createPMX();
		try {
			pmx.run();
			return true;
		} catch (AnalysisConfigurationException e) {
			log.info("AnalysisConfigurationException" + e);
			return false;
		} catch (NullPointerException e) {
			log.info("NullPointerException" + e);
			return false;
		}
	}

	private static PMXCommandLine parse(String[] args) {
		PMXCommandLine cmd = new PMXCommandLine();
		try {
			cmd.commandLine = commmandLineParser.parse(options, args);
		} catch (final ParseException e) {
			String s = "";
			for (String sub : args) {
				s += " " + sub;
			}
			if (s.contains("-h") || s.contains("--help")) {
				// --help is no error even if the required file path is missing
			} else {
				log.info("Error parsing arguments:" + s);
				// log.info(e.getMessage());
			}
			PMXCommandLine.printUsage();
		}
		return cmd;
	}

	private PMXController createPMX() {
		try {
			String[] inputDirs = getInputDirs();
			Configuration pmxConfiguration = new Configuration();
			AnalysisController analysisController = new AnalysisController(pmxConfiguration);
			registerFSReader(analysisController, inputDirs);

			String outputDir = commandLine.getOptionValue(CMD_LONG_OPT_OUTPUT_DIR);
			if (outputDir == null) {
				outputDir = inputDirs[0];
				log.info("No output directory specified. Logging to input directory.");
			}

			HashMap<String, Integer> numCores = getNumberOfCores();

			initFileLogging(outputDir, "extraction.log", new SimpleLayout());
			initFileLogging(outputDir, "extraction.log.html", new HTMLLayout());
			LogManager.getRootLogger().setLevel(Level.INFO); // log all except
																// // for debug

			PMXController controller = new PMXController(analysisController, outputDir,
					commandLine.getOptionValue(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_BEFORE_TIMESTAMP),
					commandLine.getOptionValue(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_AFTER_TIMESTAMP),
					commandLine.hasOption("reduced"), numCores);
			return controller;
		} catch (NullPointerException e) {
			// PMXCommandLine.printUsage();
			log.info("Nullpointer in class " + e.getStackTrace()[0].getClass() + ", method "
					+ e.getStackTrace()[0].getMethodName() + ", Line " + e.getStackTrace()[0].getLineNumber());
		} catch (IllegalArgumentException e) {
			log.info("IllegalArgumentException in class " + e.getStackTrace()[0].getClass() + ", method "
					+ e.getStackTrace()[0].getMethodName() + ", Line " + e.getStackTrace()[0].getLineNumber());
		} catch (IOException e) {
			log.info("IOException in class " + e.getStackTrace()[0].getClass() + ", method "
					+ e.getStackTrace()[0].getMethodName() + ", Line " + e.getStackTrace()[0].getLineNumber());
		}
		return null;

	}

	private HashMap<String, Integer> getNumberOfCores() {
		String coresCmd = commandLine.getOptionValue(CMD_OPT_NUM_CORE);
		if (coresCmd == null) {
			return null;
		}
		log.info(coresCmd + " < parsed number of cores specification");
		String[] numCoresDescriptions;
		if (coresCmd.contains(",")) {
			numCoresDescriptions = coresCmd.split(",");
		} else if (coresCmd.contains("=")) {
			numCoresDescriptions = new String[] { coresCmd };
		} else {
			numCoresDescriptions = new String[0];
		}
		HashMap<String, Integer> numberOfCores = new HashMap<String, Integer>();
		for (String ab : numCoresDescriptions) {
			numberOfCores.put(ab.split("=")[0], Integer.parseInt(ab.split("=")[1]));
			log.info(ab.split("=")[0] + " = " + Integer.parseInt(ab.split("=")[1]));
		}
		return numberOfCores;
	}

	private String[] getInputDirs() {
		String inputDirCmd = commandLine.getOptionValue(CMD_LONG_OPT_INPUT_DIR);
		String[] inputDirs = inputDirCmd.split(";");
		for (int idx = 0; idx < inputDirs.length; idx++) {
			final String inputDir = inputDirs[idx]; // + File.separator;
			if (inputDir == null) {
				log.info("missing value for command line option '" + CMD_LONG_OPT_INPUT_DIR + "'");
				System.exit(1);
			} else if (!new File(inputDir).exists()) {
				log.info("File does not exist: " + CMD_LONG_OPT_INPUT_DIR);
				log.info("\tcurrent:" + System.getProperty("user.dir"));
				log.info("\trequested:" + inputDir);
				System.exit(1); // or other exit code?
			} else {
				// inputDirs[idx] = inputDir;
				if (!new File(inputDir).getName().startsWith("kieker-")) {
					String[] recursive = recursiveKiekerFileSearch(inputDir);
					inputDirs = concat(inputDirs, recursive);
					for (String sys : recursive) {
						log.info(sys);
					}
				}
				// inputDirs[idx] = null;
			}
		}
		return inputDirs;
	}

	private String[] concat(String[] a, String[] b) {
		int aLen = a.length;
		int bLen = b.length;
		String[] c = new String[aLen + bLen];
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);
		return c;
	}

	String[] recursiveKiekerFileSearch(String inputDir) {
		final String[] inputDirs;
		// search subdirectories for folders "kieker- ..."
		File[] fileArray = new File(inputDir).listFiles();
		ArrayList<String> inputDirList = new ArrayList<String>();
		for (int i = 0; i < fileArray.length; i++) {
			if (fileArray[i].toString().contains("kieker-")) {
				inputDirList.add(fileArray[i].getAbsolutePath() + File.separator);
			}
		}
		return inputDirs = (String[]) inputDirList.toArray(new String[inputDirList.size()]);
	}

	private static FSReader registerFSReader(IAnalysisController analysisInstance, final String[] kiekerTraceFile) {
		final String[] inputDirs = kiekerTraceFile;

		// if (new File(kiekerTraceFile).getName().startsWith("kieker-")) {
		// inputDirs = new String[1];
		// inputDirs[0] = kiekerTraceFile;
		// } else {
		// // search subdirectories for folders "kieker- ..."
		// File[] fileArray = new File(kiekerTraceFile).listFiles();
		// ArrayList<String> inputDirList = new ArrayList<String>();
		// for (int i = 0; i < fileArray.length; i++) {
		// if (fileArray[i].toString().contains("kieker-")) {
		// inputDirList.add(fileArray[i].getAbsolutePath());
		// }
		// }
		// inputDirs = (String[]) inputDirList.toArray(new
		// String[inputDirList.size()]);
		// }

		final Configuration readerConfiguration = new Configuration();
		readerConfiguration.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, Configuration.toProperty(inputDirs));
		final FSReader fsReader = new FSReader(readerConfiguration, analysisInstance);
		return fsReader;
	}

	private static void printUsage() {
		commandLineFormatter.printHelp(toolName, options);
	}

	private static void initConsoleLogging() {
		try {
			SimpleLayout simpleLayout = new SimpleLayout();
			// PatternLayout layout = new PatternLayout("%-5p [%t]: %m%n");
			PatternLayout layout = new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN);
			ConsoleAppender consoleAppender = new ConsoleAppender(simpleLayout);
			BasicConfigurator.configure(consoleAppender);
		} catch (Exception ex) {
			log.error("Error during inialization of logging");
		}
	}

	private static void initFileLogging(String directory, String logFileName, Layout layout) {
		try {
			// add logging to file
			// new File(outputDir).isDirectory()?(outputDir):(outputDir+
			// File.separator)
			String path = directory + File.separator + logFileName;
			log.info("logging to file " + path);
			FileAppender fileAppender = new FileAppender(layout, path, false);
			BasicConfigurator.configure(fileAppender);
		} catch (Exception ex) {
			log.error("Error during inialization of logging");
		}
	}

	/**
	 * Returns if the specified input directories {@link #inputDirs} exist and
	 * that each one is a monitoring log. If this is not the case for one of the
	 * directories, an error message is printed to stderr.
	 * 
	 * @return true if {@link #outputDir} is exists and is a directory; false
	 *         otherwise TODO Integrate check in comand line and gui
	 */
	private boolean assertInputDirsExistsAndAreMonitoringLogs(String[] inputDirs) {
		if (inputDirs == null) {
			log.error("No input directories configured");
			return false;
		}

		for (final String inputDir : inputDirs) {
			final File inputDirFile = new File(inputDir);
			try {
				if (!inputDirFile.exists()) {
					log.error("The specified input directory '" + inputDirFile.getCanonicalPath() + "' does not exist");
					return false;
				}
				if (!inputDirFile.isDirectory() && !inputDir.endsWith(FSUtil.ZIP_FILE_EXTENSION)) {
					log.error("The specified input directory '" + inputDirFile.getCanonicalPath()
							+ "' is neither a directory nor a zip file");
					return false;
				}
				// check whether inputDirFile contains a (kieker|tpmon).map
				// file; the latter for legacy reasons
				if (inputDirFile.isDirectory()) { // only check for dirs
					final File[] mapFiles = { new File(inputDir + File.separatorChar + FSUtil.MAP_FILENAME),
							new File(inputDir + File.separatorChar + FSUtil.LEGACY_MAP_FILENAME), };
					boolean mapFileExists = false;
					for (final File potentialMapFile : mapFiles) {
						if (potentialMapFile.isFile()) {
							mapFileExists = true;
							break;
						}
					}
					if (!mapFileExists) {
						log.error("The specified input directory '" + inputDirFile.getCanonicalPath()
								+ "' is not a kieker log directory");
						return false;
					}
				}
			} catch (final IOException e) { // thrown by File.getCanonicalPath()
				log.error("Error resolving name of input directory: '" + inputDir + "'");
			}
		}

		return true;
	}

	public static String parseOutputDir(String[] args) {
		PMXCommandLine commandLine = PMXCommandLine.parse(args);
		return commandLine.parseOutputDir();
	}

	private String parseOutputDir() {
		String outputDir = commandLine.getOptionValue(CMD_LONG_OPT_OUTPUT_DIR);
		if (outputDir == null) {
			outputDir = getInputDirs()[0];
			log.info("No output directory specified. Logging to input directory.");
		}
		return outputDir;
	}

	public static IModelBuilder getModelBuilder() {
		return modelBuilder;
	}

	public static void setModelBuilder(IModelBuilder modelBuilder) {
		PMXCommandLine.modelBuilder = modelBuilder;
		PMXController.setModelBuilder(modelBuilder);
	}
}
