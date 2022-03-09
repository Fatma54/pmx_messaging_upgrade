package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeasurementSpecification {


    @XmlAttribute(name="id")
    private String id;

    @XmlElement(name="metricDescription")
    private MetricDescription metricDescription;

    @XmlElement(name="processingType")
    private ProcessingType processingType;

    public MeasurementSpecification() {
        this.id = "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getId() {
        return id;
    }

    public MetricDescription getMetricDescription() {
        return metricDescription;
    }

    public void setMetricDescription(MetricDescription metricDescription) {
        this.metricDescription = metricDescription;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }
}
