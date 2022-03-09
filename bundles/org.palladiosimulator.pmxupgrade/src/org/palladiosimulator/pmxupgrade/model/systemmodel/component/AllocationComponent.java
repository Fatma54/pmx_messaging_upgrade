package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;

/**
 * @author PMX, Universitaet Wuerzburg.
 * @since 1.1
 */
public class AllocationComponent implements ISystemModelElement {

    private final int id;
    private final AssemblyComponent assemblyComponent;
    private final ExecutionContainer executionContainer;

    public AllocationComponent(final int id, final AssemblyComponent assemblyComponent, final ExecutionContainer executionContainer) {
        this.id = id;
        this.assemblyComponent = assemblyComponent;
        this.executionContainer = executionContainer;
    }

    public final int getId() {
        return this.id;
    }

    public final AssemblyComponent getAssemblyComponent() {
        return this.assemblyComponent;
    }

    public final ExecutionContainer getExecutionContainer() {
        return this.executionContainer;
    }

    @Override
    public final String toString() {
        final StringBuilder strBuild = new StringBuilder();
        strBuild.append(this.executionContainer.getName()).append("::").append(this.assemblyComponent.toString());
        return strBuild.toString();
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof AllocationComponent)) {
            return false;
        }
        final AllocationComponent other = (AllocationComponent) obj;
        return other.id == this.id;
    }

    @Override
    public String getIdentifier() {
        return this.getAssemblyComponent().getName();
    }
}
