package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents the Allocation repository of the system model.
 *
 * @author Patrick Treyer
 */
public class AllocationRepository extends AbstractSystemSubRepository {
	public static final AllocationComponent ROOT_ALLOCATION_COMPONENT = new AllocationComponent(ROOT_ELEMENT_ID,
			AssemblyRepository.ROOT_ASSEMBLY_COMPONENT, ExecutionEnvironmentRepository.ROOT_EXECUTION_CONTAINER);

	private final Map<String, AllocationBasicComponent> allocationComponentInstancesByName = new Hashtable<>();
	private final Map<Integer, AllocationBasicComponent> allocationComponentInstancesById = new Hashtable<>();
	// TODO: allocation component typ hierarchie hinzufügen
	// TODO: data channel map erstmal
	private final Map<String, AllocationDataChannel> allocationChannelInstancesByName = new Hashtable<>();
	private final Map<Integer, AllocationDataChannel> allocationChannelInstancesById = new Hashtable<>();

	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param systemFactory The system factory.
	 */
	public AllocationRepository(final SystemModelRepository systemFactory) {
		super(systemFactory);
	}

	/**
	 * Returns the instance for the passed factoryIdentifier; null if no instance
	 * with this factoryIdentifier.
	 *
	 * @param namedIdentifier The identifier to search for.
	 * @return The corresponding instance if it exists.
	 */
	public final AllocationBasicComponent lookupAllocationComponentInstanceByNamedIdentifier(final String namedIdentifier) {
		return this.allocationComponentInstancesByName.get(namedIdentifier);
	}

	// similar to above ~fat
	public final AllocationDataChannel lookupAllocationChannelInstanceByNamedIdentifier(final String namedIdentifier) {
		return this.allocationChannelInstancesByName.get(namedIdentifier);
	}

	public final AllocationComponent createAndRegisterAllocationComponentInstance(final String namedIdentifier,
			final AssemblyComponent assemblyComponentInstance, final ExecutionContainer executionContainer) {
		if (assemblyComponentInstance instanceof AssemblyBasicComponent) {
			if (this.allocationComponentInstancesByName.containsKey(namedIdentifier)) {
				throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
			}
			final int id = this.getAndIncrementNextId();
			final AllocationBasicComponent newInst = new AllocationBasicComponent(id, assemblyComponentInstance, executionContainer);
			this.allocationComponentInstancesById.put(id, newInst);
			this.allocationComponentInstancesByName.put(namedIdentifier, newInst);
			return newInst;
		} else {
			if (assemblyComponentInstance instanceof AssemblyDataChannel) {
				if (this.allocationChannelInstancesByName.containsKey(namedIdentifier)) {
					throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
				}
				final int id = this.getAndIncrementNextId();
				final AllocationDataChannel newInst = new AllocationDataChannel(id, assemblyComponentInstance, executionContainer);
				this.allocationChannelInstancesById.put(id, newInst);
				this.allocationChannelInstancesByName.put(namedIdentifier, newInst);
				return newInst;
			}
		}
		return null;
	}

	public final Collection<AllocationBasicComponent> getAllocationComponentInstances() {
		return this.allocationComponentInstancesById.values();
	}
	
	//similar to above ~fat
	public final Collection<AllocationDataChannel> getAllocationChannelInstances() {
		return this.allocationChannelInstancesById.values();
	}
}
