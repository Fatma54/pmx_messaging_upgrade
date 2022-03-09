package org.palladiosimulator.pmxupgrade.model.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Specific origin retention policy.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class SpecificOriginRetentionPolicy extends AbstractOriginRetentionPolicy {

    private final Set<Object> selectedOrigins = new HashSet<>();

    private IOriginRetentionPolicy successor;

    protected SpecificOriginRetentionPolicy(final Set<?> selectedOrigins) {
        super(OriginRetentionPolicyKind.SPECIFIC);
        this.selectedOrigins.addAll(selectedOrigins);
    }

    @Override
    public boolean dependsOn(final IOriginRetentionPolicy policy) {
        if (this == policy) {
            return true;
        } else if (this.successor == null) {
            return false;
        } else {
            return this.successor.equals(policy) || this.successor.dependsOn(policy);
        }
    }

    @Override
    public IOriginRetentionPolicy uniteWith(final IOriginRetentionPolicy other) {
        if (other == null) {
            return this;
        }

        // Do not add a default case to the following switch to avoid the suppression of warnings
        // about missing enum values.
        switch (other.getKind()) { // NOPMD NOCS
            case NONE:
                return this;
            case SPECIFIC:
                if (other.dependsOn(this)) {
                    throw new IllegalArgumentException(other.toString());
                }

                this.successor = other;
                return this;
            case ALL:
                return other;
        }

        throw new IllegalArgumentException(other.toString());
    }

    @Override
    public <T> void handleOrigin(final AbstractGraphElement<T> element, final T origin) {
        if (this.selectedOrigins.contains(origin)) {
            element.addOrigin(origin);
        }

        if (this.successor != null) {
            this.successor.handleOrigin(element, origin);
        }
    }

    /**
     * Factory method for the specific origin retention policy.
     *
     * @param selectedOrigins The origins to retain
     * @return See above
     */
    public static SpecificOriginRetentionPolicy createInstance(final Set<?> selectedOrigins) {
        return new SpecificOriginRetentionPolicy(selectedOrigins);
    }
}
