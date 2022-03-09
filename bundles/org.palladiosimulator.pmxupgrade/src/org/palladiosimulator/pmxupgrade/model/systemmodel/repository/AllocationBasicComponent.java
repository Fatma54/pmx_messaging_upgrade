package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;

public class AllocationBasicComponent extends AllocationComponent {

	public AllocationBasicComponent(int id, AssemblyComponent assemblyComponent,
			ExecutionContainer executionContainer) {
		super(id, assemblyComponent, executionContainer);
	}

}
