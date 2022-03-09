package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository;

import org.palladiosimulator.pcm.core.CoreFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.UUID;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessingType {

    @XmlAttribute(name="id")
    private String id;

    @XmlAttribute(name="xsi:type")
    private final String type = "monitorrepository:FeedThrough";

    // TODO id, irgendwo her oder neu?
    public ProcessingType() {
        this.id = "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
