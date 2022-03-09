package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;

/**
 * Represents the Assembly component of the system model.
 *
 * @author Patrick Treyer
 */
public class AssemblyComponent implements ISystemModelElement {
    private final int id;
    private final String name;
    private final ComponentType type;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param id   The ID of this assembly component.
     * @param name The name of this component.
     * @param type The type of this component.
     */
    public AssemblyComponent(final int id, final String name, final ComponentType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public final int getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final ComponentType getType() {
        return this.type;
    }

    @Override
    public final String toString() {
        final StringBuilder strBuild = new StringBuilder();
        strBuild.append(this.name).append(':').append(this.type.getFullQualifiedName());
        return strBuild.toString();
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AssemblyComponent)) {
            return false;
        }
        final AssemblyComponent other = (AssemblyComponent) obj;
        return other.id == this.id;
    }

    /**
     * Denotes whether this assembly component is a root component.
     *
     * @return See above
     */
    public boolean isRootComponent() {
        return false;
    }

    @Override
    public String getIdentifier() {
        if (this.getType() == null) {
            return this.getName();
        } else {
            return this.getType().getFullQualifiedName();
        }
    }
}
