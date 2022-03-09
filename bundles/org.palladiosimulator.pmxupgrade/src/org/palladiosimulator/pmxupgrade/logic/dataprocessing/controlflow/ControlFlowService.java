package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow;

import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.AbstractDependencyGraph;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.FlowGraphCreationAdapter;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.OperationAllocationDependencyGraph;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AbstractSystemSubRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.OperationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;

import java.util.List;

/**
 * Service for Extracting the control flow using an operation call graph including calls weights that allow deriving call probabilities.
 *
 * @author Patrick Treyer
 */
public class ControlFlowService {

    public AbstractDependencyGraph<AllocationComponentOperationPair> resolveControlFlow(final List<ExecutionTrace> executionTraces, final SystemModelRepository systemModelRepository) {
        FlowGraphCreationAdapter adapter = new FlowGraphCreationAdapter(new OperationAllocationDependencyGraph(new AllocationComponentOperationPair(
                AbstractSystemSubRepository.ROOT_ELEMENT_ID, OperationRepository.ROOT_OPERATION, AllocationRepository.ROOT_ALLOCATION_COMPONENT)));

        for (ExecutionTrace t : executionTraces) {
            adapter.inputMessageTraces(t.getMessageTrace(), systemModelRepository);
        }

        return adapter.getGraph();
    }

}
