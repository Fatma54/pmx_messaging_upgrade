package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "monitorrepository:MonitorRepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class MonitorRepository {

    @XmlAttribute(name = "xmlns:xsi")
    private final String xsi = "http://www.w3.org/2001/XMLSchema-instance";

    @XmlAttribute(name = "xmlns:metricspec")
    private final String metricspec = "http://palladiosimulator.org/MetricSpec/1.0";

    @XmlAttribute(name = "xmlns:monitorrepository")
    private final String monitorrepository = "http://palladiosimulator.org/MonitorRepository/1.0";

    @XmlAttribute(name = "xmlns:pcmmeasuringpoint")
    private final String pcmmeasuringpoint = "http://palladiosimulator.org/PCM/MeasuringPoint/1.0";

    @XmlAttribute(name = "id")
    private String id = "_j-U0EJfVEea4dum8SWzrBw";

    @XmlElement(name = "monitors")
    private List<Monitor> monitors;

    public MonitorRepository() {
        this.id = "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getXsi() {
        return xsi;
    }

    public String getMetricspec() {
        return metricspec;
    }

    public String getMonitorrepository() {
        return monitorrepository;
    }

    public String getPcmmeasuringpoint() {
        return pcmmeasuringpoint;
    }

    public String getId() {
        return id;
    }

    public List<Monitor> getMonitors() {
        if (monitors == null) {
            monitors = new ArrayList<>();
        }
        return monitors;
    }

    public void setMonitors(List<Monitor> monitors) {
        this.monitors = monitors;
    }
}
