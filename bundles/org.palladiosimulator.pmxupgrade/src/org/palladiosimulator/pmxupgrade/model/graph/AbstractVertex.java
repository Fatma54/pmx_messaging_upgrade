package org.palladiosimulator.pmxupgrade.model.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic superclass for all vertices in the visualization package.
 *
 * @param <V> The type of the graph's vertices
 * @param <E> The type of the graph's edges
 * @param <O> The type of object from which the graph's elements originate
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractVertex<V extends AbstractVertex<V, E, O>, E extends AbstractEdge<V, E, O>, O> extends AbstractGraphElement<O> {

    private final Map<Class<? extends AbstractVertexDecoration>, AbstractVertexDecoration> decorations = new ConcurrentHashMap<>(); // NOPMD(UseConcurrentHashMap)//NOCS

    protected AbstractVertex(final O origin, final IOriginRetentionPolicy originPolicy) {
        super(origin, originPolicy);
    }

    /**
     * Returns the outgoing edges of this vertex.
     *
     * @return See above
     */
    public abstract Collection<E> getOutgoingEdges();

    /**
     * Returns the decoration of this vertex of the given type.
     *
     * @param type The type of the desired decoration
     * @return The given decoration or {@code null} if no such type exists
     */
    @SuppressWarnings("unchecked")
    public <DecorationT extends AbstractVertexDecoration> DecorationT getDecoration(final Class<DecorationT> type) { // NOCS (DecorationT istaed of T)
        return (DecorationT) this.decorations.get(type);
    }

    /**
     * Adds a decoration to this vertex. Note that the given decoration may replace an existing
     * decoration of the same type.
     *
     * @param decoration The decoration to add
     */
    public void addDecoration(final AbstractVertexDecoration decoration) {
        this.decorations.put(decoration.getClass(), decoration);
    }

    /**
     * Returns all decorations of this vertex.
     *
     * @return See above
     */
    public Collection<AbstractVertexDecoration> getDecorations() {
        return Collections.unmodifiableCollection(this.decorations.values());
    }

}
