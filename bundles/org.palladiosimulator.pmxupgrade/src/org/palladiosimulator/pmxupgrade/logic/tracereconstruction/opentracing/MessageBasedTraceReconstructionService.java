package org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.DataInterface;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationBasicComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationDataChannel;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AssemblyBasicComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AssemblyDataChannel;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.OperationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExecutionTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessagingExecution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;

/**
 * This class is a modified version of {@link TraceReconstructionService} that
 * supports message-based systems
 * 
 * @author Fatma Chebbi
 *
 */
public class MessageBasedTraceReconstructionService implements TraceReconstructionInterface {

	private final String[] emptyArray = new String[0];
	private AtomicInteger numberOfValidExecutions = new AtomicInteger(-1);

	private SystemModelRepository systemModelRepository;
	private List<ExecutionTrace> executionTraces = new ArrayList<>();
	private List<Execution> invalidExecutions = new ArrayList<>();
	private Map<String,String> assemblyNames = new  HashMap<>();
	private HashMap<String, Set<String>> componentsBySpan = new HashMap<>();

	private final String GENERIC_ASSEMBLY_COMPONENT_TYPE = "org.palladiosimulatpr.pmxupgrade.generic.";
	private final String UNKNOWN_ASSEMBLY_COMPONENT_TYPE = "unknown";
	private final String DATABASE_CALL = "databaseCall";
	private final String NETWORK_CALL = "networkCall";
	private final String SEND = "send";
	private final String RECEIVE = "receive";
	private final String HTTP_CLIENT = "HttpClient";
	private final String DATABASE_DRIVER = "DatabaseDriver";
	private final String MESSAGING_COMPONENT = "Topic";
	private final String SRV_INDICATION = "-SRV";

	@Override
	public ProcessingObjectWrapper reconstructTrace(Configuration configuration, TraceRecord traceRecord)
			throws InvalidTraceException {
		systemModelRepository = new SystemModelRepository();

		for (Trace t : traceRecord.getData()) {
			executionTraces.add(mapExecutionTraces(t));
		}
		executionTraces.forEach(this::mapMessageTraces);

		return new ProcessingObjectWrapper(systemModelRepository, executionTraces, invalidExecutions);
	}

	/**
	 * Transforms the trace into an internal data format and extracts information
	 * about the system architecture.
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
		// map.Entry = key-value pair, here we get the set of all entries of the map
		// (for understanding)
		for (Map.Entry<String, Process> stringProcessEntry : trace.getProcesses().entrySet()) {
			Entry<String, Process> pair = stringProcessEntry;
			Process p = (Process) pair.getValue();
			String executionContainerName = p.getServiceName().toUpperCase() + SRV_INDICATION;
			// TODO: why is this line here? (asked for understanding)
			systemModelRepository.getExecutionEnvironmentFactory()
					.lookupExecutionContainerByNamedIdentifier(executionContainerName);
			executionContainer.put((String) pair.getKey(), executionContainerName);
			assemblyNames.put((String) pair.getKey(), p.getServiceName());
		}
		
		// set topic for sending spans and other children ~fat
				for (Span span : trace.getSpans()) {
					Set <Span> children = trace.getSpanChildren(span.getSpanID());
					
					if ((span.getOperationName().toUpperCase().startsWith("SEND")|| 
							span.getKind().equalsIgnoreCase("producer"))&& !children.isEmpty()) {
						Iterator<Span> it = children.iterator();
						Span child = it.next();
						while(it.hasNext() && child.getTopic() == null) {
							child = it.next();
						}
						if(child!= null) {
							span.setTopic(child.getTopic());
						}
						
						//look for children without topic
						for (Span childSpan: trace.getSpans()) {
							if (childSpan.getTopic() == null && childSpan.getParent() != null && child.getParent().equalsIgnoreCase(span.getSpanID()) && trace.getSpanChildren(childSpan.getSpanID()) != null) {
								childSpan.setTopic(span.getTopic());
							}
						}
						
						
					}
					
				}
		// set Message types ~fat
		for (Span span: trace.getSpans()) {
			
			if(span.getOperationName().toUpperCase().startsWith("SEND") ||
					span.getKind().equalsIgnoreCase("producer")) {
				Set<Span> children = trace.getSpanChildren(span.getSpanID());
				Set<String> components = new HashSet<>();
				for (Span child: children) {
					components.add(trace.getProcesses().get(child.getProcessID()).getServiceName());
				}
				components.add(span.getTopic());
				DataInterface dInterface = systemModelRepository.getDataInterfacesRepository().lookupDataInterfaceByComponentsSet(components);
				if ( dInterface == null && span.getTopic() != null) {
					// allow empty datainterfaces && ! empty 
					dInterface = systemModelRepository.getDataInterfacesRepository().createAndRegisterDataInterface(components);
					
				}
				if(span.getTopic()!= null) {
					this.componentsBySpan.put(span.getSpanID(), components);
				}
				for (Span child: children) {
					if(dInterface != null)
					this.componentsBySpan.put(child.getSpanID(), components);

				}
				
				//allow datainterface with no children ~fat
				
			}
		}
		
		
		
		//by messaging traces several executions can result from a single execution ~fat
		for (Span t : trace.getSpans()) {
			if (t.getProcessID().equalsIgnoreCase("p1") && t.getOperationName().toLowerCase().contains("send")) {
			}
			Execution[] execution = mapExecution(t, executionContainer, componentsBySpan.get(t.getSpanID()), trace.hasChildren(t.getSpanID()));
			if (execution != null) {
				if (execution[0] != null) {
					executionTrace.add(execution[0]);
				}
				if (execution[1] != null) {
					executionTrace.add(execution[1]);
				}
				if (execution[2] != null) {
					executionTrace.add(execution[2]);
				}
			}

		}
		executionTrace.getInvalidExecutions().addAll(invalidExecutions);
		return executionTrace;
	}

	/**
	 * Custom processing of the data to transfer the tracing data into a uniform
	 * data model. Therefore, technical component names are abstracted. After that,
	 * the system architecture is extracted.
	 *
	 * @param span,               the corresponding @{@link Span} to be processed.
	 * @param executionContainer, the {@link ExecutionContainer} of the transferred
	 *                            span.
	 * @return the created {@link Execution} if the an obtained @{@link Span}.
	 */
	private synchronized Execution[] mapExecution(Span span, HashMap<String, String> executionContainer, Set<String> components, boolean hasChildren) {
		Execution first = null;
		Execution second = null;
		Execution third = null;
		String executionContainerId = span.getProcessID();
		String channelExecutionContainerId = span.getTopic();
		

		DataInterface dataInterface = null;
			if (components != null) {	
				dataInterface = systemModelRepository.getDataInterfacesRepository().lookupDataInterfaceByComponentsSet(components); 
			}
		
		final String executionContainerName = executionContainer.get(executionContainerId);
		String assemblyComponentTypeName = span.getComponentType();
		String assemblyChannelName = null;
		String channelExecutionContainerName = channelExecutionContainerId != null
				? executionContainer.get(channelExecutionContainerId)
				: null;
		String allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
		String operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();
		String allocationChannelName = null;
		String channelOperationFactoryName = assemblyChannelName + "." + span.getOperationName();
		boolean isMessaging = false;
		
		if (StringUtils.isEmpty(assemblyComponentTypeName)) {

			long tout = span.getStartTime() + span.getDuration();

			if (StringUtils.equalsIgnoreCase(span.getOperationName(), "GET")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "POST")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "PUT")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "DELETE")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "HEAD")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "OPTIONS")) {
				// resolve http requests
				assemblyComponentTypeName = resolveOperation(span, HTTP_CLIENT, assemblyNames.get(executionContainerId));

				allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
				span.setOperationName(NETWORK_CALL);
				span.setOperationParameters(emptyArray);
				operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

			} else if (StringUtils.equalsIgnoreCase(span.getOperationName(), "QUERY")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "UPDATE")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "DELETE")
					| StringUtils.equalsIgnoreCase(span.getOperationName(), "SELECT")) {
				// resolve database queries
				assemblyComponentTypeName = resolveOperation(span, DATABASE_DRIVER, assemblyNames.get(executionContainerId));

				allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
				span.setOperationName(DATABASE_CALL);
				span.setOperationParameters(emptyArray);
				operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

				//in the following situations we wil not consider the websockets of springboot. ~fat
			} else if ((span.getOperationName().toUpperCase().startsWith("SEND") || span.getKind().equalsIgnoreCase("producer"))&& hasChildren && span.getTopic() != null ) {
				// data channels also have an SRV as execution container
				// added part ~fat
				assemblyComponentTypeName = resolveOperation(span, MESSAGING_COMPONENT, assemblyNames.get(executionContainerId));
				allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
				span.setOperationName(SEND);
				span.setOperationParameters(emptyArray);
				channelOperationFactoryName = span.getTopic() + "." + span.getOperationName();
				assemblyChannelName = span.getTopic();
				channelExecutionContainerName = span.getTopic().toUpperCase() + SRV_INDICATION;
				allocationChannelName = channelExecutionContainerName + "::" + assemblyChannelName;
				isMessaging = true;
				operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

			} else if ((span.getOperationName().toUpperCase().startsWith("RECEIVE") || span.getKind().equalsIgnoreCase("consumer")) && componentsBySpan.get(span.getSpanID()) != null) {
				// added part ~fat
				assemblyComponentTypeName = resolveOperation(span, MESSAGING_COMPONENT, assemblyNames.get(executionContainerId));
				allocationComponentName = executionContainerName + "::" + assemblyComponentTypeName;
				span.setOperationName(RECEIVE);
				span.setOperationParameters(emptyArray);
				isMessaging = true;
				operationFactoryName = assemblyComponentTypeName + "." + span.getOperationName();

			} else {
				// invalid
				Operation op = new Operation(-1, null,
						new Signature(span.getOperationName(), new String[0], null, new String[0]));
				invalidExecutions.add(new Execution(op, new AllocationComponent(-1, null, null), span.getTraceID(),
						span.getSpanID(), Execution.NO_SESSION_ID, span.getFollowsFrom(), -1, -1, span.getStartTime(),
						tout, false));
				return null;
			}
		}

		AllocationBasicComponent allocInst = systemModelRepository.getAllocationFactory()
				.lookupAllocationComponentInstanceByNamedIdentifier(allocationComponentName);

		Execution[] result = new Execution[3];
		// add an execution for the message broker and change ids so that no consistency
		// problems occur.
		long firstId = UUID.randomUUID().getLeastSignificantBits();
		long secondId = UUID.randomUUID().getLeastSignificantBits();
		if ( span.getKind().equalsIgnoreCase("producer")) {
			String spanId = span.getSpanID();
			String spanParent = span.getFollowsFrom();
			AllocationDataChannel allocCh = systemModelRepository.getAllocationFactory()
					.lookupAllocationChannelInstanceByNamedIdentifier(allocationChannelName);
			span.setSpanID(String.valueOf(firstId));
			first = resolveSystemArchitecture(span, executionContainerName, assemblyComponentTypeName,
					allocationComponentName, operationFactoryName, allocInst, false, isMessaging, dataInterface);
			numberOfValidExecutions.getAndIncrement();

			// for receiving
			span.setSpanID(String.valueOf(secondId));
			span.setFollowsFrom(String.valueOf(firstId));
			span.setOperationName(RECEIVE);
			channelOperationFactoryName = span.getTopic() + "." + span.getOperationName();
			second = resolveSystemArchitecture(span, channelExecutionContainerName, assemblyChannelName,
					allocationChannelName, channelOperationFactoryName, allocCh, true, true, dataInterface);
			numberOfValidExecutions.getAndIncrement();

			// for sending
			span.setSpanID(spanId);
			span.setFollowsFrom(String.valueOf(secondId));
			span.setOperationName(SEND);
			channelOperationFactoryName = span.getTopic() + "." + span.getOperationName();

			third = resolveSystemArchitecture(span, channelExecutionContainerName, assemblyChannelName,
					allocationChannelName, channelOperationFactoryName, allocCh, true, true, dataInterface);
			numberOfValidExecutions.getAndIncrement();

			result[0] = first;
			result[1] = third;
			result[2] = second;
		} else {
			first = resolveSystemArchitecture(span, executionContainerName, assemblyComponentTypeName,
					allocationComponentName, operationFactoryName, allocInst, false, isMessaging, dataInterface);
			numberOfValidExecutions.getAndIncrement();

			result[0] = first;
		}
		return result;
	}

	/**
	 * Extracts the system architecture out of the data. This process is executed
	 * incrementally for each span in order to not miss an details and achieve a
	 * precise architecture.
	 *
	 * @param span,                      the @{@link Span} to be processed.
	 * @param executionContainerName,    name of the {@link ExecutionContainer}
	 * @param assemblyComponentTypeName, name of the {@link AssemblyComponent}
	 * @param allocationComponentName,   name of the {@link AllocationComponent}
	 * @param operationFactoryName,      name of the {@link Operation}.
	 * @param allocInst,                 the corresponding allocation instance.
	 * @return the created {@link Execution} if the an obtained @{@link Span}.
	 */
	private Execution resolveSystemArchitecture(Span span, String executionContainerName,
			String assemblyComponentTypeName, String allocationComponentName, String operationFactoryName,
			AllocationComponent allocInst, boolean channel, boolean isMessaging, DataInterface dataInterface) {
		// Allocation component instance doesn't exist
		if (allocInst == null) {
			AssemblyComponent assemblyComponent = systemModelRepository.getAssemblyFactory()
					.lookupAssemblyComponentInstanceByNamedIdentifier(assemblyComponentTypeName);

			if (assemblyComponent == null) {
				String type = channel ? "DATACHANNEL" : "BASICCOMPONENT";
				ComponentType componentType = systemModelRepository.getTypeRepositoryFactory()
						.lookupComponentTypeByNamedIdentifier(assemblyComponentTypeName, type);

				if (componentType == null) {
					componentType = systemModelRepository.getTypeRepositoryFactory()
							.createAndRegisterComponentType(assemblyComponentTypeName, assemblyComponentTypeName, type);
				}
				if (!channel) {
					assemblyComponent = systemModelRepository.getAssemblyFactory()
							.createAndRegisterAssemblyComponentInstance(assemblyComponentTypeName, componentType);
				} else {
					if (systemModelRepository.getAssemblyFactory()
							.lookupAssemblyComponentInstanceByNamedIdentifier(assemblyComponentTypeName) != null) {
						assemblyComponent = systemModelRepository.getAssemblyFactory()
								.lookupAssemblyComponentInstanceByNamedIdentifier(assemblyComponentTypeName);
					} else {
						assemblyComponent = systemModelRepository.getAssemblyFactory()
								.createAndRegisterAssemblyChannelInstance(assemblyComponentTypeName, componentType);
					}

				}

			}

			ExecutionContainer execContainer = systemModelRepository.getExecutionEnvironmentFactory()
					.lookupExecutionContainerByNamedIdentifier(executionContainerName);
			if (execContainer == null) {
				execContainer = systemModelRepository.getExecutionEnvironmentFactory()
						.createAndRegisterExecutionContainer(executionContainerName);
			}

			if (systemModelRepository.getAllocationFactory()
					.lookupAllocationChannelInstanceByNamedIdentifier(allocationComponentName) != null) {
				allocInst = systemModelRepository.getAllocationFactory()
						.lookupAllocationChannelInstanceByNamedIdentifier(allocationComponentName);
			} else {
				allocInst = systemModelRepository.getAllocationFactory().createAndRegisterAllocationComponentInstance(
						allocationComponentName, assemblyComponent, execContainer);
			}
		}

		Signature operationSignature = new Signature(span.getOperationName(), emptyArray, span.getReturnType(),
				span.getParameters());
		Operation op;

		long tout = span.getStartTime() + span.getDuration();
		if (isMessaging) {
			op = systemModelRepository.getOperationFactory().lookupMessagingOperationByNamedIdentifierAndDataInterface(operationFactoryName, dataInterface);
			// Operation doesn't exist
			// or add to Messaging operation ~fat
			if (op == null) {
				op = systemModelRepository.getOperationFactory().createAndRegisterMessagingOperation(
						operationFactoryName, allocInst.getAssemblyComponent().getType(), operationSignature, dataInterface);
				allocInst.getAssemblyComponent().getType().addOperation(op);
			}
			return new MessagingExecution(op, allocInst, span.getTraceID(), span.getSpanID(), Execution.NO_SESSION_ID,
					span.getFollowsFrom(), numberOfValidExecutions.get(), numberOfValidExecutions.get(),
					span.getStartTime(), tout, false, span.getTopic(), dataInterface);
		}
		
		op = systemModelRepository.getOperationFactory().lookupOperationByNamedIdentifier(operationFactoryName);

		// Operation doesn't exist
		// or add to Messaging operation ~fat
		if (op == null) {
			op = systemModelRepository.getOperationFactory().createAndRegisterOperation(operationFactoryName,
					allocInst.getAssemblyComponent().getType(), operationSignature);
			allocInst.getAssemblyComponent().getType().addOperation(op);
		}
		return new Execution(op, allocInst, span.getTraceID(), span.getSpanID(), Execution.NO_SESSION_ID,
				span.getFollowsFrom(), numberOfValidExecutions.get(), numberOfValidExecutions.get(),
				span.getStartTime(), tout, false);
	}

	private String resolveOperation(Span span, String componentType, String containerName) {
		String assemblyComponentTypeName;
		//containerName instead of span.getComponent in order to avoid technical naming and have several instances in repository view
		String component = containerName;
		if (StringUtils.isNotEmpty(containerName) && !component.startsWith(UNKNOWN_ASSEMBLY_COMPONENT_TYPE)) {
			String name = StringUtils.replace(component, "-", " ");
			String capitalizedName = WordUtils.capitalize(name).replace(" ", "");
			assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + capitalizedName;
		} else {
			assemblyComponentTypeName = GENERIC_ASSEMBLY_COMPONENT_TYPE + componentType;
		}
		return assemblyComponentTypeName;
	}

	private void mapMessageTraces(ExecutionTrace executionTrace) {
		Execution rootExecution = new Execution(OperationRepository.ROOT_OPERATION,
				AllocationRepository.ROOT_ALLOCATION_COMPONENT, "-1", "-1", "-1", "-1", -1, -1, -1, -1, false);

		executionTrace.toMessageTrace(rootExecution);
	}

}
