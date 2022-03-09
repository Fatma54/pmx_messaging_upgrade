package org.palladiosimulator.pmxupgrade.logic.dataprocessing;

import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.ControlFlowService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.AbstractDependencyGraph;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.failureestimation.FailureEstimationService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.resourcedemands.ResourceDemandEstimationService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.workload.WorkloadService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.parametricdependencies.ParametricDependenciesResolverInterface;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.parametricdependencies.impl.ParametricDependenciesResolverApproachImpl;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;

import java.util.HashMap;
import java.util.List;

/**
 * Central service for processing the input data. This includes processing of the control flow, workloads, resource demand estimation,
 * failure estimation and resolving of parametric dependencies.
 *
 * @author Patrick Treyer
 */
public class DataProcessingService {

    private ControlFlowService controlFlowService = new ControlFlowService();
    private WorkloadService workloadService = new WorkloadService();
    private ResourceDemandEstimationService resourceDemandEstimationService = new ResourceDemandEstimationService(new Configuration());
    private FailureEstimationService failureEstimationService = new FailureEstimationService();
    private ParametricDependenciesResolverInterface parametricDependenciesResolver = new ParametricDependenciesResolverApproachImpl();

    public HashMap<String, List<Double>> analyzeWorkload(List<ExecutionTrace> executionTraces) {
        return workloadService.analyzeWorkload(executionTraces);
    }

    public AbstractDependencyGraph<AllocationComponentOperationPair> resolveControlFlow(List<ExecutionTrace> executionTraces, SystemModelRepository systemModelRepository) {
        return controlFlowService.resolveControlFlow(executionTraces, systemModelRepository);
    }

    public HashMap<String, Double> calculateResourceDemands(List<ExecutionTrace> executionTraces) {
        executionTraces.forEach(executionTrace -> resourceDemandEstimationService.inputMessageTraces(executionTrace.getMessageTrace()));
        return resourceDemandEstimationService.terminate();
    }

    public SystemModelRepository calculateFailureProbabilities(SystemModelRepository systemModelRepository) {
        return failureEstimationService.calculateFailureProbabilities(systemModelRepository);
    }

    public SystemModelRepository resolveParametricDependencies(SystemModelRepository systemModelRepository) {
        SystemModelRepository updatedSystemModel = parametricDependenciesResolver.identifyParametricDependencies(systemModelRepository);
        return parametricDependenciesResolver.characterizeParametricDependencies(updatedSystemModel);
    }

}
