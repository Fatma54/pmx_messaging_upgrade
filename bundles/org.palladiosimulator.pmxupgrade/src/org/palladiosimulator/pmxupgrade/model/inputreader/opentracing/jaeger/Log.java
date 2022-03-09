package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Log representation of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Log {

    private long timestamp;
    private List<Tag> fields;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Tag> getFields() {
        return fields;
    }

    public void setFields(List<Tag> fields) {
        this.fields = fields;
    }

}
