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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.RepositoryPort;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAllocationDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.AbstractGraph;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.TraceInformation;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;
import tools.descartes.pmx.PMXController;
import tools.descartes.pmx.builder.CSVBuilder;
import tools.descartes.pmx.builder.IModelBuilder;
import tools.descartes.pmx.builder.ModelBuilder;
import tools.descartes.pmx.filter.controlflow.CallDecoration;
import tools.descartes.pmx.filter.util.PerformanceModelCreator;
import tools.descartes.pmx.util.ExternalCall;

@Plugin(description = "Transforms the contents of a Kieker SystemModelRepository + graph + resource demands to the model of the builder ", repositoryPorts = { @RepositoryPort(name = AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, repositoryType = SystemModelRepository.class) })
public class PerformanceModelFilter extends AbstractFilterPlugin{ 	//extends AbstractTraceAnalysisFilter {
	private static final Logger log = Logger.getLogger(PerformanceModelFilter.class);
	public static final String INPUT_PORT_NAME_GRAPH = "graph";
	public static final String INPUT_PORT_NAME_OPERATION_GRAPH = "graphs";
	public static final String INPUT_PORT_NAME_RESOURCE_DEMANDS = "resourceDemands";
	public static final String INPUT_PORT_NAME_WORKLOAD = "workload";

	public static final String CONFIG_PROPERTY_NAME_BUILDER = "builder";
	/**
	 * Name of the configuration property to pass the directory of the output.
	 */
	public static final String CONFIG_PROPERTY_NAME_OUTPUT_FN = "outputDirectory";

	/**
	 * By default, writes output files to this file in the working directory.
	 */
	private final String outputDir;
	private final IModelBuilder builder;
	private AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> operationGraph;
	private HashMap<String, Double> resourceDemands; // = new HashMap<String, Double>();
	private HashMap<String, List<Double>> workload; // = new HashMap<String, Vector<Double>>();
	private IProjectContext localProjectContextRef;
	private SystemModelRepository systemEntityFactory;
	private HashMap<String, Integer> numCores = new HashMap<String, Integer>();


//	/**
//	 * Creates a new instance of this class using the given parameters.
//	 * 
//	 * @param configuration
//	 *            The configuration for this component.
//	 * @param projectContext
//	 *            The project context for this component.
//	 */
	public PerformanceModelFilter(final Configuration configuration,
			final IProjectContext projectContext, IModelBuilder builder) {
		super(configuration, projectContext);
		this.localProjectContextRef = projectContext;
		this.outputDir = configuration
				.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN);
		this.builder = builder;
	}
	
	public PerformanceModelFilter(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.localProjectContextRef = projectContext;
		this.outputDir = configuration
				.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN);
		this.builder = PMXController.getModelBuilder();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration currentConfiguration = new Configuration();

		currentConfiguration.setProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN,
				this.outputDir);

		return currentConfiguration;
	}

	@InputPort(name = PerformanceModelFilter.INPUT_PORT_NAME_RESOURCE_DEMANDS, description = "Receives resource demands", eventTypes = { HashMap.class })
	public void inputResourceDemands(
			final HashMap<String, Double> resourceDemands) {
		log.info("received resource demands");
		this.resourceDemands = resourceDemands;
	}

	@InputPort(name = PerformanceModelFilter.INPUT_PORT_NAME_OPERATION_GRAPH, description = "Receives OperationAllocationDependencyGraph", eventTypes = { OperationAllocationDependencyGraph.class })
	public void inputGraph(
			final AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> operationGraph) {
		log.info("received operation graph");
		this.operationGraph = operationGraph;
	}

	@InputPort(name = PerformanceModelFilter.INPUT_PORT_NAME_WORKLOAD, description = "...", eventTypes = { HashMap.class })
	public void inputWorkload(final HashMap<String, List<Double>> workload) {
		log.info("received workload");
		this.workload = workload;
	}


//	@Override
//	public void terminate(final boolean errorBeforeTermination) {
//		for(AbstractPlugin p: this.getIncomingPlugins(false)) {
//			System.out.println("\t\t<--"+p.getName() + " "+p.getState());
//			p.start();
//			p.shutdown(false);
//			System.out.println("\t\t<--"+p.getName() + " "+p.getState());
//		}
//		log.info(this.getName() + "terminated YYYYYYYYYYY");
//		
//	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate(final boolean errorBeforeTermination) {

		log.info("starting model creation "+ "|||||||||||||||||||||||||||||||||||||||||||||||||");
		/**
		 * Used to keep track of whether an error occurred, regardless of
		 * whether before or during termination.
		 */
		boolean error = errorBeforeTermination;
		if (operationGraph == null) {
			log.error("No operation graph has been passed!");
			error = true;
		}
		if (resourceDemands == null) {
			log.error("\tNo resource demand set has been passed!");
			error = true;
		}else if(resourceDemands.keySet().isEmpty()){
			log.warn("\tPassed resource demand set is empty!");
			//error = true;
		}
		if (workload == null) {
			log.error("\tNo workload set has been passed!");
			error = true;
		}else if(workload.keySet().isEmpty()){
			log.error("\tPassed workload is empty!");
			error = true;
		}
		
		if (!error) {
			buildPerformanceModel(operationGraph, resourceDemands, workload,
					this.getSystemEntityFactory(), numCores, builder);
			//super.terminate(errorBeforeTermination);

			//log.info("writing results to files... |||||||||||||||||||||||||||||||||||||||||||||||||");
			builder.saveToFile(outputDir);
		}else{
			//super.terminate(errorBeforeTermination);
			//throw new InternalError();
			//System.exit(0);
		}
	}
	
	public final SystemModelRepository getSystemEntityFactory() {
		if (this.systemEntityFactory == null) {
			this.systemEntityFactory = (SystemModelRepository)
					this.getRepository(AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL);
		}
		if (this.systemEntityFactory == null) {
			LOG.error("Failed to connect to system model repository via repository port '"
					+ AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL + "' (not connected?)");
		}
		return this.systemEntityFactory;
	}

	public void addCPUCoreNumbers(HashMap<String, Integer> numCores) {
		this.numCores = numCores;
	}

	/**
	 * Creates a performance model using a builder + preprocessed monitoring
	 * input
	 * 
	 * @param operationGraph
	 * @param resourceDemands
	 * @param systemModelRepository
	 * @param builder
	 * @param workloadTimeSeriesMap
	 */
	private static void buildPerformanceModel(
			AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> operationGraph,
			HashMap<String, Double> resourceDemands,
			HashMap<String, List<Double>> workload,
			SystemModelRepository systemModelRepository, HashMap<String, Integer> numCores, IModelBuilder builder) {

		PerformanceModelCreator.createExecutionContainers(
				systemModelRepository, builder, numCores);
		PerformanceModelCreator.createComponentsAndInterfaces(
				systemModelRepository, builder);

		for (DependencyGraphNode<AllocationComponentOperationPair> node : operationGraph
				.getVertices()) {
			AssemblyComponent component = node.getEntity()
					.getAllocationComponent().getAssemblyComponent();
			String componentName = ModelBuilder.applyNameFixes(component.getType().getTypeName());
			String hostName = node.getEntity().getAllocationComponent()
					.getExecutionContainer().getName();
			// Teerat Pitakrat: TODO add allocation information to assembly

			if (componentName.equals("'Entry'")) {
				continue; // System entry node
			}
			builder.addAssembly(componentName + ModelBuilder.seperatorChar + hostName);
			builder.addComponentToAssembly(componentName + ModelBuilder.seperatorChar + hostName,
					componentName);

			Operation method = node.getEntity().getOperation();
			String methodName = node.getEntity().getOperation().getSignature()
					.getName();
			CallDecoration decoration = node
					.getDecoration(CallDecoration.class);// ResponseTimeDecoration.class);
			int numIncomingCalls = Integer.parseInt(decoration
					.createFormattedOutput());
			List<ExternalCall> externalCalls = new ArrayList<ExternalCall>();

			log.info("\t" + hostName + " " + componentName + " " + methodName
					+ " " + numIncomingCalls + "x called externally");

			for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outgoingEdge : node
					.getOutgoingEdges()) {
				helperMethod(builder, componentName, hostName, numIncomingCalls,
						externalCalls, outgoingEdge);
			}

			Double meanResourceDemand = resourceDemands.get(componentName + ModelBuilder.seperatorChar
					+ methodName + ModelBuilder.seperatorChar + hostName);
			if (meanResourceDemand == null) {
				log.warn("\t\tNo resource demand found for " + componentName
						+ ModelBuilder.seperatorChar + methodName + ModelBuilder.seperatorChar + hostName);
				log.info(resourceDemands);
				log.info("\t\tresource demand has been set to 0.0");
				meanResourceDemand = 0.0;
			}
			log.info("Try to create behavior description for" + componentName + method.getSignature().getName());
			builder.addSEFF(componentName, method.getSignature().getName(),
					externalCalls, hostName, meanResourceDemand);
		}

		PerformanceModelCreator.createAllocations(systemModelRepository,
				builder);

		Double averageNetworkDelay = resourceDemands.get("Network");
		double throughput = 10000000;
		if (averageNetworkDelay != null) {
			builder.createNetwork(averageNetworkDelay, throughput);
			log.info("\tnetwork");
			log.info("\t\taverageNetworkDelay set to " + averageNetworkDelay
					+ ", throughput set to " + throughput + ".");
			log.warn("\t\tthroughput value is not based on any measurements");
		}

		HashSet<String> names = new HashSet<String>();
		if (workload != null & !workload.keySet().isEmpty()) {
			for (String key : workload.keySet()) {
				String className = key.split(ModelBuilder.seperatorChar)[1];
				names.add(className);
			}
			CSVBuilder.setOutputDirectory(builder.getOutputDirectory() + File.separator + "workloads" + File.separator);
			CSVBuilder.workloadToCSV(workload);
			// CSVBuilder.createInterarrivalCSVs(workload);
			// CSVBuilder.createCombinedCSV(workload);
			// CSVBuilder.createPetClinicCSVs(workload);
			//log.info(names);
			builder.addUsageScenario(workload);
		}
	}


	private static void helperMethod(
			IModelBuilder builder,
			String componentName,
			String hostName,
			int numIncomingCalls,
			List<ExternalCall> externalCalls,
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outgoingEdge) {
		AllocationComponentOperationPair targetNode = outgoingEdge.getTarget()
				.getEntity();
		String targetComponentName = ModelBuilder.applyNameFixes(targetNode.getAllocationComponent()
				.getAssemblyComponent().getType().getTypeName());
		String calledMethodName = targetNode.getOperation().getSignature()
				.getName();
		String targetHostName = targetNode.getAllocationComponent()
				.getExecutionContainer().getName();

		// if(targetComponentName != componentName){
		builder.addProvidedRole(targetComponentName, "I" + targetComponentName);
		builder.addRequiredRole(componentName, "I" + targetComponentName);
		// }
		int numOutgoinCalls = outgoingEdge.getTargetWeight().intValue(); // TODO
																			// think
																			// about
																			// adding
																			// more
																			// TraceInformation
																			// to
																			// graph

		double averageCalls = numOutgoinCalls / (double) numIncomingCalls;
		ExternalCall call = new ExternalCall(targetComponentName,
				calledMethodName, averageCalls);
		externalCalls.add(call);

		log.info("\t\tcalls on average " + call.getNumCalls() + "x "
				+ targetComponentName + " " + call.getMethodName());

		builder.addAssembly(targetComponentName + ModelBuilder.seperatorChar + targetHostName);
		builder.addComponentToAssembly(targetComponentName + ModelBuilder.seperatorChar
				+ targetHostName, targetComponentName);

		// if(targetComponentName != componentName){
		builder.addConnectionToAssemblies(componentName + ModelBuilder.seperatorChar + hostName,
				targetComponentName + ModelBuilder.seperatorChar + targetHostName);
		// }
	}
}
