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
package tools.descartes.pmx.filter.usage;

import java.io.File;

import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter;
import tools.descartes.pmx.util.FilterPortTuple;

public class UsageFilterAppender {
	
	
	public static void addUsageFilter(FilterPortTuple sessionReconstruction_fpt, IAnalysisController analysisController, String resultPath)
			throws AnalysisConfigurationException {
		final Configuration sessionDatWriterConfiguration = new Configuration();
		sessionDatWriterConfiguration.setProperty(Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_NAME_STREAM, resultPath+"/usage/"+ "session.dat");
		sessionDatWriterConfiguration.setProperty(Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_NAME_APPEND, "false");
		(new File(resultPath+ "usage" + File.separator)).mkdir();
		final Session2UsageProfileWriterPlugin sessionsDatWriter = new Session2UsageProfileWriterPlugin(sessionDatWriterConfiguration, analysisController);
		analysisController.connect(sessionReconstruction_fpt.filter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS, 
				sessionsDatWriter, Session2UsageProfileWriterPlugin.INPUT_PORT_NAME_SESSIONS);
	}

}
