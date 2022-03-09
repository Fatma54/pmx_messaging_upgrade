/**
 * ==============================================
 * PMX : Performance Model eXtractor
 * ==============================================
 * <p>
 * (c) Copyright 2014-2015, by Juergen Walter and Contributors.
 * <p>
 * Project Info:   http://descartes.tools/pmx
 * <p>
 * All rights reserved. This software is made available under the terms of the
 * Eclipse Public License (EPL) v1.0 as published by the Eclipse Foundation
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse Public License (EPL)
 * for more details.
 * <p>
 * You should have received a copy of the Eclipse Public License (EPL)
 * along with this software; if not visit http://www.eclipse.org or write to
 * Eclipse Foundation, Inc., 308 SW First Avenue, Suite 110, Portland, 97204 USA
 * Email: license (at) eclipse.org
 * <p>
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */
package org.palladiosimulator.pmxupgrade.pcm.console;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
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

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.IModelBuilder;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.pcm.builder.PCMBuilder;


public class PMXCommandLine {

    private static final Logger log = Logger.getLogger(PMXCommandLine.class);
    private static final CommandLineParser commmandLineParser = new BasicParser();
    private static final String CMD_LONG_OPT_INPUT_DIR = "trace-file";
    private static final String CMD_LONG_OPT_CONFIG_FILE = "pmx-config";
    private static final String CMD_LONG_OPT_OUTPUT_DIR = "output-dir";
    private static final String CMD_OPT_NUM_CORE = "cores";

    private static final HelpFormatter commandLineFormatter = new HelpFormatter();
    private static final String toolName = "Performance Model eXtractor(PMX)";

    public static void main(String[] args) throws PMXException {
        run(args);
    }

    public static boolean run(String[] args) throws PMXException {
        initConsoleLogging();
        PMXCommandLine cmd = PMXCommandLine.parse(args);
        PMXController pmx = cmd.createPMX(args);

        pmx.buildPerformanceModel();
        return true;
    }

    private static PMXCommandLine parse(String[] args) {
        return new PMXCommandLine();
    }

    private PMXController createPMX(String[] args) {
        try {

            org.palladiosimulator.pmxupgrade.model.common.Configuration configuration = new org.palladiosimulator.pmxupgrade.model.common.Configuration();

            String outputDir = "output/";
            String inputDir = "input/";

            initFileLogging(outputDir, "extraction.log", new SimpleLayout());
            initFileLogging(outputDir, "extraction.log.html", new HTMLLayout());
            LogManager.getRootLogger().setLevel(Level.INFO);

            configuration.setNumCores(getNumberOfCores());
            //configuration.setInputFileName("resources/json/combination.json");
            //configuration.setInputFileName("resources/json/streams-online-inferencing-tracing.json"); //1. use case ~fat
            configuration.setInputFileName("resources/json/traceWithoutCamundaOptimizeIngest.json"); //2. use case~fat

            configuration.setOutputDirectory(outputDir);

            PMXController controller = null;
            try {
                IModelBuilder builder = new PCMBuilder(configuration.getOutputDirectory());
                controller = new PMXController(configuration, builder);
            } catch (PMXException e) {
                log.error("PMXException", e);
            }

            return controller;
        } catch (NullPointerException e) {
            log.info("Nullpointer in class " + e.getStackTrace()[0].getClass() + ", method "
                    + e.getStackTrace()[0].getMethodName() + ", Line " + e.getStackTrace()[0].getLineNumber());
        } catch (IllegalArgumentException e) {
            log.info("IllegalArgumentException in class " + e.getStackTrace()[0].getClass() + ", method "
                    + e.getStackTrace()[0].getMethodName() + ", Line " + e.getStackTrace()[0].getLineNumber());
        }
        return null;

    }

    private HashMap<String, Integer> getNumberOfCores() {
        //TODO
        // String coresCmd = commandLine.getOptionValue(CMD_OPT_NUM_CORE);
        String coresCmd = null;
        if (coresCmd == null) {
            return null;
        }
        log.info(coresCmd + " < parsed number of cores specification");
        String[] numCoresDescriptions;
        if (coresCmd.contains(",")) {
            numCoresDescriptions = coresCmd.split(",");
        } else if (coresCmd.contains("=")) {
            numCoresDescriptions = new String[]{coresCmd};
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


}
