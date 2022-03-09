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
package tools.descartes.pmx.util;

import org.apache.log4j.Logger;

import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.filter.select.TimestampFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.opad.filter.TimeSeriesPointAggregatorFilter;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.ExecutionTraceBasedSession;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.util.FilterPortTuple;
import tools.descartes.pmx.util.Util;

public class Util {
	private static final Logger log = Logger.getLogger(Util.class);

	public static FilterPortTuple addTimeStampFilter(
			IAnalysisController analysisController, FilterPortTuple fpt,
			long start, long end) {
		if (start == 0 && end == 0) {
			return new FilterPortTuple((AbstractPlugin) fpt.filter,
					FSReader.OUTPUT_PORT_NAME_RECORDS);
		}

		try {
			TimestampFilter timeStampFilter = createTimestampFilter(
					analysisController, start, end);
			analysisController.connect(fpt.filter, fpt.port, timeStampFilter,
					TimestampFilter.INPUT_PORT_NAME_ANY_RECORD);
			return new FilterPortTuple(timeStampFilter,
					TimestampFilter.OUTPUT_PORT_NAME_WITHIN_PERIOD);
		} catch (AnalysisConfigurationException e) {
			log.error(e);
			return fpt;
		}
	}
	/**
	 * Creates a {@link TimestampFilter} with the given properties using the
	 * constructor
	 * {@link TimestampFilter#TimestampFilter(kieker.common.configuration.Configuration, java.util.Map)}
	 * .
	 * 
	 * @param ignoreExecutionsBeforeTimestamp
	 * @param ignoreExecutionsAfterTimestamp
	 * @return
	 * @throws AnalysisConfigurationException
	 *             If the internally assembled analysis configuration is somehow
	 *             invalid.
	 * @throws IllegalStateException
	 *             If the internal analysis is in an invalid state.
	 */
	private static TimestampFilter createTimestampFilter(
			IAnalysisController analysisController,
			final long ignoreExecutionsBeforeTimestamp,
			final long ignoreExecutionsAfterTimestamp)
			throws IllegalStateException, AnalysisConfigurationException {
		final Configuration cfg = new Configuration();
		cfg.setProperty(
				TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_BEFORE_TIMESTAMP,
				Long.toString(ignoreExecutionsBeforeTimestamp));
		cfg.setProperty(
				TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_AFTER_TIMESTAMP,
				Long.toString(ignoreExecutionsAfterTimestamp));
		return new TimestampFilter(cfg, analysisController);
	}

	public static void addTimeSeriesFilter(IAnalysisController analysisController,
			FilterPortTuple fpt) throws IllegalStateException, AnalysisConfigurationException{

		final Configuration tsPointAggregatorConfig = new Configuration();
		tsPointAggregatorConfig.setProperty(TimeSeriesPointAggregatorFilter.CONFIG_PROPERTY_NAME_AGGREGATION_SPAN, "1");
		tsPointAggregatorConfig.setProperty(TimeSeriesPointAggregatorFilter.CONFIG_PROPERTY_NAME_AGGREGATION_TIMEUNIT, "NANOSECONDS");
		tsPointAggregatorConfig.setProperty(TimeSeriesPointAggregatorFilter.CONFIG_PROPERTY_NAME_AGGREGATION_METHOD, "MEANJAVA");
		final TimeSeriesPointAggregatorFilter tsPointAggregatorFilter = new TimeSeriesPointAggregatorFilter(tsPointAggregatorConfig, analysisController);

		analysisController.connect(fpt.filter, fpt.port,
				tsPointAggregatorFilter,
				TimeSeriesPointAggregatorFilter.INPUT_PORT_NAME_TSPOINT);
//		analysisController.connect(tsPointAggregatorFilter, TimeSeriesPointAggregatorFilter.OUTPUT_PORT_NAME_AGGREGATED_TSPOINT, uniteMeasurementPairFilter,
//				UniteMeasurementPairFilter.INPUT_PORT_NAME_TSPOINT);
	}

	/**
	 * @deprecated
	 * @param analysisController
	 * @param fpt
	 * @param systemModelRepository
	 * @return
	 * @throws AnalysisConfigurationException
	 */
	public static FilterPortTuple addMagicFilters(
			final IAnalysisController analysisController, FilterPortTuple fpt, SystemModelRepository systemModelRepository)
			throws AnalysisConfigurationException {
	
		// Initialize, register and connect the execution record transformation filter
		final ExecutionRecordTransformationFilter executionRecordTransformationFilter = new ExecutionRecordTransformationFilter(new Configuration(),
				analysisController);
		analysisController.connect(executionRecordTransformationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		analysisController.connect(fpt.filter, fpt.port,
				executionRecordTransformationFilter, ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);

		// Initialize, register and connect the trace reconstruction filter
		final TraceReconstructionFilter traceReconstructionFilter = new TraceReconstructionFilter(new Configuration(), analysisController);
		analysisController.connect(traceReconstructionFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		analysisController.connect(executionRecordTransformationFilter, ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS,
				traceReconstructionFilter, TraceReconstructionFilter.INPUT_PORT_NAME_EXECUTIONS);

		// Initialize, register and connect the session reconstruction filter
		final Configuration bareSessionReconstructionFilterConfiguration = new Configuration();
		bareSessionReconstructionFilterConfiguration.setProperty(SessionReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_THINK_TIME, 
				SessionReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_THINK_TIME);


		final SessionReconstructionFilter sessionReconstructionFilter = new SessionReconstructionFilter(bareSessionReconstructionFilterConfiguration,
				analysisController);
		analysisController.connect(traceReconstructionFilter, TraceReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE,
				sessionReconstructionFilter, SessionReconstructionFilter.INPUT_PORT_NAME_EXECUTION_TRACES);

		// Initialize, register and connect the list collection filter
		final ListCollectionFilter<ExecutionTraceBasedSession> listCollectionFilter = new ListCollectionFilter<ExecutionTraceBasedSession>(new Configuration(),
				analysisController);
		analysisController.connect(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS,
				listCollectionFilter, ListCollectionFilter.INPUT_PORT_NAME);
		
//		final TeeFilter teeFilter = new TeeFilter(new Configuration(), analysisController);
//		analysisController.connect(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS, 
//				teeFilter, TeeFilter.INPUT_PORT_NAME_EVENTS);
		
//		analysisController.connect(fpt.filter, fpt.port, operationDependencyGraphAllocationFilter, OperationDependencyGraphAllocationFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		/**NEUE Controlflow extraction */
		// TeeFilter
//		final Configuration confTeeFilter1 = new Configuration();
//		confTeeFilter1.setProperty(TeeFilter.CONFIG_PROPERTY_NAME_STREAM, TeeFilter.CONFIG_PROPERTY_VALUE_STREAM_STDOUT);
//		final TeeFilter teeFilter1 = new TeeFilter(confTeeFilter1, analysisController);
//
//		// CountingFilter
//		final Configuration confTraceEvents2ExecutionAndMessageTraceFilter = new Configuration();
//		final TraceEventRecords2ExecutionAndMessageTraceFilter traceEvents2ExecutionAndMessageTraceFilter =
//				new TraceEventRecords2ExecutionAndMessageTraceFilter(confTraceEvents2ExecutionAndMessageTraceFilter, analysisController);

//		
//		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, traceRepo);
//		analysisController.connect(
//				eventTraceReconstructionFilter, EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID,
//				traceEvents2ExecutionAndMessageTraceFilter, TraceEventRecords2ExecutionAndMessageTraceFilter.INPUT_PORT_NAME_EVENT_TRACE);
//
//		analysisController.connect(
//				traceEvents2ExecutionAndMessageTraceFilter, TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
//				teeFilter1, TeeFilter.INPUT_PORT_NAME_EVENTS);
//
//		analysisController.connect(sequenceDiagramFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, traceRepo);
//		analysisController.connect(
//				teeFilter1, TeeFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS,
//				sequenceDiagramFilter, AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
//
//		analysisController.connect(componentDependencyGraphAllocationFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, traceRepo);
//		analysisController.connect(
//				teeFilter1, TeeFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS,
//				componentDependencyGraphAllocationFilter, AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
//
//		
//		
//		OperationDependencyGraphAllocationFilter operationDependencyGraphAllocationFilter = new OperationDependencyGraphAllocationFilter(new Configuration(),analysisController);
//		
//		analysisController.connect(operationDependencyGraphAllocationFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
//		analysisController.connect(
//				teeFilter1, TeeFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS,
//				operationDependencyGraphAllocationFilter, AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
//
//		
		return new FilterPortTuple(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS);
	}
	public static String createMethodKey(String signatureName, String componentName){
	//		signatureName = signatureName.replace("<init>", "constructor");
			signatureName = signatureName.replace("<","");
			signatureName = signatureName.replace(">","");
			signatureName = componentName + signatureName;
			return signatureName;
		}
	public static String createSEFFKey(String methodName, String componentName) {
		return componentName + methodName;
	}


}
