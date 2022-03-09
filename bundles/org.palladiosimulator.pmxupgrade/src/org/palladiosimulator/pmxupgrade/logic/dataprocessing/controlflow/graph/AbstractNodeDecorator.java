package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import java.util.concurrent.TimeUnit;

import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract superclass for all node decorators.
 *
 * @author PMX, Universitaet Wuerzburg.
 * @since 1.5
 */
public abstract class AbstractNodeDecorator {

    private static final String RESPONSE_TIME_DECORATOR_FLAG_NS = "responseTimes-ns";
    private static final String RESPONSE_TIME_DECORATOR_FLAG_US = "responseTimes-us";
    private static final String RESPONSE_TIME_DECORATOR_FLAG_MS = "responseTimes-ms";
    private static final String RESPONSE_TIME_DECORATOR_FLAG_S = "responseTimes-s";

    /**
     * Creates a node decorator from its option name.
     *
     * @param optionName The option name to create a decorator for
     * @return An appropriate node decorator or {@code null} if none can be determined
     */
    public static AbstractNodeDecorator createFromName(final String optionName) {
        if (StringUtils.equalsIgnoreCase(RESPONSE_TIME_DECORATOR_FLAG_NS, optionName)) {
            return new ResponseTimeNodeDecorator(TimeUnit.NANOSECONDS);
        } else if (StringUtils.equalsIgnoreCase(RESPONSE_TIME_DECORATOR_FLAG_US, optionName)) {
            return new ResponseTimeNodeDecorator(TimeUnit.MICROSECONDS);
        } else if (StringUtils.equalsIgnoreCase(RESPONSE_TIME_DECORATOR_FLAG_MS, optionName)) {
            return new ResponseTimeNodeDecorator(TimeUnit.MILLISECONDS);
        } else if (StringUtils.equalsIgnoreCase(RESPONSE_TIME_DECORATOR_FLAG_S, optionName)) {
            return new ResponseTimeNodeDecorator(TimeUnit.SECONDS);
        }
        return null;
    }

    /**
     * Processes a message sent from the given source to the given target node.
     *
     * @param message    The sent message
     * @param sourceNode The source node sending the message
     * @param targetNode The target node receiving the message
     * @param timeunit   The time unit which determines how to interpret times.
     */
    public abstract void processMessage(AbstractMessage message, DependencyGraphNode<?> sourceNode, DependencyGraphNode<?> targetNode, final TimeUnit timeunit);
}
