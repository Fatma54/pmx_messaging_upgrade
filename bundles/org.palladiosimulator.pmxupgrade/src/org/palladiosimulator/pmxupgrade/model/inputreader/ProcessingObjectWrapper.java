package org.palladiosimulator.pmxupgrade.model.inputreader;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;

import java.util.List;

/**
 * Wrapper for the processing the input data, which includes the system model and the input-dependent execution traces.
 *
 * @author Patrick Treyer
 */
public class ProcessingObjectWrapper {

    private SystemModelRepository systemModelRepository;
    private List<ExecutionTrace> executionTraces;
    private List<Execution> invalidExecutions;

    public ProcessingObjectWrapper(SystemModelRepository systemModelRepository, List<ExecutionTrace> executionTraces, List<Execution> invalidExecutions) {
        this.systemModelRepository = systemModelRepository;
        this.executionTraces = executionTraces;
        this.invalidExecutions = invalidExecutions;
    }

    public SystemModelRepository getSystemModelRepository() {
        return systemModelRepository;
    }

    public void setSystemModelRepository(SystemModelRepository systemModelRepository) {
        this.systemModelRepository = systemModelRepository;
    }

    public List<ExecutionTrace> getExecutionTraces() {
        return executionTraces;
    }

    public void setExecutionTraces(List<ExecutionTrace> executionTraces) {
        this.executionTraces = executionTraces;
    }

    public List<Execution> getInvalidExecutions() {
        return invalidExecutions;
    }

    public void setInvalidExecutions(List<Execution> invalidExecutions) {
        this.invalidExecutions = invalidExecutions;
    }
}
