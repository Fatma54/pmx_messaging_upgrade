package org.palladiosimulator.pmxupgrade.logic.inputreader.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.palladiosimulator.pmxupgrade.logic.inputreader.InputReaderInterface;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.inputreader.InputObjectWrapper;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;

import java.io.*;

/**
 * Reads the tracing data of the OpenTracing standard, which are exported as json data ans stored
 * in a specific directory which is transferred via the configuration object.
 *
 * @author Patrick Treyer
 */
public class InputReaderOpenTracingImpl implements InputReaderInterface {

    @Override
    public InputObjectWrapper readTracingData(Configuration configuration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File initialFile = new File(configuration.getInputFileName());
        InputStream targetStream = new FileInputStream(initialFile);
        return mapper.readValue(targetStream, TraceRecord.class);
    }

}
