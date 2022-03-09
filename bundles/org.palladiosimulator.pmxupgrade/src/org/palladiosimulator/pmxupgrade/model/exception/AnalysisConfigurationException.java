package org.palladiosimulator.pmxupgrade.model.exception;

/**
 * An exception to show that something went wrong during the configuration of an analysis.
 *
 * @author Patrick Treyer.
 */
public class AnalysisConfigurationException extends Exception {

    private static final long serialVersionUID = -9115316314866942458L;

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param message The message of the exception.
     * @param cause   The cause of the exception.
     */
    public AnalysisConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param message The message of the exception.
     */
    public AnalysisConfigurationException(final String message) {
        super(message);
    }
}
