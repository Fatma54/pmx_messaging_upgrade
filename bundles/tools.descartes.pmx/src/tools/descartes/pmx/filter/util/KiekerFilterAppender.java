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
package tools.descartes.pmx.filter.util;

import kieker.analysis.IAnalysisController;
import kieker.analysis.analysisComponent.AbstractAnalysisComponent;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.flow.MyEventRecordTraceReconstructionFilter;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.Constants;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.flow.MyTraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.util.FilterPortTuple;

public class KiekerFilterAppender {

	/**
	 * Receives IMonitoringRecord entries from reader
	 * 
	 * @param analysisController
	 * @param systemModelRepository
	 * @param fpt
	 * @throws IllegalStateException
	 * @throws AnalysisConfigurationException
	 */
	public static AbstractFilterPlugin addStandardFilters(IAnalysisController analysisController,
			SystemModelRepository systemModelRepository, FilterPortTuple fpt, boolean isReduced)
			throws IllegalStateException, AnalysisConfigurationException {
		EventRecordTraceReconstructionFilter eventRecordTraceReconstructionFilter = new EventRecordTraceReconstructionFilter(
				new Configuration(), analysisController);
		analysisController.connect(fpt.filter, fpt.port, eventRecordTraceReconstructionFilter,
				EventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);

		Configuration configurationEventTrace2ExecutionTraceFilter = new Configuration();
		TraceEventRecords2ExecutionAndMessageTraceFilter traceEvents2ExecutionAndMessageTraceFilter = new TraceEventRecords2ExecutionAndMessageTraceFilter(
				configurationEventTrace2ExecutionTraceFilter, analysisController);
		analysisController.connect(eventRecordTraceReconstructionFilter,
				EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID,
				traceEvents2ExecutionAndMessageTraceFilter,
				TraceEventRecords2ExecutionAndMessageTraceFilter.INPUT_PORT_NAME_EVENT_TRACE);
		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		return traceEvents2ExecutionAndMessageTraceFilter;
	}

	private static TraceReconstructionFilter addStandardGraphFilters(IAnalysisController analysisController,
			SystemModelRepository systemModelRepository, FilterPortTuple fpt)
			throws IllegalStateException, AnalysisConfigurationException {
		// to execution objects
		ExecutionRecordTransformationFilter executionRecordTransformationFilter = addExecutionRecordTransformationFilter(
				analysisController, fpt, systemModelRepository);

		// Uses the incoming data to enrich the connected repository with the
		// reconstructed traces
		TraceReconstructionFilter traceReconstructionFilter = addTraceReconstructionFilter(analysisController,
				executionRecordTransformationFilter, systemModelRepository);

		return traceReconstructionFilter;
	}

	private static FilterPortTuple addSessionReconstructionFilters(IAnalysisController analysisController,
			FilterPortTuple traceReconstructionFilter_fpt)
			throws IllegalStateException, AnalysisConfigurationException {
		// Initialize, register and connect the session reconstruction filter
		final Configuration bareSessionReconstructionFilterConfiguration = new Configuration();
		bareSessionReconstructionFilterConfiguration.setProperty(
				SessionReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_THINK_TIME,
				SessionReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_THINK_TIME);

		final SessionReconstructionFilter sessionReconstructionFilter = new SessionReconstructionFilter(
				bareSessionReconstructionFilterConfiguration, analysisController);
		analysisController.connect(traceReconstructionFilter_fpt.filter,
				TraceReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE, // fpt.port
				sessionReconstructionFilter, SessionReconstructionFilter.INPUT_PORT_NAME_EXECUTION_TRACES);

		// // Initialize, register and connect the list collection filter
		// final ListCollectionFilter<ExecutionTraceBasedSession>
		// listCollectionFilter = new
		// ListCollectionFilter<ExecutionTraceBasedSession>(new Configuration(),
		// analysisController);
		// analysisController.connect(sessionReconstructionFilter,
		// SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS,
		// listCollectionFilter, ListCollectionFilter.INPUT_PORT_NAME);
		// return new FilterPortTuple(listCollectionFilter,
		// ListCollectionFilter.OUTPUT_PORT_NAME);

		return new FilterPortTuple(sessionReconstructionFilter,
				SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS);
	}

	private static FilterPortTuple addMyEventRecordTraceReconstructionFilter(IAnalysisController analysisController,
			FilterPortTuple fpt, SystemModelRepository systemModelRepository) throws AnalysisConfigurationException {
		final MyEventRecordTraceReconstructionFilter eventTraceReconstructionFilter = new MyEventRecordTraceReconstructionFilter(
				new Configuration(), analysisController);
		analysisController.connect(fpt.filter, fpt.port, eventTraceReconstructionFilter,
				MyEventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);
		return new FilterPortTuple(eventTraceReconstructionFilter,
				MyEventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID);
	}

	private static FilterPortTuple addEventRecordTraceReconstructionFilter(IAnalysisController analysisController,
			FilterPortTuple fpt, SystemModelRepository systemModelRepository) throws AnalysisConfigurationException {
		final EventRecordTraceReconstructionFilter eventTraceReconstructionFilter = new EventRecordTraceReconstructionFilter(
				new Configuration(), analysisController);
		analysisController.connect(fpt.filter, fpt.port, eventTraceReconstructionFilter,
				EventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);
		return new FilterPortTuple(eventTraceReconstructionFilter,
				EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID);
	}

	private static TraceReconstructionFilter addTraceReconstructionFilter(IAnalysisController analysisController,
			ExecutionRecordTransformationFilter executionRecordTransformationFilter,
			SystemModelRepository systemModelRepository) throws IllegalStateException, AnalysisConfigurationException {
		// Create the trace reconstruction filter and connect to the record
		// transformation filter's output port
		final Configuration mtReconstrFilterConfig = new Configuration();
		// mtReconstrFilterConfig.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
		// Constants.TRACERECONSTR_COMPONENT_NAME);
		// mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_TIMEUNIT,
		// TimeUnit.MILLISECONDS.name());

		// TODO tune parameters to get not memory problems
		// mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION,
		// Integer.toString(50));
		mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_IGNORE_INVALID_TRACES,
				Boolean.toString(true));

		TraceReconstructionFilter mtReconstrFilter = new TraceReconstructionFilter(mtReconstrFilterConfig,
				analysisController);
		analysisController.connect(mtReconstrFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);
		// analysisController.connect(execRecTransformer,
		// ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS,
		analysisController.connect(executionRecordTransformationFilter,
				ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS, mtReconstrFilter,
				TraceReconstructionFilter.INPUT_PORT_NAME_EXECUTIONS);
		return mtReconstrFilter;
		// return new FilterPortTuple(mtReconstrFilter,
		// ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS);
	}

	// private static void addTeeFilter(IAnalysisController analysisController,
	// FilterPortTuple fpt) throws AnalysisConfigurationException {
	// // TeeFilter
	// final Configuration confTeeFilter = new Configuration();
	// confTeeFilter.setProperty(TeeFilter.CONFIG_PROPERTY_NAME_STREAM,
	// TeeFilter.CONFIG_PROPERTY_VALUE_STREAM_STDLOG);
	// TeeFilter teeFilter = new TeeFilter(confTeeFilter, analysisController);
	//
	// analysisController.connect(fpt.filter, fpt.port, teeFilter,
	// TeeFilter.INPUT_PORT_NAME_EVENTS);
	// }

	private static ExecutionRecordTransformationFilter addExecutionRecordTransformationFilter(
			IAnalysisController analysisController, FilterPortTuple fpt, SystemModelRepository systemModelRepository)
			throws IllegalStateException, AnalysisConfigurationException {
		final Configuration execRecTransformerConfig = new Configuration();
		execRecTransformerConfig.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
				Constants.EXEC_TRACE_RECONSTR_COMPONENT_NAME);
		ExecutionRecordTransformationFilter execRecTransformer = new ExecutionRecordTransformationFilter(
				execRecTransformerConfig, analysisController);
		analysisController.connect(execRecTransformer, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);
		analysisController.connect(fpt.filter, fpt.port, execRecTransformer,
				ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);
		// fpt = new FilterPortTuple(execRecTransformer,
		// ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS);
		return execRecTransformer; // fpt;
	}

}
