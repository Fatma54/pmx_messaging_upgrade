package org.palladiosimulator.pmxupgrade.logic.filter;

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Span;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Trace;
import org.junit.jupiter.api.Test;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilterTest {
//checked and works on my traces ~fat

    @Test
    void dateTest() {
        long time = Long.parseLong("1594631346638");
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = format.format(date);

        System.out.println("Date: " + dateString);
    }

    @Test
    void importJSON() throws PMXException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");
        
        //Long time = Long.parseLong("1594631346638000000"); //old value wit some zeros to work
        //Long time = Long.parseLong("1598606488367000000"); //both values are for combination4.json
        Long time = Long.parseLong("1632470994694425000"); //for shortenedTraces.json

        
        configuration.setIgnoreAfterTimestamp(time);

        List<String> traceIdsToFilter = new ArrayList<>();
        traceIdsToFilter.add("180cb19cb3a6d11d");

        configuration.setTraceIDsToFilter(traceIdsToFilter);

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();
        pmxController.initAndExecuteFilters();

        for (Trace trace : pmxController.getTraceRecord().getData()) {
            System.out.println("-----");
            System.out.println("Trace: ");
            System.out.println(trace.getStartTime());
            System.out.println(trace.getTraceID());
            for (Span span : trace.getSpans()) {
                System.out.println("Span: ");
                System.out.println(span.getStartTime());
            }

        }
    }
}
