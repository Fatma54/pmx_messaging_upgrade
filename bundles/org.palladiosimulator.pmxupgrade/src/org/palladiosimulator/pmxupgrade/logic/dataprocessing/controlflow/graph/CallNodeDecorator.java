package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;

import java.util.concurrent.TimeUnit;

/**
 * Call bode decorator for generating the call graph.
 *
 * @author PMX, Universitaet Wuerzburg.
 * @since 1.5
 */
public class CallNodeDecorator extends AbstractNodeDecorator {

    @Override
    public void processMessage(AbstractMessage message,
                               DependencyGraphNode<?> sourceNode, DependencyGraphNode<?> targetNode,
                               TimeUnit timeUnit) {
        if (sourceNode.equals(targetNode)) {
            return;
        }

        CallDecoration callDecoration = targetNode.getDecoration(CallDecoration.class);
        if (callDecoration == null) {
            callDecoration = new CallDecoration();
            targetNode.addDecoration(callDecoration);
        }

        callDecoration.registerExecution();    //message.getReceivingExecution()
    }

}
