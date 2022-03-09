package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Span representation of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Span {

	private String traceID;
	private String spanID;
	private int flags;
	private String operationName;
	private List<Reference> references;
	private Long startTime;
	private Long duration;
	private List<Tag> tags;

	private List<Log> logs;
	private String processID;

	private String component;
	private String componentType;
	private String[] operationParameters;
	private String operationReturnType;
	private String childOf;
	private String kind;

	// added attributes with getters and setters ~fat
	private String topic;
	private String followsFrom;

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

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}

	public Long getStartTime() {
		return TimeUnit.MICROSECONDS.toNanos(startTime);
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getDuration() {
		return TimeUnit.MICROSECONDS.toNanos(duration);
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Log> getLogs() {
		return logs;
	}

	public void setLogs(List<Log> logs) {
		this.logs = logs;
	}

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getComponent() {
		if (StringUtils.isEmpty(component)) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "component"))
					component = t.getValue();
			});
		}
		return component;
	}

	public String getComponentType() {
		if (StringUtils.isEmpty(componentType)) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "componentType"))
					componentType = t.getValue();
			});
		}
		return componentType;
	}

	public String getKind() {
		if (StringUtils.isEmpty(kind)) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "span.kind"))
					kind = t.getValue();
			});
		}
		return kind;
	}

	public String[] getParameters() {
		if (operationParameters == null) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "paramType")) {
					operationParameters = StringUtils.split(t.getValue(), ",");
				}
			});
		}
		return operationParameters;
	}

	public String getReturnType() {
		if (StringUtils.isEmpty(operationReturnType)) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "returnType"))
					operationReturnType = t.getValue();
			});
		}
		return operationReturnType;
	}

	public String getChildOf() {
		if (StringUtils.isEmpty(childOf)) {
			if (this.getReferences() == null)
				return null;

			this.getReferences().forEach(r -> {
				if (StringUtils.equalsIgnoreCase(r.getRefType(), "CHILD_OF"))
					childOf = r.getSpanID();
			});
		}
		return childOf;
	}

	public void setOperationParameters(String[] operationParameters) {
		this.operationParameters = operationParameters;
	}

	/**
	 * this method returns the topic name of each consumer TODO: use this value to
	 * set the topic name of the producer in operations later maybe TODO: code copy
	 * places to be indicated
	 * 
	 * @return the topic name
	 */
	public String getTopic() {
		if (StringUtils.isEmpty(topic) && !this.getLogs().isEmpty()) {
			this.getLogs().get(0).getFields().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "thread"))
					if (t.getValue().contains("consumerDestinationName")) {
						Pattern pattern = Pattern.compile("'(.*?)'");
						Matcher matcher = pattern.matcher(t.getValue());
						if (matcher.find()) {
							topic = matcher.group(1);
						}
					}
			});
		}
		if (topic == null && !(this.component == null) && !this.component.equalsIgnoreCase("spring-messaging")) {
			this.getTags().forEach(t -> {
				if (StringUtils.equalsIgnoreCase(t.getKey(), "message_bus.destination"))
					topic = t.getValue();

			});
		}

		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getFollowsFrom() {
		if (StringUtils.isEmpty(followsFrom)) {
			if (this.getReferences() == null)
				return null;

			this.getReferences().forEach(r -> {
				if (StringUtils.equalsIgnoreCase(r.getRefType(), "FOLLOWS_FROM"))
					followsFrom = r.getSpanID();
			});
		}
		return followsFrom;
	}

	public void setFollowsFrom(String followsFrom) {
		this.followsFrom = followsFrom;
	}

	public String getParent() {
		if (this.getChildOf() != null) {
			return this.getChildOf();
		}
		return this.getFollowsFrom();
	}

}
