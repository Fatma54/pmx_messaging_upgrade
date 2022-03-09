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

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.AbstractGraph;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.TraceInformation;

/**
 * Takes {@link OperationExecutionRecord} objects an creates PCM control
 *  
 * @author J�rgen Walter
 */
public class MyControlFlowFilter2 extends AbstractFilterPlugin {
	public MyControlFlowFilter2(Configuration configuration,
			IProjectContext projectContext) {
		super(configuration, projectContext);
	}
	/** The name of the input port accepting execution records. */
	public static final String INPUT_PORT_NAME_GRAPH = "graph";

	public static final String OUTPUT_PORT_NAME_COMPONENT = "component";

	@Override
	public Configuration getCurrentConfiguration() {
		return configuration;
	}
	
	@InputPort(name = INPUT_PORT_NAME_GRAPH, description = "Receives execution events to be selected by trace ID", eventTypes = { AbstractGraph.class })
	public void inputOperationExecutionRecord(
		final AbstractGraph<DependencyGraphNode<AllocationComponent>, WeightedBidirectionalDependencyGraphEdge<AllocationComponent>, TraceInformation> graph){

		log.info("graph ");
		for(DependencyGraphNode<AllocationComponent> vertex: graph.getVertices()){
			log.info("\t"+vertex.getIdentifier()+ "\tclass:"+ vertex.getClass() +" descr:"+ vertex.getDescription() + vertex.getOutgoingEdges() +" | "+ vertex.getOutgoingDependencies());
		}
	}
}
