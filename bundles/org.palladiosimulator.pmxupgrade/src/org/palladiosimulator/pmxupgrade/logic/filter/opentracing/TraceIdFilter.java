package org.palladiosimulator.pmxupgrade.logic.filter.opentracing;

import org.palladiosimulator.pmxupgrade.logic.filter.TraceProcessingInterface;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Trace;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;

import java.util.List;

/**
 * Filters the tracing data based on a given black-listed trace id's.
 *
 * @author Patrick Treyer
 */
public class TraceIdFilter implements TraceProcessingInterface {

    public TraceRecord filter(Configuration configuration, TraceRecord traceRecord) {
        List<Trace> tracesToFilter = traceRecord.getData();

        tracesToFilter.removeIf(p -> configuration.getTraceIDsToFilter().contains(p.getTraceID()));

        return new TraceRecord(tracesToFilter);
    }

}
