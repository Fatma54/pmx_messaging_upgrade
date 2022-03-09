package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.exporter;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.measuringpoint.*;
import org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.model.monitorrepository.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeasuringFileExporterService {

    public static List<ExternalCallAction> externalCallActions;

    public static void addExternalCall(ExternalCallAction externalCallAction) {
        if (externalCallActions == null) {
            externalCallActions = new ArrayList<>();
        }
        externalCallActions.add(externalCallAction);

    }

    public static void createMeasuringFiles(String outputDir, UsageModel usageModel, ResourceEnvironment resourceEnvironment) {
        MeasuringPointRepository measuringPointRepository = new MeasuringPointRepository();


        List<MeasuringPoint> resources = extractResources(resourceEnvironment);
        List<MeasuringPoint> usageScenarios = extractUsageScenario(usageModel);
        List<MeasuringPoint> externalCalls = extractExternalCalls();

        measuringPointRepository.getMeasuringPoints().addAll(resources);
        measuringPointRepository.getMeasuringPoints().addAll(usageScenarios);
        measuringPointRepository.getMeasuringPoints().addAll(externalCalls);

        MonitorRepository monitorRepository = new MonitorRepository();
        monitorRepository.getMonitors().addAll(resolveResources(resources, measuringPointRepository));
        monitorRepository.getMonitors().addAll(resolveUsageScenarios(usageScenarios, measuringPointRepository));
        monitorRepository.getMonitors().addAll(resolveExternalCalls(externalCalls, measuringPointRepository));

        try {
            JAXBContext jCont = JAXBContext.newInstance(MeasuringPointRepository.class);
            Marshaller marshal = jCont.createMarshaller();
            marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshal.marshal(measuringPointRepository, new File(outputDir + "measuring2.measuringpoint"));

            JAXBContext jCont2 = JAXBContext.newInstance(MonitorRepository.class);
            Marshaller marshal2 = jCont2.createMarshaller();
            marshal2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshal2.marshal(monitorRepository, new File(outputDir + "measuring2.monitorrepository"));

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    private static List<MeasuringPoint> extractResources(ResourceEnvironment resourceEnvironment) {
        List<MeasuringPoint> result = new ArrayList<>();

        EList<ResourceContainer> resourceContainerList = resourceEnvironment.getResourceContainer_ResourceEnvironment();
        for (ResourceContainer resourceContainer : resourceContainerList) {
            EList<ProcessingResourceSpecification> prs = resourceContainer.getActiveResourceSpecifications_ResourceContainer();

            for (ProcessingResourceSpecification processingResourceSpecification : prs) {
                MeasuringPoint measuringPoint = new MeasuringPoint();
                measuringPoint.setType(MeasuringPoint.PCMMEASURINGPOINT_AR);
                measuringPoint.setStringRepresentation(resourceContainer.getEntityName() + ", CPU");
                measuringPoint.setResourceURIRepresentation(MeasuringPoint.PCMRESOURCEURI_AR + processingResourceSpecification.getId());
                measuringPoint.setMeasuringPointRS(new MeasuringPointRS(processingResourceSpecification.getId()));
                result.add(measuringPoint);
            }
        }

        return result;
    }

    private static List<MeasuringPoint> extractUsageScenario(UsageModel usageModel) {
        List<MeasuringPoint> result = new ArrayList<>();

        EList<UsageScenario> usageScenarios = usageModel.getUsageScenario_UsageModel();

        for (UsageScenario usageScenario : usageScenarios) {
            MeasuringPoint measuringPoint = new MeasuringPoint();
            measuringPoint.setType(MeasuringPoint.PCMMEASURINGPOINT_UC);
            measuringPoint.setStringRepresentation("Usage Scenario: " + usageScenario.getEntityName());
            measuringPoint.setResourceURIRepresentation(MeasuringPoint.PCMRESOURCEURI_UC + usageScenario.getId());
            measuringPoint.setMeasuringPointUC(new MeasuringPointUC(usageScenario.getId()));
            result.add(measuringPoint);
        }
        return result;
    }

    private static List<MeasuringPoint> extractExternalCalls() {
        List<MeasuringPoint> result = new ArrayList<>();

        for (ExternalCallAction externalCallAction : getExternalCallActions()) {
            MeasuringPoint measuringPoint = new MeasuringPoint();
            measuringPoint.setType(MeasuringPoint.PCMMEASURINGPOINT_EC);
            measuringPoint.setStringRepresentation("External Call: " + externalCallAction.getEntityName() + ": " + externalCallAction.getId());
            measuringPoint.setResourceURIRepresentation(MeasuringPoint.PCMRESOURCEURI_EC + externalCallAction.getId());
            measuringPoint.setMeasuringPointEC(new MeasuringPointEC(externalCallAction.getId()));
            result.add(measuringPoint);
        }
        return result;
    }

    private static List<Monitor> resolveResources(List<MeasuringPoint> resources, MeasuringPointRepository measuringPointRepository) {
        List<Monitor> monitors = new ArrayList<>();

        for (MeasuringPoint measuringPoint : resources) {
            Monitor monitor = new Monitor();
            monitor.setEntityName(Monitor.ENTITYNAME_AR);

            MeasurementSpecification measurementSpecificationActiveResource = new MeasurementSpecification();
            measurementSpecificationActiveResource.setMetricDescription(new MetricDescription(MetricDescription.STATE_OF_ACTIVE_RESOURCE));
            measurementSpecificationActiveResource.setProcessingType(new ProcessingType());

            MeasurementSpecification measurementSpecificationResourceDemand = new MeasurementSpecification();
            measurementSpecificationResourceDemand.setMetricDescription(new MetricDescription(MetricDescription.RESOURCE_DEMAND));
            measurementSpecificationResourceDemand.setProcessingType(new ProcessingType());

            monitor.getMeasurementSpecifications().add(measurementSpecificationActiveResource);
            monitor.getMeasurementSpecifications().add(measurementSpecificationResourceDemand);

            int index = measuringPointRepository.getMeasuringPoints().indexOf(measuringPoint);
            monitor.setMeasuringPoint(new MeasuringPointRef(index));
            monitor.getMeasuringPoint().setType(MeasuringPointRef.PCMMEASURINGPOINT_AR);

            monitors.add(monitor);
        }
        return monitors;
    }

    private static List<Monitor> resolveUsageScenarios(List<MeasuringPoint> usageScenarios, MeasuringPointRepository measuringPointRepository) {
        List<Monitor> monitors = new ArrayList<>();

        for (MeasuringPoint measuringPoint : usageScenarios) {
            Monitor monitor = new Monitor();
            monitor.setEntityName(Monitor.ENTITYNAME_UC);

            MeasurementSpecification measurementSpecification = new MeasurementSpecification();
            measurementSpecification.setMetricDescription(new MetricDescription(MetricDescription.RESPONSE_TIME));
            measurementSpecification.setProcessingType(new ProcessingType());

            monitor.getMeasurementSpecifications().add(measurementSpecification);

            int index = measuringPointRepository.getMeasuringPoints().indexOf(measuringPoint);
            monitor.setMeasuringPoint(new MeasuringPointRef(index));
            monitor.getMeasuringPoint().setType(MeasuringPointRef.PCMMEASURINGPOINT_UC);

            monitors.add(monitor);
        }

        return monitors;
    }

    private static List<Monitor> resolveExternalCalls(List<MeasuringPoint> externalCalls, MeasuringPointRepository measuringPointRepository) {
        List<Monitor> monitors = new ArrayList<>();

        for (MeasuringPoint measuringPoint : externalCalls) {
            Monitor monitor = new Monitor();
            monitor.setEntityName(Monitor.ENTITYNAME_EC);

            MeasurementSpecification measurementSpecification = new MeasurementSpecification();
            measurementSpecification.setMetricDescription(new MetricDescription(MetricDescription.RESPONSE_TIME));
            measurementSpecification.setProcessingType(new ProcessingType());

            monitor.getMeasurementSpecifications().add(measurementSpecification);

            int index = measuringPointRepository.getMeasuringPoints().indexOf(measuringPoint);
            monitor.setMeasuringPoint(new MeasuringPointRef(index));
            monitor.getMeasuringPoint().setType(MeasuringPointRef.PCMMEASURINGPOINT_EC);

            monitors.add(monitor);
        }

        return monitors;
    }

    public static List<ExternalCallAction> getExternalCallActions() {
        if (externalCallActions == null) {
            externalCallActions = new ArrayList<>();
        }
        return externalCallActions;
    }


}
