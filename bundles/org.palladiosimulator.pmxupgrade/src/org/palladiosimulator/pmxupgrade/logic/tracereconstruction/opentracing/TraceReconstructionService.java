package org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing;

import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.TraceReconstructionInterface;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.InvalidTraceException;
import org.palladiosimulator.pmxupgrade.model.inputreader.ProcessingObjectWrapper;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Process;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Span;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.Trace;
import org.palladiosimulator.pmxupgrade.model.inputreader.opentracing.jaeger.TraceRecord;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.OperationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for reconstructing the traces. This refers to the transformation of the input-dependent
 * tracing data into an uniform internal data format. In addition, the system architecture is extracted as this
 * is also an input-dependent step.
 *
 * @author Patrick Treyer
 */
public class TraceReconstructionService implements TraceReconstructionInterface {

    private final String[] emptyArray = new String[0];
    private AtomicInteger numberOfValidExecutions = new AtomicInteger(-1);

    private SystemModelRepository systemModelRepository;
    private List<ExecutionTrace> executionTraces = new ArrayList<>();
    private List<Execution> invalidExecutions = new ArrayList<>();

    private final String GENERIC_ASSEMBLY_COMPONENT_TYPE = "org.palladiosimulatpr.pmxupgrade.generic.";
    private final String UNKNOWN_ASSEMBLY_COMPONENT_TYPE = "unknown";
    private final String DATABASE_CALL = "databaseCall";
    private final String NETWORK_CALL = "networkCall";
    private final String HTTP_CLIENT = "HttpClient";
    private final String DATABASE_DRIVER = "DatabaseDriver";
    private final String SRV_INDICATION = "-SRV";


    @Override
    public ProcessingObjectWrapper reconstructTrace(Configuration configuration, TraceRecord traceRecord) throws InvalidTraceException {
        systemModelRepository = new SystemModelRepository();

        for (Trace t : traceRecord.getData()) {
            executionTraces.add(mapExecutionTraces(t));
        }
        executionTraces.forEach(this::mapMessageTraces);

        return new ProcessingObjectWrapper(systemModelRepository, executionTraces, invalidExecutions);
    }

    /**
     * Transforms the trace into an internal data format and extracts information about the system architecture.
     *
     * @param trace, the {@link Trace} which has to be transformed.
     * @return the transformed @{@link Execution}
     * @throws InvalidTraceException, if the trace is invalid.
     */
    private ExecutionTrace mapExecutionTraces(Trace trace) throws InvalidTraceException {
        numberOfValidExecutions.set(-1);

        ExecutionTrace executionTrace = new ExecutionTrace(trace.getTraceID());
        invalidExecutions = new ArrayList<>();
        
        HashMap<String, String> executionContainer = new HashMap<>();
        // map.Entry = key-value pair, here we get the set of all entries of the map (for understanding)
        for (Map.Entry<String, Process> stringProcessEntry : trace.getProcesses().entrySet()) {
            Entry<String, Process> pair = stringProcessEntry;
            Process p = (Process) pair.getValue();
            String executionContainerName = p.getServiceName().toUpperCase() + SRV_INDICATION;
            //TODO: why is this line here? (asked for understanding)
            systemModelRepository.getExecutionEnvironmentFactory()
                    .lookupExecutionContainerByNamedIdentifier(executionContainerName);
            executionContainer.put((String) pair.getKey(), executionContainerName);
        }

        for (Span t : trace.getSpans()) {
            Execution execution = mapExecution(t, executionContainer);
            if (execution != null)
                executionTrace.add(execution);
        }
        executionTrace.getInvalidExecutions().addAll(invalidExecutions);
        return executionTrace;
    }

    /**
     * Custom processing of the data to transfer the tracing data into a uniform data model. Therefore,
     * technical component names are abstracted. After that, the system architecture is extracted.
     *
     * @param span,               the corresponding @{@link Span} to be processed.
     * @param executionContainer, the {@link ExecutionContainer} of the transferred span.
     * @return the created {@link Execution} if the an obtained @{@link Span}.
     */
    private Execution mapExecution(Span span, HashMap<String, String> executionContainer) {
        String executionContainerId = span.getProcessID();

        final String executionContainerName = executionContainer.get(executionContainerId);
        String assemblyComponentTypeName = span.getComponentType();
        String allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
        String operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

        if (StringUtils.isEmpty(assemblyComponentTypeName)) {

            long tout = span.getStartTime() + span.getDuration();

            if (StringUtils.equalsIgnoreCase(span.getOperationName(), "GET") | StringUtils.equalsIgnoreCase(span.getOperationName(), "POST") |
                    StringUtils.equalsIgnoreCase(span.getOperationName(), "PUT") | StringUtils.equalsIgnoreCase(span.getOperationName(), "DELETE")
                    | StringUtils.equalsIgnoreCase(span.getOperationName(), "HEAD") | StringUtils.equalsIgnoreCase(span.getOperationName(), "OPTIONS")) {
                // resolve http requests
                assemblyComponentTypeName = resolveHttpRequests(span);

                allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
                span.setOperationName(NETWORK_CALL);
                span.setOperationParameters(emptyArray);
                operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();


            } else if (StringUtils.equalsIgnoreCase(span.getOperationName(), "QUERY") | StringUtils.equalsIgnoreCase(span.getOperationName(), "UPDATE")
                    | StringUtils.equalsIgnoreCase(span.getOperationName(), "DELETE") | StringUtils.equalsIgnoreCase(span.getOperationName(), "SELECT")) {
                // resolve database queries
                assemblyComponentTypeName = resolveDatabaseQueries(span);

                allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
                span.setOperationName(DATABASE_CALL);
                span.setOperationParameters(emptyArray);
                operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

            } else {
                // invalid
                Operation op = new Operation(-1, null, new Signature(span.getOperationName(), new String[0], null, new String[0]));
                invalidExecutions.add(new Execution(op, new AllocationComponent(-1, null, null), span.getTraceID(), span.getSpanID(), Execution.NO_SESSION_ID, span.getFollowsFrom(), -1, -1, span.getStartTime(), tout, false));
                return null;
            }
        }
        numberOfValidExecutions.getAndIncrement();

        AllocationComponent allocInst = systemModelRepository.getAllocationFactory()
                .lookupAllocationComponentInstanceByNamedIdentifier(allocationComponentName);

        return resolveSystemArchitecture(span, executionContainerName, assemblyComponentTypeName, allocationComponentName, operationFactoryName, allocInst);
    }

    /**
     * Extracts the system architecture out of the data. This process is executed incrementally for each span in order to
     * not miss an details and achieve a precise architecture.
     *
     * @param span,                      the @{@link Span} to be processed.
     * @param executionContainerName,    name of the {@link ExecutionContainer}
     * @param assemblyComponentTypeName, name of the {@link AssemblyComponent}
     * @param allocationComponentName,   name of the {@link AllocationComponent}
     * @param operationFactoryName,      name of the {@link Operation}.
     * @param allocInst,                 the corresponding allocation instance.
     * @return the created {@link Execution} if the an obtained @{@link Span}.
     */
    private Execution resolveSystemArchitecture(Span span, String executionContainerName, String assemblyComponentTypeName, String allocationComponentName, String operationFactoryName, AllocationComponent allocInst) {
        // Allocation component instance doesn't exist
        if (allocInst == null) {
            AssemblyComponent assemblyComponent = systemModelRepository.getAssemblyFactory()
                    .lookupAssemblyComponentInstanceByNamedIdentifier(assemblyComponentTypeName);
            if (assemblyComponent == null) {
                ComponentType componentType = systemModelRepository.getTypeRepositoryFactory().lookupComponentTypeByNamedIdentifier(assemblyComponentTypeName, "BASICCOMPONENT" );

                if (componentType == null) {
                    componentType = systemModelRepository.getTypeRepositoryFactory().createAndRegisterComponentType(assemblyComponentTypeName,
                            assemblyComponentTypeName, "BASICCOMPONENT");
                }
                assemblyComponent = systemModelRepository.getAssemblyFactory()
                        .createAndRegisterAssemblyComponentInstance(assemblyComponentTypeName, componentType);
            }

            ExecutionContainer execContainer = systemModelRepository.getExecutionEnvironmentFactory()
                    .lookupExecutionContainerByNamedIdentifier(executionContainerName);
            if (execContainer == null) {
                execContainer = systemModelRepository.getExecutionEnvironmentFactory()
                        .createAndRegisterExecutionContainer(executionContainerName);
            }

            allocInst = systemModelRepository.getAllocationFactory()
                    .createAndRegisterAllocationComponentInstance(allocationComponentName, assemblyComponent, execContainer);
        }

        Signature operationSignature = new Signature(span.getOperationName(), emptyArray, span.getReturnType(), span.getParameters());
        Operation op = systemModelRepository.getOperationFactory().lookupOperationByNamedIdentifier(operationFactoryName);

        // Operation doesn't exist
        if (op == null) {
            op = systemModelRepository.getOperationFactory()
                    .createAndRegisterOperation(operationFactoryName, allocInst.getAssemblyComponent().getType(), operationSignature);
            allocInst.getAssemblyComponent().getType().addOperation(op);
        }

        long tout = span.getStartTime() + span.getDuration();
        return new Execution(op, allocInst, span.getTraceID(), span.getSpanID(), Execution.NO_SESSION_ID, span.getFollowsFrom(), numberOfValidExecutions.get(), numberOfValidExecutions.get(), span.getStartTime(), tout, false);
    }

    private String resolveDatabaseQueries(Span span) {
        String assemblyComponentTypeName;
        String component = span.getComponent();
        if (StringUtils.isNotEmpty(span.getComponent()) && !component.startsWith(UNKNOWN_ASSEMBLY_COMPONENT_TYPE)) {
            String name = StringUtils.replace(component, "-", " ");
            String capitalizedName = WordUtils.capitalize(name).replace(" ", "");
            assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + capitalizedName;
        } else {
            assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + DATABASE_DRIVER;
        }
        return assemblyComponentTypeName;
    }

    private String resolveHttpRequests(Span span) {
        String assemblyComponentTypeName;
        String component = span.getComponent();

        // look if there is a component auto instrumented, if not use generic component
        if (StringUtils.isNotEmpty(span.getComponent()) && !component.startsWith(UNKNOWN_ASSEMBLY_COMPONENT_TYPE)) {
            String name = StringUtils.replace(component, "-", " ");
            String capitalizedName = WordUtils.capitalize(name).replace(" ", "");
            assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + capitalizedName;
        } else {
            assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + HTTP_CLIENT;
        }
        return assemblyComponentTypeName;
    }

    private void mapMessageTraces(ExecutionTrace executionTrace) {
        Execution rootExecution =
                new Execution(OperationRepository.ROOT_OPERATION, AllocationRepository.ROOT_ALLOCATION_COMPONENT, "-1", "-1", "-1", "-1", -1, -1, -1, -1, false);

        executionTrace.toMessageTrace(rootExecution);
    }

}
