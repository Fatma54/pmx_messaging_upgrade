package org.palladiosimulator.pmxupgrade.model.systemmodel.util;

import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractTrace;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator which compares abstract traces according to their end timestamps.
 *
 * @author Patrick Treyer
 */
public class TraceEndTimestampComparator implements Comparator<AbstractTrace>, Serializable {

    private static final long serialVersionUID = 7583764582950192813L;

    public TraceEndTimestampComparator() {
        // Empty default constructor
    }

    @Override
    public int compare(final AbstractTrace o1, final AbstractTrace o2) {
        final long endTimestamp1 = o1.getEndTimestamp();
        final long endTimestamp2 = o2.getEndTimestamp();

        if (endTimestamp1 == endTimestamp2) {
            return 0;
        } else if (endTimestamp1 < endTimestamp2) {
            return -1;
        } else {
            return 1;
        }
    }

}
