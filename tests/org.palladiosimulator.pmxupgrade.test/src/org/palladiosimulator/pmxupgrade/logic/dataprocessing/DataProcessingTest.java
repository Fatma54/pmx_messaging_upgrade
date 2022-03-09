package org.palladiosimulator.pmxupgrade.logic.dataprocessing;

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.junit.jupiter.api.Test;

public class DataProcessingTest {
	//does not seem to work ~fat
    @Test
    void detectComponents() throws PMXException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/combination4.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        SystemModelRepository systemModelRepository = new SystemModelRepository();

        if (!(pmxController.getInputObjectWrapper() instanceof TraceRecord))
            return;

        TraceRecord traceRecord = (TraceRecord) pmxController.getInputObjectWrapper();

        //ComponentDetectionService service = new ComponentDetectionService();
        //systemModelRepository = service.detectComponents(systemModelRepository, traceRecord);

        for (ExecutionContainer container: systemModelRepository.getExecutionEnvironmentFactory().getExecutionContainers()){
            System.out.println("Container: " + container.getIdentifier());
        }
    }
    
    @Test
    void detectComponentsFL() throws PMXException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        SystemModelRepository systemModelRepository = new SystemModelRepository();

        if (!(pmxController.getInputObjectWrapper() instanceof TraceRecord))
            return;

        TraceRecord traceRecord = (TraceRecord) pmxController.getInputObjectWrapper();

        //ComponentDetectionService service = new ComponentDetectionService();
        //systemModelRepository = service.detectComponents(systemModelRepository, traceRecord);

        for (ExecutionContainer container: systemModelRepository.getExecutionEnvironmentFactory().getExecutionContainers()){
            System.out.println("Container: " + container.getIdentifier());
        }
    }

}
