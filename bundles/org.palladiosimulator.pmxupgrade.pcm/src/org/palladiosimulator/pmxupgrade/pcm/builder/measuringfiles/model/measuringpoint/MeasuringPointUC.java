package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.measuringpoint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class MeasuringPointUC {

    @XmlAttribute(name = "href")
    private String href = "extracted.usagemodel#";

    public MeasuringPointUC(String id) {
        this.href = href + id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

}
