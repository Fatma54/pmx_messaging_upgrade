package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.RootAssemblyComponent;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents the Assembly repository of the system model.
 *
 * @author Patrick Treyer
 */
public class AssemblyRepository extends AbstractSystemSubRepository {

    /**
     * This constant represents the root assembly component.
     */
    public static final AssemblyComponent ROOT_ASSEMBLY_COMPONENT = new RootAssemblyComponent();

    private final Map<String, AssemblyBasicComponent> assemblyComponentInstancesByName = new Hashtable<>();
    private final Map<Integer, AssemblyBasicComponent> assemblyComponentInstancesById = new Hashtable<>();
    
    //might have to change the structure later and extract messaging based attributes in separate classes
    private final Map<String, AssemblyDataChannel> assemblyChannelInstancesByName = new Hashtable<>();
    private final Map<Integer, AssemblyDataChannel> assemblyChannelInstancesById = new Hashtable<>();

    
    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param systemFactory The system factory.
     */
    public AssemblyRepository(final SystemModelRepository systemFactory) {
        super(systemFactory);
    }

    /**
     * Returns the instance for the passed ID; null if no instance with this ID exists.
     *
     * @param containerId The ID to search for.
     * @return The component for the given ID if it exists; null otherwise.
     */
    public final AssemblyComponent lookupAssemblyComponentById(final int containerId) {
        return this.assemblyComponentInstancesById.get(containerId);
    }
    
    //see above ~fat
    public final AssemblyDataChannel lookupAssemblyChannelById(final int containerId) {
        return this.assemblyChannelInstancesById.get(containerId);
    }

    /**
     * Returns the instance for the passed factoryIdentifier; null if no instance
     * with this factoryIdentifier.
     *
     * @param namedIdentifier The identifier to search for.
     * @return The component for the given identifier if it exists; null otherwise.
     */
    public final AssemblyComponent lookupAssemblyComponentInstanceByNamedIdentifier(final String namedIdentifier) {
    	if (this.assemblyComponentInstancesByName != null 
    			&& this.assemblyComponentInstancesByName.get(namedIdentifier) != null) {
        	return this.assemblyComponentInstancesByName.get(namedIdentifier);
    	} else { if(this.assemblyChannelInstancesById != null)
    		return this.assemblyChannelInstancesByName.get(namedIdentifier);
    	}
    	return null;
    }
    
    /**
     * Creates a new assembly component instance and registers it as well.
     *
     * @param namedIdentifier The identifier of the new component.
     * @param componentType   The new component type.
     * @return The newly created assembly component.
     */
    public final AssemblyBasicComponent createAndRegisterAssemblyComponentInstance(final String namedIdentifier, final ComponentType componentType) {

    	if (this.assemblyComponentInstancesByName.containsKey(namedIdentifier)) {
            throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
        }
        final int id = this.getAndIncrementNextId();
        final AssemblyBasicComponent newInst = new AssemblyBasicComponent(id, "@" + id, componentType);
        this.assemblyComponentInstancesById.put(id, newInst);
        this.assemblyComponentInstancesByName.put(namedIdentifier, newInst);
        return newInst;
    }
    
    //see aboce ~fat
    public final AssemblyDataChannel createAndRegisterAssemblyChannelInstance(final String namedIdentifier, final ComponentType componentType) {
        if (this.assemblyChannelInstancesByName.containsKey(namedIdentifier)) {
            throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
        }
        final int id = this.getAndIncrementNextId();
        final AssemblyDataChannel newInst = new AssemblyDataChannel(id, "@" + id, componentType);
        this.assemblyChannelInstancesById.put(id, newInst);
        this.assemblyChannelInstancesByName.put(namedIdentifier, newInst);
        return newInst;
    }

    /**
     * Delivers all available assembly component instances.
     *
     * @return A collection containing all components.
     */
    public final Collection<AssemblyBasicComponent> getAssemblyComponentInstances() {
        return this.assemblyComponentInstancesById.values();
    }
    
    //see above ~fat
    public final Collection<AssemblyDataChannel> getAssemblyChannelInstances() {
        return this.assemblyChannelInstancesById.values();
    }
}
