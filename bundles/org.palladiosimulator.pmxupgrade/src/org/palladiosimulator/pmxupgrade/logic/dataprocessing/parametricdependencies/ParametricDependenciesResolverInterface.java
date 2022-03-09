package org.palladiosimulator.pmxupgrade.logic.dataprocessing.parametricdependencies;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;

/**
 * General interface for the identification and characterization of parametric dependencies.
 *
 * @author Patrick Treyer
 */
public interface ParametricDependenciesResolverInterface {

    SystemModelRepository identifyParametricDependencies(SystemModelRepository systemModelRepository);

    SystemModelRepository characterizeParametricDependencies(SystemModelRepository systemModelRepository);

}
