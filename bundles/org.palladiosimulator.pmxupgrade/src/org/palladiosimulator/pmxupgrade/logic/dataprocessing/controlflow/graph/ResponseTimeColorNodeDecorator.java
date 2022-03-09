package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.graph.Color;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;

import java.util.concurrent.TimeUnit;

/**
 * Decorator to set the color of graph nodes depending on graph nodes execution time.
 *
 * @author Henry Grow
 * @since 1.9
 */
public class ResponseTimeColorNodeDecorator extends AbstractNodeDecorator {

    private static final TimeUnit DISPLAY_TIMEUNIT = TimeUnit.MILLISECONDS;

    private static final Color COLOR = Color.RED;

    private final int threshold;

    /**
     * Creates a new response time decorator.
     *
     * @param threshold The threshold for the execution time to color the graph nodes
     */
    public ResponseTimeColorNodeDecorator(final int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void processMessage(final AbstractMessage message, final DependencyGraphNode<?> sourceNode, final DependencyGraphNode<?> targetNode,
                               final TimeUnit timeunit) {
        // Ignore internal executions
        if (sourceNode.equals(targetNode)) {
            return;
        }

        final long responseTime = message.getReceivingExecution().getTout() - message.getReceivingExecution().getTin();

        final int convertedResponseTime = (int) DISPLAY_TIMEUNIT.convert(responseTime, timeunit);

        if (convertedResponseTime > this.threshold) {
            targetNode.setColor(COLOR);
        }

    }

}
