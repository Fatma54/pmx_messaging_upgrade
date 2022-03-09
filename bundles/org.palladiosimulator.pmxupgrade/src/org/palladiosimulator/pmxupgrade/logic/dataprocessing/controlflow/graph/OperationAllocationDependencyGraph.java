
package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;

/**
 * This class represents operation dependency graphs on the allocation level.
 * 
 * @author PMX, Universitaet Wuerzburg.
 * 
 * @since 1.6
 */
public class OperationAllocationDependencyGraph extends AbstractDependencyGraph<AllocationComponentOperationPair> {

	/**
	 * Creates a new graph with the given root entity.
	 * 
	 * @param rootEntity
	 *            The root entity to use for this graph
	 */
	public OperationAllocationDependencyGraph(final AllocationComponentOperationPair rootEntity) {
		super(rootEntity);
	}

}
