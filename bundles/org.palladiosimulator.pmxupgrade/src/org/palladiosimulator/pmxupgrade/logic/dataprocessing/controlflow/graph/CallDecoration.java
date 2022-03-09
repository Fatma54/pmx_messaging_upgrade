package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.graph.AbstractVertexDecoration;

/**
 * Call Decoration for generating the call graph.
 *
 * @author PMX, Universitaet Wuerzburg.
 * @since 1.5
 */
public class CallDecoration extends AbstractVertexDecoration {

    int executionCount;

    public CallDecoration() {
        executionCount = 0;
    }

    @Override
    public String createFormattedOutput() {
        return "" + executionCount;
    }

    public void registerExecution() {
        this.executionCount++;

    }

}
