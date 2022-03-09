package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.RootExecutionContainer;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Execution environment repository of the system model.
 *
 * @author Patrick Treyer
 */
public class ExecutionEnvironmentRepository extends AbstractSystemSubRepository {

    /**
     * The root execution container.
     */
    public static final ExecutionContainer ROOT_EXECUTION_CONTAINER = new RootExecutionContainer();

    private final Map<String, ExecutionContainer> executionContainersByName = new Hashtable<>();
    private final Map<String, ExecutionContainer> executionContainersById = new Hashtable<>();

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param systemFactory The system factory.
     */
    public ExecutionEnvironmentRepository(final SystemModelRepository systemFactory) {
        super(systemFactory);
    }

    /**
     * @param namedIdentifier The identifier to search for.
     * @return The instance for the passed namedIdentifier; null if no instance with this namedIdentifier.
     */
    public final ExecutionContainer lookupExecutionContainerByNamedIdentifier(final String namedIdentifier) {
        return this.executionContainersByName.get(namedIdentifier);
    }

    /**
     * Returns the instance for the passed container ID; null if no instance
     * with this ID.
     *
     * @param containerId The ID to search for.
     * @return The container for the given ID if it exists; null otherwise.
     */
    public final ExecutionContainer lookupExecutionContainerByContainerId(final String containerId) {
        return this.executionContainersById.get(containerId);
    }

    /**
     * This method creates a new execution container and registers it as well.
     *
     * @param namedIdentifier The identifier of the new container.
     * @return The newly created execution container.
     */
    public final ExecutionContainer createAndRegisterExecutionContainer(final String namedIdentifier) {
        if (this.executionContainersByName.containsKey(namedIdentifier)) {
            throw new IllegalArgumentException("Element with name " + namedIdentifier + " exists already");
        }
        final int id = this.getAndIncrementNextId();
        final ExecutionContainer newInst = new ExecutionContainer(id, null, namedIdentifier);
        this.executionContainersById.put(namedIdentifier, newInst);
        this.executionContainersByName.put(namedIdentifier, newInst);
        return newInst;
    }

    public final boolean exists(final String namedIdentifier) {
        return this.executionContainersByName.containsKey(namedIdentifier);
    }

    /**
     * Delivers all available execution containers.
     *
     * @return A collection containing the available containers.
     */
    public final Collection<ExecutionContainer> getExecutionContainers() {
        return this.executionContainersById.values();
    }
}
