package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

/**
 * This is the abstract base for a trace (like a message trace e.g.).
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractTrace {

    /**
     * Default value for the session ID.
     */
    public static final String DEFAULT_SESSION_ID = "N/A";

    /**
     * This constant can be used as an ID to show that the trace has no ID.
     */
    public static final String NO_TRACE_ID = "N/A";

    private final TraceInformation traceInformation;

    /**
     * Default constructor.
     */
    protected AbstractTrace() {
        this("", DEFAULT_SESSION_ID);
    }

    /**
     * Creates a new abstract trace with the given trace ID and a default session ID.
     *
     * @param traceId The trace ID to use for the new trace
     */
    public AbstractTrace(final String traceId) {
        this(traceId, DEFAULT_SESSION_ID);
    }

    /**
     * Creates a new abstract trace with the given parameters.
     *
     * @param traceId   The trace ID to use for the new trace
     * @param sessionId The session ID to use for the new trace
     */
    public AbstractTrace(final String traceId, final String sessionId) {
        this.traceInformation = new TraceInformation(traceId, sessionId);
    }

    /**
     * Returns information about this trace.
     *
     * @return See above
     */
    public TraceInformation getTraceInformation() {
        return this.traceInformation;
    }

    /**
     * Returns this trace's trace ID.
     *
     * @return See above
     */
    public String getTraceId() {
        return this.traceInformation.getTraceId();
    }

    /**
     * Delivers the ID of the session.
     *
     * @return The session ID.
     */
    public String getSessionId() {
        return this.traceInformation.getSessionId();
    }

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Returns this trace's start timestamp.
     *
     * @return See above
     */
    public abstract long getStartTimestamp();

    /**
     * Returns this trace's end timestamp.
     *
     * @return See above
     */
    public abstract long getEndTimestamp();
}
