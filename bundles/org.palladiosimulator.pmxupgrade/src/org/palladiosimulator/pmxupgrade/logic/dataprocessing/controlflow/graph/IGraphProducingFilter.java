package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;


import org.palladiosimulator.pmxupgrade.model.graph.AbstractGraph;
import org.palladiosimulator.pmxupgrade.model.graph.IOriginRetentionPolicy;
import org.palladiosimulator.pmxupgrade.model.exception.AnalysisConfigurationException;

/**
 * Interface for graph-producing filters.
 * 
 * @author PMX, Universitaet Wuerzburg.
 * 
 * @param <G>
 *            The type of the produced graph
 * 
 * @since 1.6
 */
public interface IGraphProducingFilter<G extends AbstractGraph<?, ?, ?>> {

	void requestOriginRetentionPolicy(IOriginRetentionPolicy policy) throws AnalysisConfigurationException;

}
