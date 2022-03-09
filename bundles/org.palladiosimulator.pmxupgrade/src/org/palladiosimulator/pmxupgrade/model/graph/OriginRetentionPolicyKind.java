package org.palladiosimulator.pmxupgrade.model.graph;

/**
 * This enumeration contains origin retention policy kinds, i.e. types of origin retention policies.
 * These policies are implemented by subtypes of {@link AbstractOriginRetentionPolicy}, this enum just
 * serves as the basis for the order relation.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public enum OriginRetentionPolicyKind {
    /**
     * Value to denote that no origins should be retained.
     */
    NONE,
    /**
     * Value to denote that only specific origins should be retained.
     */
    SPECIFIC,
    /**
     * Value to denote that all origins should be retained.
     */
    ALL
}
