package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.util.TraceStartTimestampComparator;

import java.util.Comparator;

/**
 * Specialized sub-class for sessions which are derived from execution traces (see {@link ExecutionTrace}).
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class ExecutionTraceBasedSession extends AbstractSession<ExecutionTrace> {

    /**
     * Creates a new execution trace-based session with the given session ID.
     *
     * @param sessionId The session ID to use
     */
    public ExecutionTraceBasedSession(final String sessionId) {
        super(sessionId);
    }

    @Override
    protected Comparator<? super ExecutionTrace> getOrderComparator() {
        return new TraceStartTimestampComparator();
    }

}
