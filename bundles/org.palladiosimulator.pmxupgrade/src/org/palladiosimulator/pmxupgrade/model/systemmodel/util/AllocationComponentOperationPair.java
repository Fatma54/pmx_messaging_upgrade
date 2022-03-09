package org.palladiosimulator.pmxupgrade.model.systemmodel.util;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;

/**
 * This class represents a pair consisting of an {@link Operation} and an {@link AllocationComponent}.
 *
 * @author Patrick Treyer
 */
public class AllocationComponentOperationPair implements ISystemModelElement {
    private final int id;
    private final Operation operation;

    private final AllocationComponent allocationComponent;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param id                  The ID of this pair.
     * @param operation           The operation.
     * @param allocationComponent The allocation component.
     */
    public AllocationComponentOperationPair(final int id, final Operation operation, final AllocationComponent allocationComponent) {
        this.id = id;
        this.operation = operation;
        this.allocationComponent = allocationComponent;
    }

    public final int getId() {
        return this.id;
    }

    public final AllocationComponent getAllocationComponent() {
        return this.allocationComponent;
    }

    public final Operation getOperation() {
        return this.operation;
    }

    @Override
    public String toString() {
        return +this.allocationComponent.getId() + ":" + this.operation.getId() + "@" + this.id;
    }

    @Override
    public String getIdentifier() {
        return this.getAllocationComponent().getIdentifier();
    }
}
