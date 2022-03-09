package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AbstractSystemSubRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;

/**
 * Specific subtype for the root execution container.
 *
 * @author Patrick Treyer
 */
public class RootExecutionContainer extends ExecutionContainer {

    /**
     * Creates a new root execution container.
     */
    public RootExecutionContainer() {
        super(AbstractSystemSubRepository.ROOT_ELEMENT_ID, null, SystemModelRepository.ROOT_NODE_LABEL);
    }

    @Override
    public boolean isRootContainer() {
        return true;
    }
}
