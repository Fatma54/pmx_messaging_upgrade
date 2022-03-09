package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;

public class BasicComponentType extends ComponentType {

	public BasicComponentType(int id, String fullqualifiedTypeName) {
		super(id, fullqualifiedTypeName);
	}
	
	public BasicComponentType(final int id, final String packageName, final String typeName) {
		super(id, packageName, typeName);
	}

}
