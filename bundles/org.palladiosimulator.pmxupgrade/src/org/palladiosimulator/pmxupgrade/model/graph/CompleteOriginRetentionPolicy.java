package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * Complete origin retention policy.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public final class CompleteOriginRetentionPolicy extends AbstractOriginRetentionPolicy {

    private static final CompleteOriginRetentionPolicy INSTANCE = new CompleteOriginRetentionPolicy();

    private CompleteOriginRetentionPolicy() {
        super(OriginRetentionPolicyKind.ALL);
    }

    @Override
    public IOriginRetentionPolicy uniteWith(final IOriginRetentionPolicy other) {
        return this;
    }

    @Override
    public <T> void handleOrigin(final AbstractGraphElement<T> element, final T origin) {
        element.addOrigin(origin);
    }

    /**
     * Factory method for the complete origin retention policy.
     *
     * @return See above
     */
    public static CompleteOriginRetentionPolicy createInstance() {
        return INSTANCE;
    }
}
