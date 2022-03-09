package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;

import java.util.concurrent.TimeUnit;

/**
 * Decorator to attach response time data to graph nodes.
 * 
 * @author PMX, Universitaet Wuerzburg.
 * 
 * @since 1.5
 */
public class ResponseTimeNodeDecorator extends AbstractNodeDecorator {

	private final TimeUnit displayTimeunit;

	/**
	 * Creates a new response time decorator.
	 */
	public ResponseTimeNodeDecorator(final TimeUnit displayTimeunit) {
		this.displayTimeunit = displayTimeunit;
	}

	@Override
	public void processMessage(final AbstractMessage message, final DependencyGraphNode<?> sourceNode, final DependencyGraphNode<?> targetNode,
							   final TimeUnit timeunit) {
		// Ignore internal executions
		if (sourceNode.equals(targetNode)) {
			return;
		}

		ResponseTimeDecoration timeDecoration = targetNode.getDecoration(ResponseTimeDecoration.class);

		if (timeDecoration == null) {
			timeDecoration = new ResponseTimeDecoration(timeunit, this.displayTimeunit);
			targetNode.addDecoration(timeDecoration);
		}

		timeDecoration.registerExecution(message.getReceivingExecution());
	}

}
