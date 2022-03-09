package org.palladiosimulator.pmxupgrade.logic.dataprocessing.workload;

import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessageTrace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Service for resolving and analyzing the workload. The workload is then written into an workload time series map
 * for further processing.
 *
 * @author Patrick Treyer
 */
public class WorkloadService {

    private static HashMap<String, List<Double>> workloadTimeSeriesMap;

    /**
     * Analyzes the workload of all gathered execution traces.
     *
     * @param executionTraces, the list of all execution traces
     * @return the generated workloadTimeSeriesMap as {@link HashMap}
     */
    public HashMap<String, List<Double>> analyzeWorkload(final List<ExecutionTrace> executionTraces) {
        workloadTimeSeriesMap = new HashMap<>();
        executionTraces.stream().map(ExecutionTrace::getMessageTrace).forEach(this::analyzeMessageTrace);
        return workloadTimeSeriesMap;
    }

    private void analyzeMessageTrace(final MessageTrace mt) {
        AbstractMessage startMessage = getStartMessage(mt);
        if (startMessage == null)
            return;

        Execution x = startMessage.getReceivingExecution();
        String host = x.getAllocationComponent().getExecutionContainer().getName();
        String function = x.getOperation().getSignature().getName();
        String component = x. getAllocationComponent().getAssemblyComponent().getType().getTypeName();
        addTimeStamp(host, component, function, x.getTin());
    }

    private AbstractMessage getStartMessage(final MessageTrace mt) {
        for (AbstractMessage message : mt.getSequenceAsVector()) {
            if (message.getTimestamp() == mt.getStartTimestamp()) {
                return message;
            }
        }
        if (mt.getSequenceAsVector().isEmpty())
            return null;

        return mt.getSequenceAsVector().get(0);
    }

    private synchronized void addTimeStamp(String host, String component, String function, double timestamp) {
        List<Double> timeSeries;
        String key = function + ModelBuilder.seperatorChar + component + ModelBuilder.seperatorChar + host;
        if (workloadTimeSeriesMap.containsKey(key)) {
            timeSeries = workloadTimeSeriesMap.get(key);
            timeSeries.add(timestamp);
        } else {
            timeSeries = new ArrayList<>();
            timeSeries.add(timestamp);
            workloadTimeSeriesMap.put(key, timeSeries);
        }
    }

}
