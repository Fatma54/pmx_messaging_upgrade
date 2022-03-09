package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents the Execution container of the system model.
 *
 * @author Patrick Treyer
 */
public class ExecutionContainer implements ISystemModelElement {
    private final int id;
    private final String name;
    private final ExecutionContainer parent;
    private final Collection<ExecutionContainer> childContainers = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param id     The ID of this container.
     * @param parent The parent of this container.
     * @param name   The name of this container.
     */
    public ExecutionContainer(final int id, final ExecutionContainer parent, final String name) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    /**
     * Delivers the ID of the container.
     *
     * @return The ID.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Delivers the name of the container.
     *
     * @return The name.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Delivers the parent of the container.
     *
     * @return The parent.
     */
    public final ExecutionContainer getParent() {
        return this.parent;
    }

    /**
     * Delivers a collection containing the added child containers.
     *
     * @return The child containers.
     */
    public final Collection<ExecutionContainer> getChildContainers() {
        return this.childContainers;
    }

    /**
     * This method adds a given container to the list of child containers.
     *
     * @param container The new child container.
     */
    public final void addChildContainer(final ExecutionContainer container) {
        this.childContainers.add(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ExecutionContainer)) {
            return false;
        }
        final ExecutionContainer other = (ExecutionContainer) obj;
        return other.id == this.id;
    }

    /**
     * Returns whether this container is a root container.
     *
     * @return See above
     */
    public boolean isRootContainer() {
        return false;
    }

    /**
     * Delivers the identifier (name) of this object.
     *
     * @return The identifier.
     */
    @Override
    public String getIdentifier() {
        return this.getName();
    }
}
