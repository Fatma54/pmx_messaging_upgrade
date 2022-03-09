package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.DataInterface;

public class DataInterfacesRepository extends AbstractSystemSubRepository {

	private final Map<Set<String>, DataInterface> dataInterfacesByComponents = new Hashtable<>();
    private final Map<Integer, DataInterface> dataInterfacesById = new Hashtable<>();
	public DataInterfacesRepository(SystemModelRepository systemFactory) {
		super(systemFactory);
	}
	
    public final DataInterface lookupDataInterfaceByComponentsSet(final Set<String> components) {
        return this.dataInterfacesByComponents.get(components);
    }

    
    public final DataInterface createAndRegisterDataInterface(final Set<String> components) {
        if (this.dataInterfacesByComponents.containsKey(components)) {
            throw new IllegalArgumentException("Element with components " + components + "exists already");
        }
        final int id = this.getAndIncrementNextId();
        final DataInterface newInst = new DataInterface(id, components);
        this.dataInterfacesById.put(id, newInst);
        this.dataInterfacesByComponents.put(components, newInst);
        return newInst;
    }
  

    /**
     *
     * @return The already stored data interfaces.
     */
    public final Collection<DataInterface> getDataInterfaces() {
        return this.dataInterfacesById.values();
    }
    
    public int getDataInterfaceIdByComponents(Set<String> components) {
    	for (Entry<Integer, DataInterface> dataInterface :dataInterfacesById.entrySet()) {
    		if (dataInterface.getValue().getComponents().equals(components)) {
    			return dataInterface.getKey();
    		}
    	}
    	return -1;
    }
    
    public String getDataInterfaceNameByComponents(Set<String> components) {
    	for (Entry<Integer, DataInterface> dataInterface :dataInterfacesById.entrySet()) {
    		if (dataInterface.getValue().getComponents().equals(components)) {
    			//return "dataInterface " + dataInterface.getKey();
    			return components.toString();
    		}
    	}
    	return null;
    }
}
