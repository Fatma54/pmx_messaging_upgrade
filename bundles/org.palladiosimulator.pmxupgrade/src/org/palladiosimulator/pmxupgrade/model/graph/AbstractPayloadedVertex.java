package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * Abstract superclass for "payloaded" vertices, i.e. vertices with attached objects.
 *
 * @param <V> The type of the graph's vertices
 * @param <E> The type of the graph's edges
 * @param <O> The type of the graph's elements origin
 * @param <P> The type of the payload
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractPayloadedVertex<V extends AbstractPayloadedVertex<V, E, O, P>, E extends AbstractEdge<V, E, O>, O, P> extends AbstractVertex<V, E, O> {

    private final P payload;

    /**
     * Creates a new vertex with the given origin and payload.
     *
     * @param origin       The vertex' origin
     * @param originPolicy The origin policy.
     * @param payload      The vertex' payload
     */
    protected AbstractPayloadedVertex(final O origin, final IOriginRetentionPolicy originPolicy, final P payload) {
        super(origin, originPolicy);
        this.payload = payload;
    }

    /**
     * Returns this vertex' payload.
     *
     * @return See above
     */
    public P getPayload() {
        return this.payload;
    }
}
