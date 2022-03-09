package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.apache.commons.lang3.StringUtils;

/**
 * This class stores trace meta-information, such as the trace ID. This kept separate from the actual trace
 * information to allow references to traces without the need to keep the trace itself.
 *
 * @author Patrick Treyer
 */
public class TraceInformation {

    private final String traceId;
    private final String sessionId;

    /**
     * Creates a new trace information object using the given data.
     *
     * @param traceId   The trace ID to use
     * @param sessionId The session ID to use
     */
    public TraceInformation(final String traceId, final String sessionId) {
        this.traceId = traceId;
        this.sessionId = sessionId;
    }

    /**
     * Returns the trace ID.
     *
     * @return See above
     */
    public String getTraceId() {
        return this.traceId;
    }

    /**
     * Returns the trace's session ID, if any.
     *
     * @return See above. Note that this ID may be {@code null}.
     */
    public String getSessionId() {
        return this.sessionId;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TraceInformation)) {
            return false;
        }

        // The equality currently relies only on the trace ID to facilitate trace coloring.
        final TraceInformation otherTraceInformation = (TraceInformation) other;
        return StringUtils.equalsIgnoreCase(this.getTraceId(), otherTraceInformation.getTraceId());
    }

}
