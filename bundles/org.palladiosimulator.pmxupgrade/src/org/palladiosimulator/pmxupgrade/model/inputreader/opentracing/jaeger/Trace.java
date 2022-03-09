package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Trace representation of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {

    private String traceID;
    private List<Span> spans;
    private Map<String, Process> processes;

    @JsonIgnore
    private Long startTime;

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public List<Span> getSpans() {
        if (spans == null) {
            spans = new ArrayList<>();
        }
        return spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public Map<String, Process> getProcesses() {
        return processes;
    }

    public void setProcesses(Map<String, Process> processes) {
        this.processes = processes;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    /**
     * 
     * @param spanId the id of span we look for children for
     * @return the spans that continue their work after the current span wih id spanId if child span 
     * stops immediately it will not be included in the result
     */
    public Set<Span> getSpanChildren(String spanId) {
    	Set <Span> result = new HashSet<Span>();
    	for (Span span: spans) {
    		if((span.getChildOf() != null && span.getChildOf().equalsIgnoreCase(spanId)) 
    				|| (span.getFollowsFrom()!= null && span.getFollowsFrom().equalsIgnoreCase(spanId))) {
    					 
    			if (! (span.getComponent().equalsIgnoreCase("spring-messaging") && !hasChildren(span.getSpanID()))) {
    				result.add(span);
    			}
    			
    		}
    	}
    	return result;
    }
    
    public boolean hasChildren(String spanId) {
    	for(Span span: spans) {
    		if((span.getChildOf() != null && span.getChildOf().equals(spanId))
    				|| (span.getFollowsFrom() != null && span.getFollowsFrom().equals(spanId))) {
    			return true;
    		}
    	}
    		return false;
    }
}
