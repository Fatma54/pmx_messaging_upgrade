package org.palladiosimulator.pmxupgrade.logic.dataprocessing.parametricdependencies.impl;

import org.palladiosimulator.pmxupgrade.logic.dataprocessing.parametricdependencies.ParametricDependenciesResolverInterface;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;

/**
 * Resolver for the identification and characterization of parametric dependencies.
 *
 * @author Patrick Treyer
 */
public class ParametricDependenciesResolverApproachImpl implements ParametricDependenciesResolverInterface {

    /**
     * Here, a specific approach for the identification of parametric dependencies can be integrated.
     *
     * @param systemModelRepository, the system Model with the corresponding operations and parameters
     * @return the updated {@link SystemModelRepository}
     */
    @Override
    public SystemModelRepository identifyParametricDependencies(SystemModelRepository systemModelRepository) {
        // TODO integrate a specific approach for the identification of parametric dependencies
        return systemModelRepository;
    }

    /**
     * Here, a specific approach for the characterization of parametric dependencies can be integrated.
     *
     * @param systemModelRepository, the system Model with the corresponding operations and parameters
     * @return the updated {@link SystemModelRepository}
     */
    @Override
    public SystemModelRepository characterizeParametricDependencies(SystemModelRepository systemModelRepository) {
        // TODO integrate a specific approach for the characterization of parametric dependencies
        return systemModelRepository;
    }
}
