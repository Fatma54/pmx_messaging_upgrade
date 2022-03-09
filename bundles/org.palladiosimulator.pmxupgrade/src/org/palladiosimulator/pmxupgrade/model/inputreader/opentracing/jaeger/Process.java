package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Process representation of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Process {

    private String id;
    private String serviceName;
    private List<Tag> tags;

    public String getId() {
        if (StringUtils.isEmpty(id)) {
            tags.forEach(t -> {
                if (StringUtils.equals(t.getKey(), "hostname")) {
                    id = t.getValue();
                }
            });
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
