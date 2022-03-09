package org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.palladiosimulator.pmxupgrade.model.inputreader.InputObjectWrapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Full trace record representation of the Opentracing Jaeger data model.
 *
 * @author Patrick Treyer
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraceRecord extends InputObjectWrapper {

    private List<Trace> data;
    public TraceRecord() {
    }

    public TraceRecord(List<Trace> data) {
        this.data = data;
    }

    public List<Trace> getData() {
        return data;
    }

    public void setData(List<Trace> data) {
        this.data = data;
    }
}
