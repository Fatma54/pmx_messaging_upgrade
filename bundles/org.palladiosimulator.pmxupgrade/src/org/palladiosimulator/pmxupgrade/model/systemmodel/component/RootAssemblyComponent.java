package org.palladiosimulator.pmxupgrade.model.systemmodel.component;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AbstractSystemSubRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.TypeRepository;

/**
 * This class represents a root assembly component.
 *
 * @author Patrick Treyer
 */
public class RootAssemblyComponent extends AssemblyComponent {

    /**
     * Creates a new root assembly component.
     */
    public RootAssemblyComponent() {
        super(AbstractSystemSubRepository.ROOT_ELEMENT_ID, SystemModelRepository.ROOT_NODE_LABEL, TypeRepository.ROOT_COMPONENT);
    }

    @Override
    public boolean isRootComponent() {
        return true;
    }
}
