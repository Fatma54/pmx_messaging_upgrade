package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MetricDescription {

    public static final String STATE_OF_ACTIVE_RESOURCE = "paDhIs7qEeOX_4BzImuHbA";
    public static final String RESPONSE_TIME = "6rYmYs7nEeOX_4BzImuHbA";
    public static final String RESOURCE_DEMAND = "eg_F0s7qEeOX_4BzImuHbA";

    @XmlAttribute(name="xsi:type")
    private final String type = "metricspec:NumericalBaseMetricDescription";

    @XmlAttribute(name="href")
    private String href = "pathmap://METRIC_SPEC_MODELS/commonMetrics.metricspec#_";

    public MetricDescription(String metric) {
        this.href = href + metric;
    }

    public String getType() {
        return type;
    }

    public String getHref() {
        return href;
    }
}
