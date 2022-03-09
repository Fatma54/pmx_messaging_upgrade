package org.palladiosimulator.pmxupgrade.model.systemmodel.repository;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract system sub repository of the system model.
 *
 * @author Patrick Treyer
 */
public abstract class AbstractSystemSubRepository {

    /**
     * This constant represents the ID of the root element.
     */
    public static final int ROOT_ELEMENT_ID = 0;

    private final AtomicInteger nextId = new AtomicInteger(ROOT_ELEMENT_ID + 1);

    private final SystemModelRepository systemFactory;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param systemFactory The system factory.
     */
    public AbstractSystemSubRepository(final SystemModelRepository systemFactory) {
        this.systemFactory = systemFactory;
    }

    protected final SystemModelRepository getSystemFactory() {
        return this.systemFactory;
    }

    /**
     * This method delivers the next ID and increments the ID counter atomically.
     *
     * @return The next ID.
     */
    protected final int getAndIncrementNextId() {
        return this.nextId.getAndIncrement();
    }
}
