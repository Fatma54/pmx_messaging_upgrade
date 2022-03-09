package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.graph.AbstractWeightedEdge;
import org.palladiosimulator.pmxupgrade.model.graph.IOriginRetentionPolicy;
import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.TraceInformation;

/**
 * This class represents a weighted but bidirected edge within a dependency graph.
 * 
 * @param <T>
 *            The type of the entity stored in the nodes linked by this edge.
 * 
 * @author PMX, Universitaet Wuerzburg.
 * 
 * @since 1.1
 */
public class WeightedBidirectionalDependencyGraphEdge<T extends ISystemModelElement> extends
		AbstractWeightedEdge<DependencyGraphNode<T>, WeightedBidirectionalDependencyGraphEdge<T>, TraceInformation> {

	private boolean assumed; // false

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param source
	 *            The source of this edge.
	 * @param target
	 *            The target of this edge.
	 * @param origin
	 *            The meta information for this edge.
	 * @param originPolicy
	 *            The origin policy.
	 */
	public WeightedBidirectionalDependencyGraphEdge(final DependencyGraphNode<T> source, final DependencyGraphNode<T> target, final TraceInformation origin,
			final IOriginRetentionPolicy originPolicy) {
		super(source, target, origin, originPolicy);
	}

	public boolean isAssumed() {
		return this.assumed;
	}

	/**
	 * Sets the assumed flag to {@code true}.
	 */
	public void setAssumed() {
		this.assumed = true;
	}

}
