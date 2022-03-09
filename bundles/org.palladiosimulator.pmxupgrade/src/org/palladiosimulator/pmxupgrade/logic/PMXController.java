package org.palladiosimulator.pmxupgrade.logic;

import org.palladiosimulator.pmxupgrade.logic.dataprocessing.DataProcessingService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.DependencyGraphNode;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.WeightedBidirectionalDependencyGraphEdge;
import org.palladiosimulator.pmxupgrade.logic.filter.opentracing.TimestampFilter;
import org.palladiosimulator.pmxupgrade.logic.filter.opentracing.TraceIdFilter;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.MessageBasedTraceReconstructionService;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.TraceReconstructionService;
import org.palladiosimulator.pmxupgrade.logic.inputreader.InputReaderInterface;
import org.palladiosimulator.pmxupgrade.logic.inputreader.impl.InputReaderOpenTracingImpl;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.PerformanceModelCreationService;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.IModelBuilder;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.constants.PMXConstants;
import org.palladiosimulator.pmxupgrade.model.exception.InvalidTraceException;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.graph.AbstractGraph;
import org.palladiosimulator.pmxupgrade.model.inputreader.InputObjectWrapper;
import org.palladiosimulator.pmxupgrade.model.inputreader.ProcessingObjectWrapper;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.TraceInformation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Central class for the execution of the extraction process. This class is invoked from the specific exporter.
 *
 * @author Patrick Treyer
 */
public class PMXController {

    private final InputReaderInterface inputReaderInterface;
    private final DataProcessingService dataProcessingService;
    private IModelBuilder modelBuilder;

    private Configuration configuration;
    private InputObjectWrapper inputObjectWrapper;
    private TraceRecord traceRecord;
    private ProcessingObjectWrapper processingObjectWrapper;
    private HashMap<String, List<Double>> workload;
    private AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> operationGraph;
    private HashMap<String, Double> resourceDemands;

    /**
     * Constructor for the PMX Controller for the external starting points.
     *
     * @param configuration, the configuration object including the necessary configuration parameters.
     * @throws PMXException, throws an @{@link PMXException} if an unexpected error occurs.
     */
    public PMXController(Configuration configuration) throws PMXException {
        validateConfiguration(configuration);

        this.configuration = configuration;
        inputReaderInterface = new InputReaderOpenTracingImpl();
        dataProcessingService = new DataProcessingService();
    }

    /**
     * Constructor for the PMX Controller for the external starting points.
     *
     * @param configuration, the configuration object including the necessary configuration parameters.
     * @param modelBuilder,  the implementation of the @{@link org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder} for
     *                       the specific model representation.
     * @throws PMXException, throws an @{@link PMXException} if an unexpected error occurs.
     */
    public PMXController(Configuration configuration, IModelBuilder modelBuilder) throws PMXException {
        this(configuration);

        this.modelBuilder = modelBuilder;
    }

    /**
     * This method indicates the starting point of the extraction process and can be called by the external processes.
     *
     * @throws PMXException, if an error occurs during the execution.
     */
    public void buildPerformanceModel() throws PMXException {
        readTracingData();
        initAndExecuteFilters();
        reconstructTrace();
        processTracingData();
        createModel();
    }

    public void readTracingData() throws PMXException {
        try {
            inputObjectWrapper = inputReaderInterface.readTracingData(configuration);
            // todo type check
            traceRecord = (TraceRecord) inputObjectWrapper;
            // TODO Runtime Exception, generelles Exception Handling überdenken - muss es eine Exception sein?
        } catch (IOException e) {
            throw new PMXException(PMXConstants.ERROR_DATA_INPUT + e.getMessage(), e);
        }
    }

    /**
     * Input-dependent Filter-pipeline.
     * Future Work: Producer, Consumer
     */
    public void initAndExecuteFilters() {
        TimestampFilter timestampFilter = new TimestampFilter();
        traceRecord = timestampFilter.filter(configuration, traceRecord);

        TraceIdFilter traceIdFilter = new TraceIdFilter();
        traceRecord = traceIdFilter.filter(configuration, traceRecord);
    }

    /**
     * Input-dependent trace reconstruction.
     */
    private void reconstructTrace() throws PMXException {
    	 //TraceReconstructionService traceReconstructionFilter = new TraceReconstructionService(); 
    	 MessageBasedTraceReconstructionService traceReconstructionFilter = new MessageBasedTraceReconstructionService();//~fat
        try {
            processingObjectWrapper = traceReconstructionFilter.reconstructTrace(configuration, traceRecord);
        } catch (InvalidTraceException e) {
            throw new PMXException(PMXConstants.ERROR_RECONSTRUCT_TRACE + e.getMessage());
        }
    }

    /**
     * Non-input-dependent processing of the tracing data.
     */
    public void processTracingData() {
        workload = dataProcessingService.analyzeWorkload(processingObjectWrapper.getExecutionTraces());
        operationGraph = dataProcessingService.resolveControlFlow(processingObjectWrapper.getExecutionTraces(), processingObjectWrapper.getSystemModelRepository());
        resourceDemands = new HashMap<>();//dataProcessingService.calculateResourceDemands(processingObjectWrapper.getExecutionTraces());
        //Future Work ~fat
        processingObjectWrapper.setSystemModelRepository(dataProcessingService.calculateFailureProbabilities(processingObjectWrapper.getSystemModelRepository()));
        processingObjectWrapper.setSystemModelRepository(dataProcessingService.resolveParametricDependencies(processingObjectWrapper.getSystemModelRepository()));
    }

    public void createModel() throws PMXException {
        PerformanceModelCreationService performanceModelCreationService = new PerformanceModelCreationService(configuration, getModelBuilder(), processingObjectWrapper.getSystemModelRepository());
        performanceModelCreationService.inputWorkload(workload);
        performanceModelCreationService.inputGraph(operationGraph);
        performanceModelCreationService.inputResourceDemands(resourceDemands);

        performanceModelCreationService.createPerformanceModel();
    }

    private void validateConfiguration(Configuration configuration) throws PMXException {
        if (configuration == null) {
            throw new PMXException(PMXConstants.ERROR_CONFIG);
        }
        if (StringUtils.isEmpty(configuration.getOutputDirectory())) {
            throw new PMXException(PMXConstants.ERROR_CONFIG_OUTPUT_DIR);
        }
        if (StringUtils.isEmpty(configuration.getInputFileName())) {
            throw new PMXException(PMXConstants.ERROR_CONFIG_INPUT_DIR);
        }
    }

    public InputObjectWrapper getInputObjectWrapper() {
        return inputObjectWrapper;
    }

    public void setInputObjectWrapper(InputObjectWrapper inputObjectWrapper) {
        this.inputObjectWrapper = inputObjectWrapper;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public TraceRecord getTraceRecord() {
        return traceRecord;
    }

    public void setTraceRecord(TraceRecord traceRecord) {
        this.traceRecord = traceRecord;
    }

    public IModelBuilder getModelBuilder() throws PMXException {
        if (modelBuilder == null) {
            throw new PMXException(PMXConstants.ERROR_BUILDER);
        }
        return modelBuilder;
    }

    public void setModelBuilder(IModelBuilder builder) {
        this.modelBuilder = builder;
    }
}
