package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringPointRef {

    public static final String PCMMEASURINGPOINT_UC = "pcmmeasuringpoint:UsageScenarioMeasuringPoint";
    public static final String PCMMEASURINGPOINT_AR = "pcmmeasuringpoint:ActiveResourceMeasuringPoint";
    public static final String PCMMEASURINGPOINT_EC = "pcmmeasuringpoint:ExternalCallActionMeasuringPoint";

    public MeasuringPointRef(int id) {
        this.href = href + id;
    }

    @XmlAttribute(name="xsi:type")
    private String type;

    @XmlAttribute(name="href")
    private String href = "measuring.measuringpoint#//@measuringPoints.";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
