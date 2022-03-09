package org.palladiosimulator.pmxupgrade.model.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Superclass for graph elements, i.e. edges and vertices, in the visualization package.
 *
 * @param <O> The type of the objects which caused the creation of this graph element
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractGraphElement<O> {

    private volatile Color color = Color.BLACK;
    private volatile String description;

    private final Set<O> origins = new HashSet<>();

    /**
     * This constructor initializes the element based on the given parameters.
     *
     * @param origin       The origin of this element.
     * @param originPolicy The origin policy of this element.
     */
    protected AbstractGraphElement(final O origin, final IOriginRetentionPolicy originPolicy) {
        originPolicy.handleOrigin(this, origin);
    }

    /**
     * Returns this graph element's color.
     *
     * @return See above
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Sets this graph element's color.
     *
     * @param color The color to set
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Returns this graph element's description.
     *
     * @return See above
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets this graph element's description.
     *
     * @param description The description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the objects which caused the creation of this element.
     *
     * @return See above
     */
    public Set<O> getOrigins() {
        return Collections.unmodifiableSet(this.origins);
    }

    /**
     * Adds a new origin object to this element.
     *
     * @param origin The origin object
     */
    public final void addOrigin(final O origin) {
        if (origin != null) {
            this.origins.add(origin);
        }
    }

    /**
     * Returns an identifier for this graph element (e.g., a label).
     *
     * @return An identifier or {@code null} if no identifier can be determined
     */
    public abstract String getIdentifier();

}
