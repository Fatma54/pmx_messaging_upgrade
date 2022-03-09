package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;

public class DataChannelType extends ComponentType {

	public DataChannelType(int id, String fullqualifiedTypeName) {
		super(id, fullqualifiedTypeName);
	}
	
	public DataChannelType(final int id, final String packageName, final String typeName) {
		super(id, packageName, typeName);
	}
	
}
