package org.palladiosimulator.pmxupgrade.model.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Configuration object, which can be passed for starting the extraction process.
 *
 * @author Patrick Treyer.
 */
public final class Configuration extends Properties {

    private String inputFileName;
    private String outputDirectory;
    private List<String> traceIDsToFilter;
    private Long ignoreBeforeTimestamp;
    private Long ignoreAfterTimestamp;
    private HashMap<String, Integer> numCores;

    /**
     * Creates a new (empty) configuration.
     */
    public Configuration() {
        this(null);
    }

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param defaults The property object which delivers the default values for the new configuration.
     */
    public Configuration(final Properties defaults) {
        super(defaults);
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public List<String> getTraceIDsToFilter() {
        if (traceIDsToFilter == null)
            return new ArrayList<>();
        return traceIDsToFilter;
    }

    public void setTraceIDsToFilter(List<String> traceIDsToFilter) {
        this.traceIDsToFilter = traceIDsToFilter;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Long getIgnoreBeforeTimestamp() {
        return ignoreBeforeTimestamp;
    }

    public void setIgnoreBeforeTimestamp(Long ignoreBeforeTimestamp) {
        this.ignoreBeforeTimestamp = ignoreBeforeTimestamp;
    }

    public Long getIgnoreAfterTimestamp() {
        return ignoreAfterTimestamp;
    }

    public void setIgnoreAfterTimestamp(Long ignoreAfterTimestamp) {
        this.ignoreAfterTimestamp = ignoreAfterTimestamp;
    }

    public HashMap<String, Integer> getNumCores() {
        return numCores;
    }

    public void setNumCores(HashMap<String, Integer> numCores) {
        this.numCores = numCores;
    }
}
