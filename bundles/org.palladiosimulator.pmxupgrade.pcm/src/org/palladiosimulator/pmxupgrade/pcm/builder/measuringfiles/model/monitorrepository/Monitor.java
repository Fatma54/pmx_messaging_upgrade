package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class Monitor {

    public static final String ENTITYNAME_UC = "UsageScenarioMonitor";
    public static final String ENTITYNAME_AR = "ActiveResourceMonitor";
    public static final String ENTITYNAME_EC = "ExternalCallActionMonitor";

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "entityName")
    private String entityName;

    @XmlElement(name = "measurementSpecifications")
    private List<MeasurementSpecification> measurementSpecifications;

    @XmlElement(name = "measuringPoint")
    private MeasuringPointRef measuringPoint;

    public Monitor() {
        this.id = "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<MeasurementSpecification> getMeasurementSpecifications() {
        if (measurementSpecifications == null) {
            measurementSpecifications = new ArrayList<>();
        }
        return measurementSpecifications;
    }

    public void setMeasurementSpecifications(List<MeasurementSpecification> measurementSpecifications) {
        this.measurementSpecifications = measurementSpecifications;
    }

    public MeasuringPointRef getMeasuringPoint() {
        return measuringPoint;
    }

    public void setMeasuringPoint(MeasuringPointRef measuringPoint) {
        this.measuringPoint = measuringPoint;
    }
}
