package org.palladiosimulator.pmxupgrade.model.systemmodel.util;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;

/**
 * This class represents a pair consisting of an {@link Operation} and an {@link AssemblyComponent}.
 *
 * @author Patrick Treyer
 */
public class AssemblyComponentOperationPair implements ISystemModelElement {
    private final int id;
    private final Operation operation;

    private final AssemblyComponent assemblyComponent;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param id                The ID of this pair.
     * @param operation         The operation.
     * @param assemblyComponent The assembly component.
     */
    public AssemblyComponentOperationPair(final int id, final Operation operation, final AssemblyComponent assemblyComponent) {
        this.id = id;
        this.operation = operation;
        this.assemblyComponent = assemblyComponent;
    }

    public final int getId() {
        return this.id;
    }

    public final AssemblyComponent getAssemblyComponent() {
        return this.assemblyComponent;
    }

    public final Operation getOperation() {
        return this.operation;
    }

    @Override
    public String toString() {
        return +this.assemblyComponent.getId() + ":" + this.operation.getId() + "@" + this.id;
    }

    @Override
    public String getIdentifier() {
        return this.getAssemblyComponent().getIdentifier();
    }
}
