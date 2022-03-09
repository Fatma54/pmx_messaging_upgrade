package org.palladiosimulator.pmxupgrade.logic.filter.opentracing;

import org.palladiosimulator.pmxupgrade.logic.filter.TraceProcessingInterface;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Span;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Trace;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Filters the tracing data based on a given timestamp.
 *
 * @author Patrick Treyer
 */
public class TimestampFilter implements TraceProcessingInterface {

    public TraceRecord filter(Configuration configuration, TraceRecord traceRecord) {
        List<Trace> tracesToFilter = prepareFiltering(traceRecord).getData();
        
        if (configuration.getIgnoreBeforeTimestamp() != null)
            tracesToFilter.removeIf(p -> p.getStartTime() < configuration.getIgnoreBeforeTimestamp());
        if (configuration.getIgnoreAfterTimestamp() != null)
            tracesToFilter.removeIf(p -> p.getStartTime() > configuration.getIgnoreAfterTimestamp());

        return new TraceRecord(tracesToFilter);
    }

    private TraceRecord prepareFiltering(TraceRecord traceRecord) {
        traceRecord.getData().forEach(t -> {
            Optional<Span> minTimeSpan = t.getSpans().stream().min(Comparator.comparing(Span::getStartTime));
            minTimeSpan.ifPresent(span -> t.setStartTime(span.getStartTime()));
        });
        return traceRecord;
    }
}
