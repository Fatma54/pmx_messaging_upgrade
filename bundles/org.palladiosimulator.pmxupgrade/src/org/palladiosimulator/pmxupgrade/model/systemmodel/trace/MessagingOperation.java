package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.DataInterface;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;

public class MessagingOperation extends Operation {
	
	private DataInterface dataInterface;

	public MessagingOperation(int id, ComponentType componentType, Signature signature, DataInterface dataInterface) {
		super(id, componentType, signature);
		this.dataInterface = dataInterface;
	}
	
	public DataInterface getdataInterface() {
		return this.dataInterface;
	}

}
