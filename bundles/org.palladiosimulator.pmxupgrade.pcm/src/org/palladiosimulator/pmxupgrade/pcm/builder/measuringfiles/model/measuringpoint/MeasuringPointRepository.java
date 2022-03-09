package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.measuringpoint;


import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "org.palladiosimulator.edp2.models:MeasuringPointRepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringPointRepository {

    @XmlAttribute(name = "xmi:version")
    private final String version = "2.0";

    @XmlAttribute(name = "xmlns:xmi")
    private final String xmi = "http://www.omg.org/XMI";

    @XmlAttribute(name = "xmlns:xsi")
    private final String xsi = "http://www.w3.org/2001/XMLSchema-instance";

    @XmlAttribute(name = "xmlns:org.palladiosimulator.edp2.models")
    private final String xxmlns = "http://palladiosimulator.org/EDP2/MeasuringPoint/1.0";

    @XmlAttribute(name = "xmlns:pcmmeasuringpoint")
    private final String xmlns = "http://palladiosimulator.org/PCM/MeasuringPoint/1.0";

    @XmlAttribute(name = "id")
    private String id;

    @XmlElement(name = "measuringPoints")
    private List<MeasuringPoint> measuringPoints;

    public MeasuringPointRepository() {
        this.id = "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getVersion() {
        return version;
    }

    public String getXmi() {
        return xmi;
    }

    public String getXsi() {
        return xsi;
    }

    public String getXxmlns() {
        return xxmlns;
    }

    public String getXmlns() {
        return xmlns;
    }

    public String getId() {
        return id;
    }

    public List<MeasuringPoint> getMeasuringPoints() {
        if (measuringPoints == null) {
            measuringPoints = new ArrayList<>();
        }
        return measuringPoints;
    }

    public void setMeasuringPoints(List<MeasuringPoint> measuringPoints) {
        this.measuringPoints = measuringPoints;
    }
}
