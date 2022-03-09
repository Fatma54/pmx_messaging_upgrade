package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * This is a repository in which the different component types
 * ({@link ComponentType}) can be stored.
 *
 * @author Patrick Treyer
 */
public class TypeRepository extends AbstractSystemSubRepository {

	/**
	 * This constant represents the root component.
	 */
	public static final ComponentType ROOT_COMPONENT = new ComponentType(ROOT_ELEMENT_ID,
			SystemModelRepository.ROOT_NODE_LABEL);

	private final Map<String, BasicComponentType> componentTypesByName = new Hashtable<>();
	private final Map<Integer, BasicComponentType> componentTypesById = new Hashtable<>();

	// with a second map we don't need to look into all channels and components when
	// one of them is not needed
	private final Map<String, DataChannelType> channelTypesByName = new Hashtable<>();
	private final Map<Integer, DataChannelType> channelTypesById = new Hashtable<>();

	private enum Type {
		BASICCOMPONENT, DATACHANNEL
	}

	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param systemFactory The system factory.
	 */
	public TypeRepository(final SystemModelRepository systemFactory) {
		super(systemFactory);
	}

	/**
	 * Returns the instance for the passed namedIdentifier; null if no instance with
	 * this namedIdentifier.
	 *
	 * @param namedIdentifier The identifier to search for.
	 * @return The corresponding component type if available; null otherwise.
	 */
	public final ComponentType lookupComponentTypeByNamedIdentifier(final String namedIdentifier, String type) {
		synchronized (this) {
			switch (Type.valueOf(type)) {
			case BASICCOMPONENT:
				return this.componentTypesByName.get(namedIdentifier);
			case DATACHANNEL:
				return this.channelTypesByName.get(namedIdentifier);
			default:
				return null;
			}
		}
	}

	/**
	 * Creates and registers a component type that has not been registered yet.
	 *
	 * @param namedIdentifier   The identifier of the new component type.
	 * @param fullqualifiedName The fully qualfieid name of the new component type.
	 * @return the created component type
	 */
	public final ComponentType createAndRegisterComponentType(final String namedIdentifier,
			final String fullqualifiedName, String type) {
		final ComponentType newInst;
		synchronized (this) {
			switch (Type.valueOf(type)) {
			case BASICCOMPONENT:
				if (this.componentTypesByName.containsKey(namedIdentifier)) {
					throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
				}
				final int id = this.getAndIncrementNextId();
				BasicComponentType bCInst = new BasicComponentType(id, fullqualifiedName);
				newInst = bCInst;
				this.componentTypesById.put(id, bCInst);
				this.componentTypesByName.put(namedIdentifier, bCInst);
				return newInst;
			case DATACHANNEL:
				if (this.componentTypesByName.containsKey(namedIdentifier)) {
					throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
				}
				final int idDC = this.getAndIncrementNextId();
				DataChannelType dCInst = new DataChannelType(idDC, fullqualifiedName);
				newInst = dCInst;
				this.channelTypesById.put(idDC, dCInst);
				this.channelTypesByName.put(namedIdentifier, dCInst);
				return newInst;
			default:
				return null;
			}
		}
	}

	/**
	 * Returns a collection of all registered component types.
	 *
	 * @return a collection of all registered component types.
	 */
	public final Collection<BasicComponentType> getComponentTypes() {
		synchronized (this) {
			return this.componentTypesById.values();
		}
	}
	
	/**
	 * Returns a collection of all registered channel types
	 * 
	 * @return a collection of all registered channel types
	 */
	public final Collection<DataChannelType> getChannelTypes() {
		synchronized (this) {
			return this.channelTypesById.values();
		}
	}
	
}
