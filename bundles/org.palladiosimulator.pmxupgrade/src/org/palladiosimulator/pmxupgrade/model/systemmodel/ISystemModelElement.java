package org.palladiosimulator.pmxupgrade.model.systemmodel;

/**
 * Abstract supertype for all entities in the system model.
 *
 * @author Patrick Treyer
 */
public interface ISystemModelElement {

    /**
     * Returns a textual identifier for this object (e.g., its name).
     */
    String getIdentifier();

}
