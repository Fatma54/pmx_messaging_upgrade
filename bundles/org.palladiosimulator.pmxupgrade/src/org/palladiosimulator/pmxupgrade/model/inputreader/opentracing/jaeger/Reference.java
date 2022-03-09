package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * References of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reference {

    private String refType;
    private String traceID;
    private String spanID;

    public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getSpanID() {
        return spanID;
    }

    public void setSpanID(String spanID) {
        this.spanID = spanID;
    }


}
