package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.measuringpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringPoint {

    public static final String PCMMEASURINGPOINT_UC = "pcmmeasuringpoint:UsageScenarioMeasuringPoint";
    public static final String PCMMEASURINGPOINT_AR = "pcmmeasuringpoint:ActiveResourceMeasuringPoint";
    public static final String PCMMEASURINGPOINT_EC = "pcmmeasuringpoint:ExternalCallActionMeasuringPoint";

    public static final String PCMRESOURCEURI_UC = "extracted.usagemodel#";
    public static final String PCMRESOURCEURI_AR = "extracted.resourceenvironment#";
    public static final String PCMRESOURCEURI_EC = "extracted.repository#";


    @XmlAttribute(name="xsi:type")
    private String type;

    @XmlAttribute(name="stringRepresentation")
    private String stringRepresentation;

    @XmlAttribute(name="resourceURIRepresentation")
    private String resourceURIRepresentation = "";

    @XmlElement(name="usageScenario")
    private MeasuringPointUC measuringPointUC;

    @XmlElement(name="externalCall")
    private MeasuringPointEC measuringPointEC;

    @XmlElement(name="activeResource")
    private MeasuringPointRS measuringPointRS;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public void setStringRepresentation(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getResourceURIRepresentation() {
        return resourceURIRepresentation;
    }

    public void setResourceURIRepresentation(String resourceURIRepresentation) {
        this.resourceURIRepresentation = resourceURIRepresentation;
    }

    public MeasuringPointUC getMeasuringPointUC() {
        return measuringPointUC;
    }

    public void setMeasuringPointUC(MeasuringPointUC measuringPointUC) {
        this.measuringPointUC = measuringPointUC;
    }

    public MeasuringPointEC getMeasuringPointEC() {
        return measuringPointEC;
    }

    public void setMeasuringPointEC(MeasuringPointEC measuringPointEC) {
        this.measuringPointEC = measuringPointEC;
    }

    public MeasuringPointRS getMeasuringPointRS() {
        return measuringPointRS;
    }

    public void setMeasuringPointRS(MeasuringPointRS measuringPointRS) {
        this.measuringPointRS = measuringPointRS;
    }
}
