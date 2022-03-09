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
package tools.descartes.pmx.filter.controlflow;

import java.io.IOException;

import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.filter.traceWriter.MessageTraceWriterFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentDependencyGraphAllocationFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationDependencyGraphAllocationFilter;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.builder.IModelBuilder;
import tools.descartes.pmx.util.FilterPortTuple;

public class ControlFlowFilterAppender {

	public static void addControlFlowFilter(
			IAnalysisController analysisController,
			SystemModelRepository systemModelRepository, String resultPath,
			FilterPortTuple traceEvents2ExecutionAndMessageTraceFilter,
			FilterPortTuple fpt2,
			IModelBuilder builder) throws IllegalStateException,
			AnalysisConfigurationException, IOException {

		testMessageTraceWriter(analysisController, systemModelRepository,
				traceEvents2ExecutionAndMessageTraceFilter, resultPath);

		addComponentDependencyGraphFilter(analysisController,
				systemModelRepository,
				traceEvents2ExecutionAndMessageTraceFilter, fpt2, builder, resultPath);
		// addOperationDependencyGraphFilter(analysisController,
		// systemModelRepository,
		// traceEvents2ExecutionAndMessageTraceFilter, fpt2);
	}

	private static void addComponentDependencyGraphFilter(
			IAnalysisController analysisController,
			SystemModelRepository systemModelRepository,
			FilterPortTuple traceEvents2ExecutionAndMessageTraceFilter,
			FilterPortTuple fpt2,
			IModelBuilder builder, String resultPath)
			throws IllegalStateException, AnalysisConfigurationException {

		final ComponentDependencyGraphAllocationFilter componentDependencyGraphAllocationFilter = new ComponentDependencyGraphAllocationFilter(
				new Configuration(), analysisController);
		analysisController.connect(componentDependencyGraphAllocationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);
		analysisController
				.connect(
						traceEvents2ExecutionAndMessageTraceFilter.filter,
						TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
						componentDependencyGraphAllocationFilter,
						AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		Configuration myControlFlowFilterConfig = new Configuration();
		myControlFlowFilterConfig.setProperty(
				MyControlFlowFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN, resultPath
						+ "graphOLD.txt");
		MyControlFlowFilter myControlFlowFilter = new MyControlFlowFilter(
				myControlFlowFilterConfig, analysisController, builder);
		analysisController
				.connect(
						componentDependencyGraphAllocationFilter,
						OperationDependencyGraphAllocationFilter.OUTPUT_PORT_NAME_GRAPH,
						myControlFlowFilter,
						MyControlFlowFilter.INPUT_PORT_NAME_GRAPH);
		analysisController
				.connect(
						fpt2.filter,
						TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
						componentDependencyGraphAllocationFilter,
						OperationDependencyGraphAllocationFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(myControlFlowFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);

	}

	private static void testMessageTraceWriter(
			IAnalysisController analysisController,
			SystemModelRepository systemModelRepository,
			FilterPortTuple traceEvents2ExecutionAndMessageTraceFilter,
			String resultPath) throws IOException, IllegalStateException,
			AnalysisConfigurationException {
		Configuration messageTraceWriterFilterConfig = new Configuration();
		messageTraceWriterFilterConfig.setProperty(
				MessageTraceWriterFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN,
				resultPath + "messageTraces-yyyyMMdd-HHmmssSSS.txt");
		MessageTraceWriterFilter messageTraceWriterFilter = new MessageTraceWriterFilter(
				messageTraceWriterFilterConfig, analysisController);

		analysisController
				.connect(
						traceEvents2ExecutionAndMessageTraceFilter.filter,
						TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
						messageTraceWriterFilter,
						AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		// MessageTraceWriterFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		analysisController.connect(messageTraceWriterFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);

	}

	/**
	 * Requires message traces
	 * 
	 * @param analysisController
	 * @param systemModelRepository
	 * @param traceEvents2ExecutionAndMessageTraceFilter
	 * @throws IllegalStateException
	 * @throws AnalysisConfigurationException
	 */
	private static void addOperationDependencyGraphFilter(
			IAnalysisController analysisController,
			SystemModelRepository systemModelRepository,
			FilterPortTuple traceEvents2ExecutionAndMessageTraceFilter,
			FilterPortTuple fpt2) throws IllegalStateException,
			AnalysisConfigurationException {
		final OperationDependencyGraphAllocationFilter operationDependencyGraphAllocationFilter = new OperationDependencyGraphAllocationFilter(
				new Configuration(), analysisController);
		analysisController.connect(operationDependencyGraphAllocationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
				systemModelRepository);
		analysisController
				.connect(
						traceEvents2ExecutionAndMessageTraceFilter.filter,
						TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
						operationDependencyGraphAllocationFilter,
						AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		analysisController
				.connect(
						fpt2.filter,
						TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
						operationDependencyGraphAllocationFilter,
						OperationDependencyGraphAllocationFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		MyControlFlowFilter2 myControlFlowFilter2 = new MyControlFlowFilter2(
				new Configuration(), analysisController);
		analysisController
				.connect(
						operationDependencyGraphAllocationFilter,
						OperationDependencyGraphAllocationFilter.OUTPUT_PORT_NAME_GRAPH,
						myControlFlowFilter2,
						MyControlFlowFilter.INPUT_PORT_NAME_GRAPH);

	}

	// // Initialize, register and connect the execution record transformation
	// filter
	// final ExecutionRecordTransformationFilter
	// executionRecordTransformationFilter = new
	// ExecutionRecordTransformationFilter(new Configuration(),
	// analysisController);
	// analysisController.connect(executionRecordTransformationFilter,
	// AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
	// systemModelRepository);
	// analysisController.connect(fpt.filter, fpt.port,
	// executionRecordTransformationFilter,
	// ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);
	//
	//
	// // Initialize, register and connect the session reconstruction filter
	// final Configuration bareSessionReconstructionFilterConfiguration = new
	// Configuration();
	// bareSessionReconstructionFilterConfiguration.setProperty(SessionReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_THINK_TIME,
	// SessionReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_THINK_TIME);
	//
	//
	// final SessionReconstructionFilter sessionReconstructionFilter = new
	// SessionReconstructionFilter(bareSessionReconstructionFilterConfiguration,
	// analysisController);
	// analysisController.connect(traceReconstructionFilter,
	// TraceReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE,
	// sessionReconstructionFilter,
	// SessionReconstructionFilter.INPUT_PORT_NAME_EXECUTION_TRACES);
	//
	// // Initialize, register and connect the list collection filter
	// final ListCollectionFilter<ExecutionTraceBasedSession>
	// listCollectionFilter = new
	// ListCollectionFilter<ExecutionTraceBasedSession>(new Configuration(),
	// analysisController);
	// analysisController.connect(sessionReconstructionFilter,
	// SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS,
	// listCollectionFilter, ListCollectionFilter.INPUT_PORT_NAME);

}
