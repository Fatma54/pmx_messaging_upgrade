package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * Abstract superclass for all origin retention policies.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class AbstractOriginRetentionPolicy implements IOriginRetentionPolicy {

    private final OriginRetentionPolicyKind kind;

    /**
     * This constructor uses the given parameter to initialize the class.
     *
     * @param kind The origin retention policy kind.
     */
    protected AbstractOriginRetentionPolicy(final OriginRetentionPolicyKind kind) {
        this.kind = kind;
    }

    @Override
    public OriginRetentionPolicyKind getKind() {
        return this.kind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWith(final IOriginRetentionPolicy policy) { // NOPMD, for some reason, PMD regards this function as empty
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dependsOn(final IOriginRetentionPolicy policy) {
        return this == policy;
    }
}
