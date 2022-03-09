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
package tools.descartes.pmx.filter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import kieker.analysis.IAnalysisController;
import kieker.analysis.analysisComponent.AbstractAnalysisComponent;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.AbstractPlugin;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.forward.StringBufferFilter;
import kieker.analysis.plugin.filter.select.TimestampFilter;
import kieker.analysis.plugin.filter.select.TraceIdFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.Constants;
import kieker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.flow.EventRecordTraceCounter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.filter.systemModel.SystemModel2FileFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentDependencyGraphAllocationFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationDependencyGraphAllocationFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ResponseTimeNodeDecorator;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.filter.controlflow.CallNodeDecorator;
import tools.descartes.pmx.filter.resourcedemands.CPUFilter;
import tools.descartes.pmx.filter.resourcedemands.ResourceDemandFilter;
import tools.descartes.pmx.util.FilterPortTuple;

public class PerformanceModelFilterAppender {
	private static final Logger log = Logger.getLogger(PerformanceModelFilterAppender.class);

	/**
	 * Builds a performance model based on the monitoring data and passed
	 * builder
	 * 
	 * @param systemModelRepository
	 * @param reader_fpt
	 * @param analysisController
	 * @param resultPath
	 * @param builder
	 * @throws AnalysisConfigurationException
	 */
	public static void initAndConnectFilters(final SystemModelRepository systemModelRepository, AbstractPlugin reader,
			String port, IAnalysisController analysisController, String resultPath,
			long ignoreExecutionsBeforeTimestamp, long ignoreExecutionsAfterTimestamp, boolean isReduced,
			HashMap<String, Integer> numCores)
			throws AnalysisConfigurationException {

		TraceReconstructionFilter mtReconstrFilter = null;
		EventRecordTraceCounter eventRecordTraceCounter = null;
		EventRecordTraceReconstructionFilter eventTraceReconstructionFilter = null;
		TraceEventRecords2ExecutionAndMessageTraceFilter traceEvents2ExecutionAndMessageTraceFilter = null;

		// Unify Strings
		final StringBufferFilter stringBufferFilter = new StringBufferFilter(new Configuration(), analysisController);
		analysisController.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, stringBufferFilter,
				StringBufferFilter.INPUT_PORT_NAME_EVENTS);

		// This map can be used within the constructor for all following plugins
		// which use the repository with the name defined in the
		// AbstractTraceAnalysisPlugin.
		final TimestampFilter timestampFilter;
		{ // NOCS (nested block)
			// Create the timestamp filter and connect to the reader's output
			// port
			final Configuration configTimestampFilter = new Configuration();
			configTimestampFilter.setProperty(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_BEFORE_TIMESTAMP,
					Long.toString(ignoreExecutionsBeforeTimestamp));
			configTimestampFilter.setProperty(TimestampFilter.CONFIG_PROPERTY_NAME_IGNORE_AFTER_TIMESTAMP,
					Long.toString(ignoreExecutionsAfterTimestamp));

			timestampFilter = new TimestampFilter(configTimestampFilter, analysisController);
			analysisController.connect(stringBufferFilter, StringBufferFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS,
					timestampFilter, TimestampFilter.INPUT_PORT_NAME_EXECUTION);
			analysisController.connect(stringBufferFilter, StringBufferFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS,
					timestampFilter, TimestampFilter.INPUT_PORT_NAME_FLOW);
		}

		final TraceIdFilter traceIdFilter;
		{ // NOCS (nested block)
			// Create the trace ID filter and connect to the timestamp filter's
			// output port
			final Configuration configTraceIdFilterFlow = new Configuration();
			configTraceIdFilterFlow.setProperty(TraceIdFilter.CONFIG_PROPERTY_NAME_SELECT_ALL_TRACES,
					Boolean.TRUE.toString());

			traceIdFilter = new TraceIdFilter(configTraceIdFilterFlow, analysisController);

			analysisController.connect(timestampFilter, TimestampFilter.OUTPUT_PORT_NAME_WITHIN_PERIOD, traceIdFilter,
					TraceIdFilter.INPUT_PORT_NAME_COMBINED);
		}

		final ExecutionRecordTransformationFilter execRecTransformer;
		{ // NOCS (nested block)
			// Create the execution record transformation filter and connect to
			// the trace ID filter's output port
			final Configuration execRecTransformerConfig = new Configuration();
			execRecTransformerConfig.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
					Constants.EXEC_TRACE_RECONSTR_COMPONENT_NAME);
			execRecTransformer = new ExecutionRecordTransformationFilter(execRecTransformerConfig, analysisController);
			analysisController.connect(traceIdFilter, TraceIdFilter.OUTPUT_PORT_NAME_MATCH, execRecTransformer,
					ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);
			analysisController.connect(execRecTransformer,
					AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		}

		{ // NOCS (nested block)
			// Create the trace reconstruction filter and connect to the record
			// transformation filter's output port
			final Configuration mtReconstrFilterConfig = new Configuration();
			mtReconstrFilterConfig.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
					Constants.TRACERECONSTR_COMPONENT_NAME);
			mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_TIMEUNIT,
					TimeUnit.MILLISECONDS.name());
			// mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION,
			// Integer.toString(maxTraceDurationMillis));
			mtReconstrFilterConfig.setProperty(TraceReconstructionFilter.CONFIG_PROPERTY_NAME_IGNORE_INVALID_TRACES,
					Boolean.toString(true));
			mtReconstrFilter = new TraceReconstructionFilter(mtReconstrFilterConfig, analysisController);
			analysisController.connect(mtReconstrFilter, AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL,
					systemModelRepository);
			analysisController.connect(execRecTransformer,
					ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS, mtReconstrFilter,
					TraceReconstructionFilter.INPUT_PORT_NAME_EXECUTIONS);
		}

		{ // NOCS (nested block)
			// Create the event record trace generation filter and connect to
			// the trace ID filter's output port
			final Configuration configurationEventRecordTraceGenerationFilter = new Configuration();
			configurationEventRecordTraceGenerationFilter.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
					Constants.EVENTRECORDTRACERECONSTR_COMPONENT_NAME);
			configurationEventRecordTraceGenerationFilter.setProperty(
					EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_TIMEUNIT, TimeUnit.MILLISECONDS.name());
			// configurationEventRecordTraceGenerationFilter.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION,
			// Long.toString(maxTraceDurationMillis));
			configurationEventRecordTraceGenerationFilter.setProperty(
					EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_REPAIR_EVENT_BASED_TRACES,
					Boolean.toString(true));
			eventTraceReconstructionFilter = new EventRecordTraceReconstructionFilter(
					configurationEventRecordTraceGenerationFilter, analysisController);

			analysisController.connect(traceIdFilter, TraceIdFilter.OUTPUT_PORT_NAME_MATCH,
					eventTraceReconstructionFilter, EventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);
		}

		{ // NOCS (nested block)
			// Create the counter for valid/invalid event record traces
			final Configuration configurationEventRecordTraceCounter = new Configuration();
			configurationEventRecordTraceCounter.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
					Constants.EXECEVENTRACESFROMEVENTTRACES_COMPONENT_NAME);
			configurationEventRecordTraceCounter.setProperty(EventRecordTraceCounter.CONFIG_PROPERTY_NAME_LOG_INVALID,
					Boolean.toString(false));
			eventRecordTraceCounter = new EventRecordTraceCounter(configurationEventRecordTraceCounter,
					analysisController);

			analysisController.connect(eventTraceReconstructionFilter,
					EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID, eventRecordTraceCounter,
					EventRecordTraceCounter.INPUT_PORT_NAME_VALID);
			analysisController.connect(eventTraceReconstructionFilter,
					EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_INVALID, eventRecordTraceCounter,
					EventRecordTraceCounter.INPUT_PORT_NAME_INVALID);
		}

		{ // NOCS (nested block)
			// Create the event trace to execution/message trace transformation
			// filter and connect its input to the event record trace generation
			// filter's output
			// port
			final Configuration configurationEventTrace2ExecutionTraceFilter = new Configuration();
			configurationEventTrace2ExecutionTraceFilter.setProperty(AbstractAnalysisComponent.CONFIG_NAME,
					Constants.EXECTRACESFROMEVENTTRACES_COMPONENT_NAME);
			// configurationEventTrace2ExecutionTraceFilter.setProperty(TraceEventRecords2ExecutionAndMessageTraceFilter.CONFIG_IGNORE_ASSUMED,
			// Boolean.toString(ignoreAssumedCalls));
			// EventTrace2ExecutionTraceFilter has no configuration properties
			traceEvents2ExecutionAndMessageTraceFilter = new TraceEventRecords2ExecutionAndMessageTraceFilter(
					configurationEventTrace2ExecutionTraceFilter, analysisController);

			analysisController.connect(eventTraceReconstructionFilter,
					EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID,
					traceEvents2ExecutionAndMessageTraceFilter,
					TraceEventRecords2ExecutionAndMessageTraceFilter.INPUT_PORT_NAME_EVENT_TRACE);
			analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter,
					AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		}

		/*
		 * Creates the filter that actually build the models and connects
		 * necessary filters to it.
		 */

		CPUFilter cpuFilter = new CPUFilter(new Configuration(), analysisController);

		// performance model filter
		Configuration performanceModelFilterConfiguration = new Configuration();
		performanceModelFilterConfiguration.setProperty(PerformanceModelFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN,
				resultPath);
		PerformanceModelFilter performanceModelFilter = new PerformanceModelFilter(performanceModelFilterConfiguration,
				analysisController);
		performanceModelFilter.addCPUCoreNumbers(numCores);

		// workload filter
		WorkloadFilter workloadFilter = new WorkloadFilter(new Configuration(), analysisController);
		analysisController.connect(mtReconstrFilter, TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				workloadFilter, AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter,
				TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE, workloadFilter,
				AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		// graph filter
		OperationDependencyGraphAllocationFilter operationDependencyGraphAllocationFilter = new OperationDependencyGraphAllocationFilter(
				new Configuration(), analysisController);
		operationDependencyGraphAllocationFilter.addDecorator(new ResponseTimeNodeDecorator(TimeUnit.NANOSECONDS));
		operationDependencyGraphAllocationFilter.addDecorator(new CallNodeDecorator());
		analysisController.connect(mtReconstrFilter, TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				operationDependencyGraphAllocationFilter,
				AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter,
				TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				operationDependencyGraphAllocationFilter,
				AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(operationDependencyGraphAllocationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);

		// resource demands filter
		final Configuration resourceDemandFilterConfiguration = new Configuration();
		resourceDemandFilterConfiguration.setProperty(ResourceDemandFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN,
				resultPath + File.separator + "resourcedemands");
		(new File(resultPath + File.separator + "resourcedemands" + File.separator)).mkdirs();

		ResourceDemandFilter resourceDemandFilter = new ResourceDemandFilter(resourceDemandFilterConfiguration,
				analysisController);
		if (numCores != null) {
			for (String host : numCores.keySet()) {
				resourceDemandFilter.addCPUCoreNumber(host, numCores.get(host));
			}
		}
		analysisController.connect(reader, port, cpuFilter, CPUFilter.INPUT_PORT_NAME);
		analysisController.connect(cpuFilter, CPUFilter.OUTPUT_PORT_NAME_UTILIZATION, resourceDemandFilter,
				ResourceDemandFilter.INPUT_PORT_NAME_UTILIZATION);
		analysisController.connect(mtReconstrFilter, TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				resourceDemandFilter, AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter,
				TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE, resourceDemandFilter,
				AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		analysisController.connect(workloadFilter, WorkloadFilter.OUTPUT_PORT_NAME_WORKLOAD, performanceModelFilter,
				PerformanceModelFilter.INPUT_PORT_NAME_WORKLOAD);

		analysisController.connect(resourceDemandFilter, ResourceDemandFilter.OUTPUT_PORT_NAME_DEMANDS,
				performanceModelFilter, PerformanceModelFilter.INPUT_PORT_NAME_RESOURCE_DEMANDS);
		analysisController.connect(operationDependencyGraphAllocationFilter,
				OperationDependencyGraphAllocationFilter.OUTPUT_PORT_NAME_GRAPH, performanceModelFilter,
				PerformanceModelFilter.INPUT_PORT_NAME_OPERATION_GRAPH);

		analysisController.connect(performanceModelFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);

		// Produces nice to have HTML output.
		PerformanceModelFilterAppender.addSystem2HTMLFilter(systemModelRepository, analysisController, resultPath);

	}

	public static void addSystem2HTMLFilter(SystemModelRepository systemModelRepository,
			IAnalysisController analysisController, String resultPath)
			throws IllegalStateException, AnalysisConfigurationException {
		final String systemEntitiesHtmlFn;
		if (new File(resultPath).isDirectory()) {
			systemEntitiesHtmlFn = resultPath + File.separator + "system.html";
		} else {
			systemEntitiesHtmlFn = "system.html";
		}

		File file = new File(resultPath + File.separator);
		file.mkdirs();

		Configuration systemModel2FileFilterConfig = new Configuration();

		systemModel2FileFilterConfig.setProperty(SystemModel2FileFilter.CONFIG_PROPERTY_NAME_HTML_OUTPUT_FN,
				new File(systemEntitiesHtmlFn).toString());
		log.info("writing additional html information to" + systemModel2FileFilterConfig
				.getPathProperty(SystemModel2FileFilter.CONFIG_PROPERTY_NAME_HTML_OUTPUT_FN));

		SystemModel2FileFilter systemModel2FileFilter = new SystemModel2FileFilter(systemModel2FileFilterConfig,
				analysisController);

		analysisController.connect(systemModel2FileFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
	}

	private static void addComponentDependencyGraphAllocationFilter(IAnalysisController analysisController,
			SystemModelRepository systemModelRepository, String resultPath,
			PerformanceModelFilter performanceModelFilter, FilterPortTuple traceEvents2ExecutionAndMessageTraceFilter,
			FilterPortTuple traceReconstructionFilter)
			throws IllegalStateException, AnalysisConfigurationException, IOException {

		final ComponentDependencyGraphAllocationFilter componentDependencyGraphAllocationFilter = new ComponentDependencyGraphAllocationFilter(
				new Configuration(), analysisController);
		analysisController.connect(componentDependencyGraphAllocationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);

		analysisController.connect(traceEvents2ExecutionAndMessageTraceFilter.filter,
				TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				componentDependencyGraphAllocationFilter,
				AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES);
		analysisController.connect(traceReconstructionFilter.filter,
				TraceReconstructionFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE, componentDependencyGraphAllocationFilter,
				ComponentDependencyGraphAllocationFilter.INPUT_PORT_NAME_MESSAGE_TRACES);

		analysisController.connect(componentDependencyGraphAllocationFilter,
				ComponentDependencyGraphAllocationFilter.OUTPUT_PORT_NAME_GRAPH, performanceModelFilter,
				PerformanceModelFilter.INPUT_PORT_NAME_GRAPH);
	}

}
