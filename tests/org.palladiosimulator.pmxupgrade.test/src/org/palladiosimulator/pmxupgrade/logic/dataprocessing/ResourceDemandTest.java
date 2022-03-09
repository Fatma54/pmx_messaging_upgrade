package org.palladiosimulator.pmxupgrade.logic.dataprocessing;

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.MessageBasedTraceReconstructionService;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.TraceReconstructionService;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.InvalidTraceException;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.ProcessingObjectWrapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class ResourceDemandTest {

    @Test
    void filter() throws PMXException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/combination4.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        TraceReconstructionService traceReconstructionService = new TraceReconstructionService();

        ProcessingObjectWrapper processingObjectWrapper = traceReconstructionService.reconstructTrace(configuration, pmxController.getTraceRecord());

        DataProcessingService service = new DataProcessingService();
        HashMap<String, Double> estimationResults =  service.calculateResourceDemands(processingObjectWrapper.getExecutionTraces());

        System.out.println("HashMap<String, Double>: " + estimationResults.size());

    }
    
    //~fat
    @Test
    void filterFL() throws PMXException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        MessageBasedTraceReconstructionService traceReconstructionService = new MessageBasedTraceReconstructionService();

        ProcessingObjectWrapper processingObjectWrapper = traceReconstructionService.reconstructTrace(configuration, pmxController.getTraceRecord());

        DataProcessingService service = new DataProcessingService();
        HashMap<String, Double> estimationResults =  service.calculateResourceDemands(processingObjectWrapper.getExecutionTraces());

        System.out.println("HashMap<String, Double>: " + estimationResults.size());

    }
}
