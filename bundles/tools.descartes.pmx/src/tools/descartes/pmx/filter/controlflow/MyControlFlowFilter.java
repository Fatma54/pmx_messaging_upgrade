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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.annotation.RepositoryPort;
import kieker.common.configuration.Configuration;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.AbstractGraph;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.TraceInformation;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.builder.IModelBuilder;

/**
 * Takes {@link OperationExecutionRecord} objects an creates PCM control
 * 
 * @author J�rgen Walter
 */
@Plugin(description = "Transforms the contents of a Kieker SystemModelRepository and XXX grap to YYY", repositoryPorts = { @RepositoryPort(name = AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, repositoryType = SystemModelRepository.class) },

configuration = { @Property(name = MyControlFlowFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN, defaultValue = MyControlFlowFilter.DEFAULT_HTML_OUTPUT_FN), 

		
	@Property(name = MyControlFlowFilter.CONFIG_PROPERTY_NAME_BUILDER, defaultValue = "")}
)
public class MyControlFlowFilter extends AbstractTraceAnalysisFilter { // AbstractFilterPlugin
																		// {

	/** The name of the input port accepting execution records. */
	public static final String INPUT_PORT_NAME_GRAPH = "graph";

	// public static final String OUTPUT_PORT_NAME_COMPONENT = "component";

	public static final String CONFIG_PROPERTY_NAME_OUTPUT_FN = "outputFn";
	public static final String CONFIG_PROPERTY_NAME_BUILDER = "builder";

	/**
	 * By default, writes output file to this file in the working directory.
	 */
	protected static final String DEFAULT_HTML_OUTPUT_FN = "graph.txt";

	private final String outputFn;
	private final IModelBuilder builder;

	public MyControlFlowFilter(final Configuration configuration,
			final IProjectContext projectContext, IModelBuilder builder) {
		super(configuration, projectContext);
		this.outputFn = configuration
				.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN);
		//this.builder = configuration.getProperty(CONFIG_PROPERTY_NAME_BUILDER);
		this.builder = builder;
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return configuration;
	}

	@InputPort(name = INPUT_PORT_NAME_GRAPH, description = "Receives abstract grap", eventTypes = { AbstractGraph.class })
	public void inputOperationExecutionRecord(
			final AbstractGraph<DependencyGraphNode<AllocationComponent>, WeightedBidirectionalDependencyGraphEdge<AllocationComponent>, TraceInformation> graph) {

		final SystemModelRepository systemModelRepository = super
				.getSystemEntityFactory();
		if (systemModelRepository == null) {
			final String errorMsg = "Failed to get system model repository";
			this.log.error(errorMsg);

		}

		log.info("WORKING GRAPH "+ graph.getVertices().size());
		
		doWork(graph, systemModelRepository);
	}

	private void doWork(
			final AbstractGraph<DependencyGraphNode<AllocationComponent>, WeightedBidirectionalDependencyGraphEdge<AllocationComponent>, TraceInformation> graph,
			SystemModelRepository systemModelRepository) {
		Writer fw = null;

		try {
//			log.info("Logging graph to " + outputFn);
			fw = new FileWriter(outputFn);
			fw.append("AllocationComponentInstances ");
			for (AllocationComponent ac : systemModelRepository
					.getAllocationFactory().getAllocationComponentInstances()) {
				fw.append("\n " + ac.getIdentifier() + " "
						+ "(assembly: "+ac.getAssemblyComponent().getIdentifier()+") "
						+ "(execContainer: "+ac.getExecutionContainer().getIdentifier()+")");

			}
			fw.append("\n");
			fw.append("AssemblyComponentInstances ");
			for (AssemblyComponent ac : systemModelRepository
					.getAssemblyFactory().getAssemblyComponentInstances()) {
				fw.append("\n " + ac.getIdentifier()); // + " "					+ ac.getName());

			}
			fw.append("\n");

			for (DependencyGraphNode<AllocationComponent> node : graph
					.getVertices()) {
				
				//AllocationComponent allocationComponent = node.getEntity();
				fw.append("Node: " + node.getIdentifier() + "\t");
				fw.append("\n");
				for (WeightedBidirectionalDependencyGraphEdge<AllocationComponent> outgoingEdge : node
						.getOutgoingEdges()) {
					
					String start = node.getEntity().getAssemblyComponent().getType().getTypeName();
					String end = outgoingEdge.getTarget().getEntity().getAssemblyComponent().getType().getTypeName();
//					builder.connectAssemblies(start, end);	//TODO
//					log.info(start + " --> "+ end);
					fw.append("\t" + outgoingEdge.getTarget().getIdentifier() + "(weight="+outgoingEdge.getWeight()+")");
				}
				fw.append("\n");
			}
		} catch (IOException e) {
			log.error("Could not write file "+outputFn);
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
