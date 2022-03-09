package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

/**
 * This object represents an somehow invalid trace of executions.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class InvalidExecutionTrace {

    private final ExecutionTrace invalidExecutionTraceArtifacts;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param invalidExecutionTrace The execution trace which will be wrapped as invalid.
     */
    public InvalidExecutionTrace(final ExecutionTrace invalidExecutionTrace) {
        this.invalidExecutionTraceArtifacts = invalidExecutionTrace;
    }

    /**
     * Delivers the wrapped execution trace.
     *
     * @return The execution trace artifacts.
     */
    public ExecutionTrace getInvalidExecutionTraceArtifacts() {
        return this.invalidExecutionTraceArtifacts;
    }

    @Override
    public String toString() {
        return "Invalid Trace: " + this.invalidExecutionTraceArtifacts.toString();
    }

    @Override
    public int hashCode() {
        return ((this.invalidExecutionTraceArtifacts == null) ? 0 : this.invalidExecutionTraceArtifacts.hashCode()); // NOCS
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof InvalidExecutionTrace)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return ((InvalidExecutionTrace) obj).getInvalidExecutionTraceArtifacts().equals(this.invalidExecutionTraceArtifacts);
    }
}
