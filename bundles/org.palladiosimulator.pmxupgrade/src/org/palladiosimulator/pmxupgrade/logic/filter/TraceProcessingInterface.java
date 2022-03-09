package org.palladiosimulator.pmxupgrade.logic.filter;

import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;

/**
 * General interface for the processing of the traces.
 *
 * @author Patrick Treyer
 */
public interface TraceProcessingInterface {

    TraceRecord filter(Configuration configuration, TraceRecord traceRecord);

}
