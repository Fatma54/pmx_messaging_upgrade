package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * Generic superclass for all graph edges in the visualization package.
 *
 * @param <V> The type of the graph's vertices
 * @param <E> The type of the graph's edges
 * @param <O> The type of object from which the graph's elements originate
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractEdge<V extends AbstractVertex<V, E, O>, E extends AbstractEdge<V, E, O>, O> extends AbstractGraphElement<O> {

    private final V source;
    private final V target;

    /**
     * Creates a new edge between the given vertices.
     *
     * @param source       The source vertex of the edge
     * @param target       The target vertex of the edge
     * @param origin       The origin of the edge
     * @param originPolicy The origin policy to use
     */
    public AbstractEdge(final V source, final V target, final O origin, final IOriginRetentionPolicy originPolicy) {
        super(origin, originPolicy);
        this.source = source;
        this.target = target;
    }

    /**
     * Returns the source of this edge.
     *
     * @return See above
     */
    public V getSource() {
        return this.source;
    }

    /**
     * Returns the target of this edge.
     *
     * @return See above
     */
    public V getTarget() {
        return this.target;
    }

    @Override
    public String getIdentifier() { // NOPMD ( EmptyMethodInAbstractClassShouldBeAbstract)
        return null;
    }
}
