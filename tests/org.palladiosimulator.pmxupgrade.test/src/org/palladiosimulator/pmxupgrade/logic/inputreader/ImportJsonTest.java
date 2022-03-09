package org.palladiosimulator.pmxupgrade.logic.inputreader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Span;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Trace;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ImportJsonTest {
	//checked and works on my traces ~fat
    @Test
    void readData() {
        ObjectMapper mapper = new ObjectMapper();
        File initialFile = new File("resources/json/shortenedTraces.json");
        try {
            InputStream targetStream = new FileInputStream(initialFile);
            TraceRecord traceRecord = mapper.readValue(targetStream, TraceRecord.class);
            System.out.println(traceRecord);
            
            for (Trace trace : traceRecord.getData()) {
                System.out.println("-----");
                System.out.println("Trace: ");
                System.out.println(trace.getTraceID());
                for (Span span : trace.getSpans()) {
                    System.out.println("Span: ");
                    System.out.println(span.getSpanID());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void importJSON() throws PMXException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        if(pmxController.getInputObjectWrapper() instanceof TraceRecord) {
            TraceRecord traceRecord = (TraceRecord) pmxController.getInputObjectWrapper();

            for (Trace trace : traceRecord.getData()) {
                System.out.println("-----");
                System.out.println("Trace: ");
                System.out.println(trace.getTraceID());
                for (Span span : trace.getSpans()) {
                    System.out.println("Span: ");
                    System.out.println(span.getSpanID());
                }
            }

        }
    }
}
