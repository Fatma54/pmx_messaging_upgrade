package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Allocation Component OperationPairFactory of the system model.
 *
 * @author Patrick Treyer
 */
public class AllocationComponentOperationPairFactory extends AbstractSystemSubRepository {

    public static final AllocationComponentOperationPair ROOT_PAIR =
            new AllocationComponentOperationPair(ROOT_ELEMENT_ID, OperationRepository.ROOT_OPERATION,
                    AllocationRepository.ROOT_ALLOCATION_COMPONENT);

    private final Map<String, AllocationComponentOperationPair> pairsByName = new Hashtable<>();
    private final Map<Integer, AllocationComponentOperationPair> pairsById = new Hashtable<>();

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param systemFactory The repository for this component.
     */
    public AllocationComponentOperationPairFactory(final SystemModelRepository systemFactory) {
        super(systemFactory);
    }

    /**
     * Returns a corresponding pair instance (existing or newly created).
     *
     * @param allocationComponent The first element of the pair (the allocation component).
     * @param operation           The second element of the pair (the operation).
     * @return A (possible new) pair containing both elements.
     */
    public final AllocationComponentOperationPair getPairInstanceByPair(final AllocationComponent allocationComponent, final Operation operation) {
        final AllocationComponentOperationPair inst = this.getPairByNamedIdentifier(allocationComponent.getId() + "-" + operation.getId());
        if (inst == null) {
            return this.createAndRegisterPair(operation, allocationComponent);
        }
        return inst;
    }

    /**
     * Creates a new pair using both elements and registers it was well.
     *
     * @param operation           The second element of the pair (the operation).
     * @param allocationComponent The first element of the pair (the allocation component).
     * @return The newly created pair.
     */
    private AllocationComponentOperationPair createAndRegisterPair(final Operation operation, final AllocationComponent allocationComponent) {
        return this.createAndRegisterPair(allocationComponent.getId() + "-" + operation.getId(), operation, allocationComponent);
    }

    /**
     * Returns the instance for the passed factory name; null if no instance with this factory name exists.
     *
     * @param namedIdentifier The identifier to search for.
     * @return The corresponding pair to the given identifier if it exists, null otherwise.
     */
    private AllocationComponentOperationPair getPairByNamedIdentifier(final String namedIdentifier) {
        return this.pairsByName.get(namedIdentifier);
    }

    /**
     * Returns the instance for the passed ID; null if no instance with this ID is available.
     *
     * @param id The ID of the instance in question.
     * @return The corresponding pair to the given ID if it exists, null otherwise.
     */
    public final AllocationComponentOperationPair getPairById(final int id) {
        return this.pairsById.get(id);
    }

    private AllocationComponentOperationPair createAndRegisterPair(final String namedIdentifier, final Operation operation,
                                                                   final AllocationComponent allocationComponent) {
        if (this.pairsByName.containsKey(namedIdentifier)) {
            throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
        }
        final int id = this.getAndIncrementNextId();
        final AllocationComponentOperationPair newInst = new AllocationComponentOperationPair(id, operation, allocationComponent);
        this.pairsById.put(id, newInst);
        this.pairsByName.put(namedIdentifier, newInst);
        return newInst;
    }

    /**
     * Delivers all available allocation-component-operation pairs.
     *
     * @return A collection containing all available pairs.
     */
    public final Collection<AllocationComponentOperationPair> getPairs() {
        return this.pairsById.values();
    }
}
