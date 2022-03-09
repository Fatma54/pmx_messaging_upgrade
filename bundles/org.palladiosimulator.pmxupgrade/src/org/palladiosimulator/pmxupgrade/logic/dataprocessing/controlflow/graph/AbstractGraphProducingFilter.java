package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.graph.AbstractGraph;
import org.palladiosimulator.pmxupgrade.model.graph.AbstractGraphElement;
import org.palladiosimulator.pmxupgrade.model.graph.IOriginRetentionPolicy;
import org.palladiosimulator.pmxupgrade.model.graph.NoOriginRetentionPolicy;
import org.palladiosimulator.pmxupgrade.model.exception.AnalysisConfigurationException;

/**
 * Abstract superclass for graph-producing filters.
 *
 * @param <G> The graph type created by this filter
 * @author PMX, Universitaet Wuerzburg.
 * @since 1.6
 */
public abstract class AbstractGraphProducingFilter<G extends AbstractGraph<?, ?, ?>> implements
        IGraphProducingFilter<G> {

    private static final String INCOMPATIBLE_RETENTION_ERROR_TEMPLATE =
            "%s: The current retention policy %s is incompatible with the requested retention policy %s.";

    private final G graph;
    private IOriginRetentionPolicy originRetentionPolicy = NoOriginRetentionPolicy.createInstance();

    /**
     * Creates a new graph-producing filter using the given configuration and the given graph.
     *
     * @param graph The (usually empty) graph to produce / extend
     */
    public AbstractGraphProducingFilter(final G graph) {
        this.graph = graph;
    }

    /**
     * Delivers the graph stored in this filter.
     *
     * @return The graph.
     */
    public G getGraph() {
        return this.graph;
    }

    protected IOriginRetentionPolicy getOriginRetentionPolicy() {
        return this.originRetentionPolicy;
    }

    public void requestOriginRetentionPolicy(final IOriginRetentionPolicy policy) throws AnalysisConfigurationException {
        if (!this.originRetentionPolicy.isCompatibleWith(policy)) {
            throw new AnalysisConfigurationException(String.format(INCOMPATIBLE_RETENTION_ERROR_TEMPLATE, this, this.originRetentionPolicy, policy));
        }

        this.originRetentionPolicy = this.originRetentionPolicy.uniteWith(policy);
    }

    protected <T> void handleOrigin(final AbstractGraphElement<T> element, final T origin) {
        this.getOriginRetentionPolicy().handleOrigin(element, origin);
    }

}
