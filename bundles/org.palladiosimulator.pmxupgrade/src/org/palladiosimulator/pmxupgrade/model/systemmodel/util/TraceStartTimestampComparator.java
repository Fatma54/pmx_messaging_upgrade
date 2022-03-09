package org.palladiosimulator.pmxupgrade.model.systemmodel.util;

import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractTrace;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator which compares abstract traces according to their start timestamps.
 *
 * @author Patrick Treyer
 */
public class TraceStartTimestampComparator implements Comparator<AbstractTrace>, Serializable {

    private static final long serialVersionUID = -8372489878918932083L;

    public TraceStartTimestampComparator() {
        // Empty default constructor
    }

    @Override
    public int compare(final AbstractTrace o1, final AbstractTrace o2) {
        final long startTimestamp1 = o1.getStartTimestamp();
        final long startTimestamp2 = o2.getStartTimestamp();

        if (startTimestamp1 == startTimestamp2) {
            return 0;
        } else if (startTimestamp1 < startTimestamp2) {
            return -1;
        } else {
            return 1;
        }
    }

}
