package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import java.util.Set;
import java.util.TreeSet;

import org.palladiosimulator.pmxupgrade.model.systemmodel.ISystemModelElement;

public class DataInterface implements ISystemModelElement {

	
	private final int id;
	private Set<String> components;
	
	public DataInterface(int id, Set<String> components) {
		this.id = id;
		this.components = new TreeSet<String>(components);
		
	}
	
	public void addComponent(String component) {
		if (!components.contains(component)) {
			components.add(component);
		}
	}
	
	public Set<String> getComponents() {
		return this.components;
	}
	
	@Override
	public String getIdentifier() {
		String result="";
		for(String str: components) {
			result = result + str;
		}
		return result;
	}
	
	@Override
	public boolean equals (Object other) {
		if(! (other instanceof DataInterface)) {
			return false;
		}
		DataInterface otherInterface = (DataInterface) other;
		return otherInterface.getComponents().equals(this.components);
	}
}
