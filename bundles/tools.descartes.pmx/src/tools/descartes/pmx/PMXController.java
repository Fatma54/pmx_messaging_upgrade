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
package tools.descartes.pmx;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.plugin.filter.select.TimestampFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.builder.IModelBuilder;
import tools.descartes.pmx.filter.PerformanceModelFilterAppender;

public class PMXController {
	private static final Logger log = Logger
			.getLogger(PMXController.class);
	private IAnalysisController analysisController;
	private String outputDir;
	private boolean isReduced;
	private long ignoreExecutionsBeforeTimestamp = Long.parseLong(TimestampFilter.CONFIG_PROPERTY_VALUE_MIN_TIMESTAMP);
	private long ignoreExecutionsAfterTimestamp = Long.parseLong(TimestampFilter.CONFIG_PROPERTY_VALUE_MAX_TIMESTAMP);
	private static IModelBuilder modelBuilder;
	private HashMap<String, Integer> numCores;

	public PMXController(IAnalysisController analysisController, String path, String ignoreExecutionsBeforeTimestamp,
			String ignoreExecutionsAfterTimestamp, HashMap<String, Integer> numCores)
			throws IOException {
		//this(analysisController, path, ignoreExecutionsBeforeTimestamp, ignoreExecutionsAfterTimestamp, false);
		this(analysisController, path, ignoreExecutionsBeforeTimestamp, ignoreExecutionsAfterTimestamp, true, numCores);
	}

	public PMXController(IAnalysisController analysisController, String path, String ignoreExecutionsBeforeTimestamp,
			String ignoreExecutionsAfterTimestamp, boolean isReduced, HashMap<String, Integer> numCores)
			throws IOException {
		this.analysisController = analysisController;
		this.outputDir = path;
		this.isReduced = isReduced;
		if(isReduced){
			outputDir += "reduced"+File.separator;
		}
		if(ignoreExecutionsBeforeTimestamp != null){
			this.ignoreExecutionsBeforeTimestamp = Long.parseLong(ignoreExecutionsBeforeTimestamp);
		}
		if(ignoreExecutionsBeforeTimestamp != null){
			this.ignoreExecutionsAfterTimestamp = Long.parseLong(ignoreExecutionsAfterTimestamp);
		}
		this.numCores = numCores;
	}
	
	public void run() throws AnalysisConfigurationException {
		log.info("initializing filters... |||||||||||||||||||||||||||||||||||||||||||||||||");
		initAndConnectKiekerFilters(analysisController, outputDir, ignoreExecutionsBeforeTimestamp,
				ignoreExecutionsAfterTimestamp, isReduced, numCores);
		
		log.info("running filters...  |||||||||||||||||||||||||||||||||||||||||||||||||");
		analysisController.run(); 
	}

	/**
	 * Connects the Kieker filters
	 * 
	 * @param numCores
	 */
	private static void initAndConnectKiekerFilters(IAnalysisController analysisController, String resultPath,
			long ignoreExecutionsBeforeTimestamp, long ignoreExecutionsAfterTimestamp, boolean isReduced,
			HashMap<String, Integer> numCores) {
		// initialize and register the system model repository
		final SystemModelRepository systemModelRepository = new SystemModelRepository(
				new Configuration(), analysisController);

		// extract connection point from analysis controller
		AbstractPlugin reader = (AbstractPlugin) analysisController
				.getReaders().toArray()[0];
		reader.getCurrentConfiguration().setProperty(FSReader.CONFIG_PROPERTY_NAME_IGNORE_UNKNOWN_RECORD_TYPES, Boolean.TRUE.toString());
		String port = FSReader.OUTPUT_PORT_NAME_RECORDS;

		// connect lots of filters to the connection point
		try {
			PerformanceModelFilterAppender.initAndConnectFilters(systemModelRepository, reader, port,
					analysisController, resultPath, ignoreExecutionsBeforeTimestamp, ignoreExecutionsAfterTimestamp,
					isReduced, numCores);
		} catch (IllegalStateException e) {
			log.info(e);
		} catch (AnalysisConfigurationException e) {
			log.info(e);
		}
	}

	public static IModelBuilder getModelBuilder() {
		if (modelBuilder == null) {
			log.error("builder not initialized");
		}
		return modelBuilder;
	}

	public static void setModelBuilder(IModelBuilder newModelBuilder) {
		modelBuilder = newModelBuilder;
	}


}