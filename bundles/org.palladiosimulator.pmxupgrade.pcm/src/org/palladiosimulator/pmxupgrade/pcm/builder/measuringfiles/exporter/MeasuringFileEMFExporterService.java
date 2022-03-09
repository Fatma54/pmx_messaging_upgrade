package org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.exporter;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPointRepository;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringpointFactory;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.monitorrepository.MeasurementSpecification;
import org.palladiosimulator.monitorrepository.Monitor;
import org.palladiosimulator.monitorrepository.MonitorRepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.ExternalCallActionMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointFactory;
import org.palladiosimulator.pcmmeasuringpoint.UsageScenarioMeasuringPoint;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.pcm.config.StandaloneSetup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MeasuringFileEMFExporterService {

    public static List<ExternalCallAction> externalCallActions;

    private static List<ActiveResourceMeasuringPoint> activeResourceMeasuringPoints;
    private static List<UsageScenarioMeasuringPoint> usageScenarioMeasuringPoints;
    private static List<ExternalCallActionMeasuringPoint> externalCallActionMeasuringPoints;

    public static void addExternalCall(ExternalCallAction externalCallAction) {
        if (externalCallActions == null) {
            externalCallActions = new ArrayList<>();
        }
        externalCallActions.add(externalCallAction);

    }

    public static void createEMFMeasuringFiles(String outputDir, UsageModel usageModel, ResourceEnvironment resourceEnvironment) throws PMXException {
        ResourceSet rs = new ResourceSetImpl();

       // StandaloneSetup.init();

        createMeasuringPoint(outputDir, usageModel, resourceEnvironment, rs);
        creatMonitoringRepository(outputDir, rs);
    }

    private static void createMeasuringPoint(String outputDir, UsageModel usageModel, ResourceEnvironment resourceEnvironment, ResourceSet rs) throws PMXException {
        MeasuringpointFactory measuringpointFactory = MeasuringpointFactory.eINSTANCE;
        MeasuringPointRepository measuringPointRepository = measuringpointFactory.createMeasuringPointRepository();

        activeResourceMeasuringPoints = extractEMFResources(resourceEnvironment);
        usageScenarioMeasuringPoints = extractEMFUsageScenario(usageModel);
        externalCallActionMeasuringPoints = extractEMFExternalCalls();

        measuringPointRepository.getMeasuringPoints().addAll(activeResourceMeasuringPoints);
        measuringPointRepository.getMeasuringPoints().addAll(usageScenarioMeasuringPoints);
        measuringPointRepository.getMeasuringPoints().addAll(externalCallActionMeasuringPoints);

        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("measuringpoint", new XMIResourceFactoryImpl());

        String relativePath = new File(outputDir + "measuring.measuringpoint").toURI().toString();
        Resource measuringpointResource = rs.createResource(URI.createURI(relativePath));

        measuringpointResource.getContents().add(measuringPointRepository);
        try {
            measuringpointResource.save(Collections.emptyMap());
        } catch (IOException e) {
            throw new PMXException("Error extracting measuringpoints.", e);
        }

    }

    private static void creatMonitoringRepository(String outputDir, ResourceSet rs) throws PMXException {
        MonitorRepositoryFactory monitorRepositoryFactory = MonitorRepositoryFactory.eINSTANCE;
        org.palladiosimulator.monitorrepository.MonitorRepository monitorRepository = monitorRepositoryFactory.createMonitorRepository();

        monitorRepository.getMonitors().addAll(resolveEMFResources(activeResourceMeasuringPoints));
        monitorRepository.getMonitors().addAll(resolveEMFUsageScenarios(usageScenarioMeasuringPoints));
        monitorRepository.getMonitors().addAll(resolveEMFExternalCalls(externalCallActionMeasuringPoints));

        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("monitorrepository", new XMIResourceFactoryImpl());

        String relativePath = new File(outputDir + "measuring.monitorrepository").toURI().toString();
        Resource monitorRepositoryResource = rs.createResource(URI.createURI(relativePath));

        monitorRepositoryResource.getContents().add(monitorRepository);

        try {
            monitorRepositoryResource.save(Collections.emptyMap());
        } catch (IOException e) {
            throw new PMXException("Error extracting monitorrepository.", e);
        }
    }

    private static List<ActiveResourceMeasuringPoint> extractEMFResources(ResourceEnvironment resourceEnvironment) {
        List<ActiveResourceMeasuringPoint> activeResourceMeasuringPoints = new ArrayList<>();

        resourceEnvironment.eResource().setURI(URI.createPlatformResourceURI("extracted.resourceenvironment", true));
        EList<ResourceContainer> resourceContainerList = resourceEnvironment.getResourceContainer_ResourceEnvironment();
        for (ResourceContainer resourceContainer : resourceContainerList) {
            EList<ProcessingResourceSpecification> prs = resourceContainer.getActiveResourceSpecifications_ResourceContainer();

            for (ProcessingResourceSpecification processingResourceSpecification : prs) {
                ActiveResourceMeasuringPoint activeResourceMeasuringPoint = PcmmeasuringpointFactory.eINSTANCE.createActiveResourceMeasuringPoint();
                activeResourceMeasuringPoint.setActiveResource(processingResourceSpecification);
                activeResourceMeasuringPoints.add(activeResourceMeasuringPoint);
            }
        }
        return activeResourceMeasuringPoints;
    }

    private static List<UsageScenarioMeasuringPoint> extractEMFUsageScenario(UsageModel usageModel) {
        List<UsageScenarioMeasuringPoint> usageScenarioMeasuringPoints = new ArrayList<>();

        usageModel.eResource().setURI(URI.createPlatformResourceURI("extracted.usagemodel", true));
        EList<UsageScenario> usageScenarios = usageModel.getUsageScenario_UsageModel();
        for (UsageScenario usageScenario : usageScenarios) {
            UsageScenarioMeasuringPoint usageScenarioMeasuringPoint = PcmmeasuringpointFactory.eINSTANCE.createUsageScenarioMeasuringPoint();
            usageScenarioMeasuringPoint.setUsageScenario(usageScenario);
            usageScenarioMeasuringPoints.add(usageScenarioMeasuringPoint);
        }
        return usageScenarioMeasuringPoints;
    }

    private static List<ExternalCallActionMeasuringPoint> extractEMFExternalCalls() {
        List<ExternalCallActionMeasuringPoint> externalCallActionMeasuringPoints = new ArrayList<>();

        for (ExternalCallAction externalCallAction : getExternalCallActions()) {
            //externalCallAction.eResource().setURI(URI.createPlatformResourceURI("extracted.repository", true));
            ExternalCallActionMeasuringPoint externalCallActionMeasuringPoint = PcmmeasuringpointFactory.eINSTANCE.createExternalCallActionMeasuringPoint();
            externalCallActionMeasuringPoint.setExternalCall(externalCallAction);
            externalCallActionMeasuringPoints.add(externalCallActionMeasuringPoint);
        }
        return externalCallActionMeasuringPoints;
    }

    private static List<Monitor> resolveEMFResources(List<ActiveResourceMeasuringPoint> activeResourceMeasuringPoints) {
        List<Monitor> monitors = new ArrayList<>();

        for (ActiveResourceMeasuringPoint measuringPoint : activeResourceMeasuringPoints) {

            Monitor monitor = MonitorRepositoryFactory.eINSTANCE.createMonitor();

            MeasurementSpecification measurementSpecification1 = MonitorRepositoryFactory.eINSTANCE.createMeasurementSpecification();
            // TODO


            //measurementSpecification1.setMetricDescription(MetricDescriptionConstants.RESOURCE_DEMAND_METRIC);
            //measurementSpecification1.setProcessingType(MonitorRepositoryFactory.eINSTANCE.createFeedThrough());

            MeasurementSpecification measurementSpecification2 = MonitorRepositoryFactory.eINSTANCE.createMeasurementSpecification();
            // TODO
            //measurementSpecification2.setMetricDescription(MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC);
            //measurementSpecification2.setProcessingType(MonitorRepositoryFactory.eINSTANCE.createFeedThrough());

            monitor.setMeasuringPoint(measuringPoint);
            monitor.getMeasurementSpecifications().add(measurementSpecification1);
            //monitor.getMeasurementSpecifications().add(measurementSpecification2);
            monitors.add(monitor);
        }
        return monitors;
    }


    private static List<Monitor> resolveEMFUsageScenarios(List<UsageScenarioMeasuringPoint> usageScenarioMeasuringPoints) {
        List<Monitor> monitors = new ArrayList<>();

        for (UsageScenarioMeasuringPoint measuringPoint : usageScenarioMeasuringPoints) {

            Monitor monitor = MonitorRepositoryFactory.eINSTANCE.createMonitor();

            MeasurementSpecification measurementSpecification = MonitorRepositoryFactory.eINSTANCE.createMeasurementSpecification();
            // TODO
            //measurementSpecification.setMetricDescription(MetricDescriptionConstants.RESPONSE_TIME_METRIC);
            //measurementSpecification.setProcessingType(MonitorRepositoryFactory.eINSTANCE.createFeedThrough());

            monitor.setMeasuringPoint(measuringPoint);
            //monitor.getMeasurementSpecifications().add(measurementSpecification);
            monitors.add(monitor);
        }
        return monitors;
    }

    private static List<Monitor> resolveEMFExternalCalls(List<ExternalCallActionMeasuringPoint> externalCallActionMeasuringPoints) {
        List<Monitor> monitors = new ArrayList<>();

        for (ExternalCallActionMeasuringPoint measuringPoint : externalCallActionMeasuringPoints) {

            Monitor monitor = MonitorRepositoryFactory.eINSTANCE.createMonitor();

            MeasurementSpecification measurementSpecification = MonitorRepositoryFactory.eINSTANCE.createMeasurementSpecification();
            // TODO
            //measurementSpecification.setMetricDescription(MetricDescriptionConstants.RESPONSE_TIME_METRIC);
            //measurementSpecification.setProcessingType(MonitorRepositoryFactory.eINSTANCE.createFeedThrough());

            monitor.setMeasuringPoint(measuringPoint);
            //monitor.getMeasurementSpecifications().add(measurementSpecification);
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
