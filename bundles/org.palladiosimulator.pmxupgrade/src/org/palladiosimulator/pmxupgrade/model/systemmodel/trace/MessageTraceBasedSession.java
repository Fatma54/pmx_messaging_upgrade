package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.util.TraceStartTimestampComparator;

import java.util.Comparator;

/**
 * Specialized sub-class for sessions based on message traces (see {@link MessageTrace}).
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class MessageTraceBasedSession extends AbstractSession<MessageTrace> {

    /**
     * Creates a new message trace-based session with the given session ID.
     *
     * @param sessionId The session ID to use
     */
    public MessageTraceBasedSession(final String sessionId) {
        super(sessionId);
    }

    @Override
    protected Comparator<? super MessageTrace> getOrderComparator() {
        return new TraceStartTimestampComparator();
    }

}
