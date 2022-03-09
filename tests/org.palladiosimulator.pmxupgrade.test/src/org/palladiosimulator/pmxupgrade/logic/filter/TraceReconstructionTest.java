package org.palladiosimulator.pmxupgrade.logic.filter;

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.MessageBasedTraceReconstructionService;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.TraceReconstructionService;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.InvalidTraceException;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.ProcessingObjectWrapper;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class TraceReconstructionTest {

    @Test
    void reconstructTrace() throws PMXException, FileNotFoundException, UnsupportedEncodingException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/combination4.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        TraceReconstructionService filter = new TraceReconstructionService();

        ProcessingObjectWrapper result = filter.reconstructTrace(configuration, pmxController.getTraceRecord());

        result.getSystemModelRepository().saveSystemToHTMLFile("target/system2.html");

        System.out.println("finish");
    }
    
    @Test
    void reconstructTraceFL() throws PMXException, FileNotFoundException, UnsupportedEncodingException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        MessageBasedTraceReconstructionService filter = new MessageBasedTraceReconstructionService();

        ProcessingObjectWrapper result = filter.reconstructTrace(configuration, pmxController.getTraceRecord());

        result.getSystemModelRepository().saveSystemToHTMLFile("target/system3.html");

        System.out.println("finish");
    }
}
