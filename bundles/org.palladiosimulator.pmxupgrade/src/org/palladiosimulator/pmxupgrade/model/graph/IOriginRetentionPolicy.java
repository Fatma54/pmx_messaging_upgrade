package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * Origin retention policies control the way in which origins for graph elements are kept. Using such
 * policies, graph consumers may specify precisely which origin data needs to be kept and avoid wasting
 * resources for storing unneccessary data.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public interface IOriginRetentionPolicy {

    /**
     * Returns the kind of this retention policy.
     *
     * @return See above
     * @since 1.6
     */
    OriginRetentionPolicyKind getKind();

    /**
     * Checks whether this policy is compatible (i.e. unitable) with another retention policy.
     *
     * @param other The retention policy to check against
     * @return {@code True} if the policies may be united, {@code false} otherwise
     * @since 1.6
     */
    boolean isCompatibleWith(final IOriginRetentionPolicy other);

    /**
     * Checks whether this origin retention policy depends on the given policy.
     *
     * @param policy The policy to check for dependencies
     * @return {@code True} if the policy depends on the given policy, {@code false} otherwise
     * @since 1.6
     */
    boolean dependsOn(IOriginRetentionPolicy policy);

    /**
     * Unites this retention policy with another one and returns the resulting policy. Note that the
     * resulting policy is not necessarily one of the original policies.
     *
     * @param other The retention policy to unite this policy with
     * @return The resulting policy, which may be a completely new object
     * @since 1.6
     */
    IOriginRetentionPolicy uniteWith(IOriginRetentionPolicy other);

    /**
     * Handles a given origin for the given graph element.
     *
     * @param element The graph element to handle the origin for
     * @param origin  The origin to handle
     * @param <T>     The type of the entity within the graph element.
     * @since 1.6
     */
    <T> void handleOrigin(AbstractGraphElement<T> element, T origin);

}
