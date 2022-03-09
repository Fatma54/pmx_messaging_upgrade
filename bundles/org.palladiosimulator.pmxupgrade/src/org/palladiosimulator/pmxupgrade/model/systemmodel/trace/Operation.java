package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AbstractSystemSubRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;

/**
 * This class represents an operation within the trace analysis tool. It consists of the component type and a signature.
 */
public class Operation {

    /**
     * The ID for the root operation.
     */
    public static final int ROOT_OPERATION_ID = AbstractSystemSubRepository.ROOT_ELEMENT_ID;

    private final int id;
    private final ComponentType componentType;
    private final Signature signature;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param id            The ID of this operation.
     * @param componentType The type of the component of this operation.
     * @param signature     The signature of this operation.
     */
    public Operation(final int id, final ComponentType componentType, final Signature signature) {
        this.id = id;
        this.componentType = componentType;
        this.signature = signature;
    }

    /**
     * Delivers the ID of the operation.
     *
     * @return The ID.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Delivers the component type of the operation.
     *
     * @return The component type.
     */
    public final ComponentType getComponentType() {
        return this.componentType;
    }

    /**
     * Delivers the signature of the operation.
     *
     * @return The signature.
     */
    public final Signature getSignature() {
        return this.signature;
    }

    /**
     * Two Operation objects are equal if their ids are equal.
     *
     * @param obj The object to be compared for equality with this
     * @return true if the two objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Operation)) {
            return false;
        }
        final Operation other = (Operation) obj;
        return other.id == this.id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (17 * hash) + this.id;
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder strBuild = new StringBuilder();
        strBuild.append(this.componentType.getFullQualifiedName()).append('.').append(this.signature.toString());
        return strBuild.toString();
    }
}
