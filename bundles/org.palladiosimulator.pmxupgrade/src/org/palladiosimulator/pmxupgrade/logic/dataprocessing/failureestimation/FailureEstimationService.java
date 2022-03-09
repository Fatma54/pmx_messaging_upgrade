package org.palladiosimulator.pmxupgrade.logic.dataprocessing.failureestimation;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;

/**
 * Service for the estimation of the failure probabilities for the individual executions.
 *
 * @author Patrick Treyer
 */
public class FailureEstimationService {

    public SystemModelRepository calculateFailureProbabilities(SystemModelRepository systemModelRepository) {
        // TODO perform failure estimation based on the span error tags and log fields and add them to the system model
        return systemModelRepository;
    }
}
