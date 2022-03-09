package org.palladiosimulator.pmxupgrade.pcm.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.indirections.composition.AssemblyDataConnector;
import org.palladiosimulator.indirections.repository.DataChannel;
import org.palladiosimulator.indirections.repository.DataInterface;
import org.palladiosimulator.indirections.repository.DataSignature;
import org.palladiosimulator.indirections.repository.DataSinkRole;
import org.palladiosimulator.indirections.repository.DataSourceRole;
import org.palladiosimulator.indirections.repository.JavaClassDataChannel;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.EventGroup;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.repository.SinkRole;
import org.palladiosimulator.pcm.repository.SourceRole;
import org.palladiosimulator.pcm.resourceenvironment.CommunicationLinkResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.resourcetype.CommunicationLinkResourceType;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.resourcetype.ResourceType;
import org.palladiosimulator.pcm.resourcetype.ResourcetypeFactory;
import org.palladiosimulator.pcm.resourcetype.SchedulingPolicy;
import org.palladiosimulator.pcm.system.SystemFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.IModelBuilder;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.util.ModelCreationUtils;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.BasicComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.DataChannelType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExternalCall;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;
import org.palladiosimulator.pmxupgrade.pcm.builder.measuringfiles.exporter.MeasuringFileExporterService;
import org.palladiosimulator.pmxupgrade.pcm.builder.persistance.PCMEMF;

public class PCMBuilder extends ModelBuilder implements IModelBuilder {
	private static final Logger log = Logger.getLogger(PCMBuilder.class);

	private final Repository repository;
	private static final org.palladiosimulator.indirections.repository.RepositoryFactory messagingRepositoryFactory = org.palladiosimulator.indirections.repository.RepositoryFactory.eINSTANCE;
	private static final org.palladiosimulator.indirections.composition.CompositionFactory compositionDataFactory = org.palladiosimulator.indirections.composition.CompositionFactory.eINSTANCE;
	private final org.palladiosimulator.pcm.system.System system;
	private final ResourceEnvironment resourceenvironment;
	private final Allocation allocation;
	private final UsageModel usage;

	private final CommunicationLinkResourceType LAN;
	private final ProcessingResourceType CPU;
	private final SchedulingPolicy PROCESSOR_SHARING;

	public PCMBuilder(String outputDir) {
		super(new File(outputDir).isDirectory() ? (outputDir) : (outputDir + File.separator) + "pcm" + File.separator);

		var resourceRepository = ResourcetypeFactory.eINSTANCE.createResourceRepository();
		CPU = ResourcetypeFactory.eINSTANCE.createProcessingResourceType();
		CPU.setEntityName("CPU");
		LAN = ResourcetypeFactory.eINSTANCE.createCommunicationLinkResourceType();
		LAN.setEntityName("LAN");
		PROCESSOR_SHARING = ResourcetypeFactory.eINSTANCE.createSchedulingPolicy();
		PROCESSOR_SHARING.setEntityName("SCHEDULING_POLICY");

		resourceRepository.getAvailableResourceTypes_ResourceRepository().add(CPU);
		resourceRepository.getAvailableResourceTypes_ResourceRepository().add(LAN);
		resourceRepository.getSchedulingPolicies__ResourceRepository().add(PROCESSOR_SHARING);

		repository = RepositoryFactory.eINSTANCE.createRepository();
		dataTypeMap = new HashMap<>();
		repository.setEntityName("repository");
		repository.setRepositoryDescription("extracted using PMX - www.descartes.tools/pmx");

		allocation = AllocationFactory.eINSTANCE.createAllocation();
		allocation.setEntityName("extractedAllocation");
		usage = UsagemodelFactory.eINSTANCE.createUsageModel();

		resourceenvironment = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
		resourceenvironment.setEntityName("extractedResourceenvironment");
		allocation.setTargetResourceEnvironment_Allocation(resourceenvironment);
		system = SystemFactory.eINSTANCE.createSystem();
		system.setEntityName("extractedSystem");
		allocation.setSystem_Allocation(system);

		PCMEMF.init();
		PCMEMF.add(resourceenvironment, new File(this.outputDir + "extracted.resourceenvironment").toURI().toString());
		PCMEMF.add(system, new File(this.outputDir + "extracted.system").toURI().toString());
		PCMEMF.add(repository, new File(this.outputDir + "extracted.repository").toURI().toString());
		PCMEMF.add(usage, new File(this.outputDir + "extracted.usagemodel").toURI().toString());
		PCMEMF.add(allocation, new File(this.outputDir + "extracted.allocation").toURI().toString());
		PCMEMF.add(resourceRepository, new File(this.outputDir + "extracted.resourcerepository").toURI().toString());
	}

	@Override
	public EObject connectAssemblies(String providingAssemblyName, String requiringAssemblyName) {
		AssemblyConnector assemblyConnector = CompositionFactory.eINSTANCE.createAssemblyConnector();
		system.getConnectors__ComposedStructure().add(assemblyConnector);
		assemblyConnector
				.setEntityName("Connector " + "Assembly_" + requiringAssemblyName + " <" + requiringAssemblyName + ">"
						+ " -> " + "Assembly_" + providingAssemblyName + " <" + providingAssemblyName + ">");
		AssemblyContext providingAssemblyContext = (AssemblyContext) getAssembly(providingAssemblyName);
		assemblyConnector.setProvidingAssemblyContext_AssemblyConnector(providingAssemblyContext);

		String providingComponentName = providingAssemblyName.split(ModelBuilder.seperatorChar)[0];
		String requiringComponentName = requiringAssemblyName.split(ModelBuilder.seperatorChar)[0];
		ProvidedRole providedRole = (ProvidedRole) getRole(
				"Provided_" + "I" + providingComponentName + ModelBuilder.seperatorChar + providingComponentName);
		assemblyConnector.setProvidedRole_AssemblyConnector((OperationProvidedRole) providedRole);

		AssemblyContext requiringAssemblyContext = (AssemblyContext) getAssembly(requiringAssemblyName);
		assemblyConnector.setRequiringAssemblyContext_AssemblyConnector(requiringAssemblyContext);
		RequiredRole requiredRole = (RequiredRole) getRole(
				"Required_" + "I" + providingComponentName + ModelBuilder.seperatorChar + requiringComponentName);
		assemblyConnector.setRequiredRole_AssemblyConnector((OperationRequiredRole) requiredRole);

		assemblyConnector.setParentStructure__Connector(system);
		return assemblyConnector;
	}

	@Override
	public EObject connectAssembliesMessaging(String sinkAssemblyName, String sourceAssemblyName,
			String dataInterface) {
		AssemblyDataConnector assemblyDataConnector = compositionDataFactory.createAssemblyDataConnector();
		system.getConnectors__ComposedStructure().add(assemblyDataConnector);
		assemblyDataConnector.setEntityName("Connector " + "Assembly_" + sourceAssemblyName + " <" + sourceAssemblyName
				+ ">" + " -> " + "Assembly_" + sinkAssemblyName + " <" + sinkAssemblyName + ">");
		AssemblyContext sinkAssemblyContext = (AssemblyContext) getAssembly(sinkAssemblyName);
		assemblyDataConnector.setSinkAssemblyContext(sinkAssemblyContext);

		String sinkComponentName = sinkAssemblyName.split(ModelBuilder.seperatorChar)[0];
		String sourceComponentName = sourceAssemblyName.split(ModelBuilder.seperatorChar)[0];
		ProvidedRole sinkRole = (ProvidedRole) getRole(
				"DataSink_" + dataInterface + ModelBuilder.seperatorChar + sinkComponentName + 0);
		assemblyDataConnector.setDataSinkRole((DataSinkRole) sinkRole);

		AssemblyContext sourceAssemblyContext = (AssemblyContext) getAssembly(sourceAssemblyName);
		assemblyDataConnector.setSourceAssemblyContext(sourceAssemblyContext);
		RequiredRole sourceRole = (RequiredRole) getRoleFromCopy(
				"DataSource_" + dataInterface + ModelBuilder.seperatorChar + sourceComponentName);
		assemblyDataConnector.setDataSourceRole((DataSourceRole) sourceRole);

		assemblyDataConnector.setParentStructure__Connector(system);
		if(sinkRole == null || sourceRole == null) {
			return null;
		}
		return assemblyDataConnector;
	}

	public void addUsageScenario(String assemblyName, String interfaceName, String methodName) {
		OperationInterface operationInterface = (OperationInterface) getInterface(interfaceName);

		OperationProvidedRole systemProvidedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		systemProvidedRole.setEntityName("Provided_" + interfaceName);
		systemProvidedRole.setProvidedInterface__OperationProvidedRole(operationInterface);
		system.getProvidedRoles_InterfaceProvidingEntity().add(systemProvidedRole);

		ProvidedDelegationConnector providedDelegationConnector = CompositionFactory.eINSTANCE
				.createProvidedDelegationConnector();
		providedDelegationConnector.setParentStructure__Connector(system);
		providedDelegationConnector.setEntityName(
				"ProvDelegation " + interfaceName + " -> " + interfaceName + ModelBuilder.seperatorChar + assemblyName);
		providedDelegationConnector
				.setAssemblyContext_ProvidedDelegationConnector((AssemblyContext) getAssembly(assemblyName));
		addProvidedRole(assemblyName, interfaceName);
		providedDelegationConnector.setInnerProvidedRole_ProvidedDelegationConnector((OperationProvidedRole) getRole(
				"Provided_" + interfaceName + ModelBuilder.seperatorChar + assemblyName));
		providedDelegationConnector.setOuterProvidedRole_ProvidedDelegationConnector(systemProvidedRole);

		PCMUsageModelFactory.addUsageScenario(usage,
				(OperationSignature) getMethod(ModelCreationUtils.createMethodKey(methodName, assemblyName)),
				systemProvidedRole);

	}

	List<OperationSignature> getCommonSignatures(OperationInterface op, OperationInterface op2) {
		List<OperationSignature> signatures = new ArrayList<>();
		for (int i = 0; i < op.getSignatures__OperationInterface().size(); i++) {
			OperationSignature sig = op.getSignatures__OperationInterface().get(i);
			Comparator<OperationSignature> comp = new Comparator<OperationSignature>() {
				public int compare(OperationSignature arg0, OperationSignature arg1) {
					if (arg0.getEntityName().equals(arg1.getEntityName())) {
						return 0;
					}
					return 1;
				}
			};
			for (int j = 0; j < op2.getSignatures__OperationInterface().size(); j++) {
				OperationSignature sig2 = op2.getSignatures__OperationInterface().get(j);
				if (comp.compare(sig, sig2) == 0) {
					signatures.add(sig);
				}
			}
		}
		return signatures;
	}

	private void createSuperInterface(OperationInterface op, OperationInterface op2) {
		List<OperationSignature> commonSignatures = getCommonSignatures(op, op2);

		log.info(op);
		log.info(op.getSignatures__OperationInterface().size());
		log.info(op2);
		log.info(op2.getSignatures__OperationInterface().size());

		if (!commonSignatures.isEmpty()) {
			OperationInterface parentInterface;
			parentInterface = RepositoryFactory.eINSTANCE.createOperationInterface(); // InfrastructureInterface();
			// //OperationInterface();
			parentInterface.setEntityName("IParrentOfAdditionValue");

			parentInterface.getSignatures__OperationInterface().addAll(commonSignatures);

			log.info(commonSignatures.size());
			log.info(op.getSignatures__OperationInterface().size());
			log.info(op2.getSignatures__OperationInterface().size());

			if (commonSignatures.size() == op.getSignatures__OperationInterface().size()
					&& commonSignatures.size() == op2.getSignatures__OperationInterface().size()) {
				log.info(op.getEntityName() + " and " + op2.getEntityName() + " are exactly the same");
			}
			repository.getInterfaces__Repository().add(parentInterface);
			op.getParentInterfaces__Interface().add(parentInterface);
			op2.getParentInterfaces__Interface().add(parentInterface);

		}
	}

	public EObject createAssembly(String assemblyName) {
		AssemblyContext assemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
		assemblyContext.setEntityName("Assembly_" + assemblyName + " <" + assemblyName + ">");
		system.getAssemblyContexts__ComposedStructure().add(assemblyContext);
		return assemblyContext;
	}

	public EObject createAllocation(String assemblyName, String hostName) {
		AllocationContext allocationContext = AllocationFactory.eINSTANCE.createAllocationContext();
		allocationContext.setEntityName("Allocation_Assembly_" + assemblyName + ModelBuilder.seperatorChar + hostName
				+ " <" + assemblyName + ">" + " <" + assemblyName + ">");
		ResourceContainer host = (ResourceContainer) getHost(hostName);
		allocationContext.setResourceContainer_AllocationContext(host);
		allocationContext.setAssemblyContext_AllocationContext(
				(AssemblyContext) getAssembly(assemblyName + ModelBuilder.seperatorChar + hostName));
		allocationContext.setAllocation_AllocationContext(allocation);

		return allocationContext;
	}

	public EObject createComponent(String componentName) {
		RepositoryComponent component = PCMComponentFactory.createComponent(componentName, repository);
		// log.info("created component " + component.getEntityName());
		if (!component.getEntityName().equals(componentName)) {
			log.error("componentName vs component.getEntityName() ==> " + componentName + " "
					+ component.getEntityName());
		}
		return component;
	}

	public void saveToFile(String path) {
		PCMEMF.saveAll();

		// PCMEMF.createEMFMeasuringFiles(path, usage, resourceenvironment);

		// try {
		// TODO
		// MeasuringFileEMFExporterService.createEMFMeasuringFiles(path, usage,
		// resourceenvironment);
		MeasuringFileExporterService.createMeasuringFiles(path, usage, resourceenvironment);
		// } catch (PMXException e) {
		// e.printStackTrace();
		// }

	}

	public void saveToFile() {
		PCMEMF.saveAll();
	}

	public EObject createRequiredRole(String requiredComponentName, String interfaceName) {
		BasicComponent requiringComponent = (BasicComponent) getComponent(requiredComponentName);
		OperationRequiredRole requiredRole = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
		requiredRole.setEntityName("Required_" + interfaceName + ModelBuilder.seperatorChar + requiredComponentName);
		requiredRole.setRequiredInterface__OperationRequiredRole((OperationInterface) getInterface(interfaceName));
		requiringComponent.getRequiredRoles_InterfaceRequiringEntity().add(requiredRole);
		return requiredRole;
	}

	public EObject createProvidedRole(String componentName, String interfaceName) {
		OperationProvidedRole providedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		providedRole.setEntityName("Provided_" + interfaceName + ModelBuilder.seperatorChar + componentName);
		OperationInterface operationInterface = (OperationInterface) getInterface(interfaceName);
		providedRole.setProvidedInterface__OperationProvidedRole(operationInterface);
		BasicComponent providingComponent = (BasicComponent) getComponent(componentName);
		providingComponent.getProvidedRoles_InterfaceProvidingEntity().add(providedRole);
		return providedRole;
	}

	public EObject createInterface(String interfaceName) {
		OperationInterface newInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
		newInterface.setEntityName(interfaceName);
		repository.getInterfaces__Repository().add(newInterface);
		return newInterface;
	}

	public EObject createMethod(ComponentType type, Signature signature, boolean messaging) {
		if (!messaging) {
			String interfaceName = "I" + applyNameFixes(type.getTypeName());
			String method = signature.getName();
			String provided = signature.getReturnType();
			String[] required = signature.getParamTypeList();

			OperationInterface newInterface = (OperationInterface) getInterface(interfaceName);
			OperationSignature operationSignature = RepositoryFactory.eINSTANCE.createOperationSignature();
			newInterface.getSignatures__OperationInterface().add(operationSignature);

			operationSignature.setEntityName(method);
			operationSignature.setReturnType__OperationSignature(addDataType(provided));
			int i = 1;
			for (String r : required) {
				Parameter parameter = RepositoryFactory.eINSTANCE.createParameter();
				parameter.setParameterName(r + i++);
				parameter.setDataType__Parameter(addDataType(r));
				operationSignature.getParameters__OperationSignature().add(parameter);
			}
			return operationSignature;
		} else {
			return null;
		}
	}

	private DataType addDataType(String name) {
		if (name.equals("void")) {
			return null;
		}
		if (name.equals("<NO-RETURN-TYPE>")) {
			return null;
		}
		name = name.replace("java.lang.String", "String");
		if (name.equals("boolean")) {
			name = PrimitiveTypeEnum.BOOL.getName();
		} else if (name.equals("int")) {
			name = PrimitiveTypeEnum.INT.getName();
		} else if (name.equals("String")) {
			name = PrimitiveTypeEnum.STRING.getName();
		}

		if (!dataTypeMap.containsKey(name)) {
			dataTypeMap.put(name, createDataType(name, repository));
		}
		return (DataType) dataTypeMap.get(name);
	}

	private DataType createDataType(String name, Repository repository) {
		DataType dataType = null;
		PrimitiveTypeEnum type = null;
		if (name != null) {
			if (name.endsWith("[]")) {
				dataType = RepositoryFactory.eINSTANCE.createCollectionDataType();
				name = name.replace("[]", "");
				repository.getDataTypes__Repository().add(dataType);
				DataType innerDataType = addDataType(name);
				((CollectionDataType) dataType).setInnerType_CollectionDataType(innerDataType);
				((CollectionDataType) dataType).setEntityName("ArrayOf" + name + "s");
			} else {
				dataType = RepositoryFactory.eINSTANCE.createCompositeDataType();
				((CompositeDataType) dataType).setEntityName(name);
			}
		}
		repository.getDataTypes__Repository().add(dataType);
		return dataType;
	}

	@Override
	public EObject createHost(String hostName, int numCores) {
		ResourceContainer resourceContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
		resourceContainer.setEntityName(hostName);

		resourceenvironment.getResourceContainer_ResourceEnvironment().add(resourceContainer);

		ProcessingResourceSpecification cpu = ResourceenvironmentFactory.eINSTANCE
				.createProcessingResourceSpecification();
		cpu.setActiveResourceType_ActiveResourceSpecification(CPU);

		cpu.setSchedulingPolicy(PROCESSOR_SHARING);
		PCMRandomVariable processingRate = CoreFactory.eINSTANCE.createPCMRandomVariable();

		processingRate.setSpecification("1");
		cpu.setNumberOfReplicas(numCores);

		cpu.setProcessingRate_ProcessingResourceSpecification(processingRate);
		resourceContainer.getActiveResourceSpecifications_ResourceContainer().add(cpu);

		return resourceContainer;
	}

	public void createNetwork(double averageNetworkDelay, double throughput) {
		LinkingResource network = ResourceenvironmentFactory.eINSTANCE.createLinkingResource();
		network.setEntityName("internal network");
		network.setResourceEnvironment_LinkingResource(resourceenvironment);
		for (ResourceContainer resource : resourceenvironment.getResourceContainer_ResourceEnvironment()) {
			network.getConnectedResourceContainers_LinkingResource().add(resource);
		}
		CommunicationLinkResourceSpecification communicationLinkResourceSpecification = ResourceenvironmentFactory.eINSTANCE
				.createCommunicationLinkResourceSpecification();
		CommunicationLinkResourceType communicationLinkResourceType = null;
		communicationLinkResourceSpecification
				.setCommunicationLinkResourceType_CommunicationLinkResourceSpecification(communicationLinkResourceType);
		network.setCommunicationLinkResourceSpecifications_LinkingResource(communicationLinkResourceSpecification);
		communicationLinkResourceSpecification.setFailureProbability(0.0);

		PCMRandomVariable delay = CoreFactory.eINSTANCE.createPCMRandomVariable();
		delay.setSpecification("" + averageNetworkDelay);
		communicationLinkResourceSpecification.setLatency_CommunicationLinkResourceSpecification(delay);

		PCMRandomVariable throughputrv = CoreFactory.eINSTANCE.createPCMRandomVariable();
		throughputrv.setSpecification("" + throughput);

		communicationLinkResourceSpecification.setThroughput_CommunicationLinkResourceSpecification(throughputrv);

		communicationLinkResourceSpecification
				.setCommunicationLinkResourceType_CommunicationLinkResourceSpecification(LAN);

	}

	public void addComponentToAssembly(String assemblyName, String componentName) {
		AssemblyContext assemblyContext = (AssemblyContext) getAssembly(assemblyName);
		RepositoryComponent component = getComponent(componentName) != null
				? (RepositoryComponent) getComponent(componentName)
				: (RepositoryComponent) getDataChannel(componentName);
		assemblyContext.setEncapsulatedComponent__AssemblyContext(component);
	}

	public EObject createSEFF(String componentName, String methodName, List<ExternalCall> externalCalls,
			String hostName, double meanResourceDemand) {
		BasicComponent component = (BasicComponent) this.getComponent(componentName);
		OperationSignature operationSignature = (OperationSignature) getMethod(
				ModelCreationUtils.createMethodKey(methodName, applyNameFixes(componentName)));
		ResourceContainer host = (ResourceContainer) getHost(hostName);
		return PCMSeffFactory2.createSEFF(this, component, operationSignature, externalCalls, host, meanResourceDemand);
	}

	public EObject createDataSEFF(String componentName, String dataInterfaceName, List<ExternalCall> externalCalls,
			String hostName, double meanResourceDemand) {
		BasicComponent component = (BasicComponent) this.getComponent(componentName);

		ResourceContainer host = (ResourceContainer) getHost(hostName);

		DataSignature dataSignature = ((DataInterface) getDataInterface(dataInterfaceName)).getDataSignature();
		return PCMSeffFactory2.createSEFF(this, component, dataSignature, externalCalls, host, meanResourceDemand);

	}

	public void addResourceDemand(String service) {
		// TODO Auto-generated method stub
	}

	HashMap<String, OperationProvidedRole> systemProvidedRoles = new HashMap<>();

	private static boolean isUsageSet = false;

	public void addUsageScenario(HashMap<String, List<Double>> workload) {
		if (isUsageSet) {
			return;
		}
		PCMUsageModelFactory2.createWorkload(workload, this, usage, system);
		isUsageSet = true;
	}

	@Override
	public EObject createDataChannel(String channelName) {
		RepositoryComponent channel = PCMComponentFactory.createChannel(channelName, repository);
		if (!channel.getEntityName().equals(channelName)) {
			log.error("channelName vs channel.getEntityName() ==> " + channelName + " " + channel.getEntityName());
		}
		return channel;

	}

	@Override
	public EObject createDataInterface(String dataInterfaceName) {

		DataInterface dataInterface = messagingRepositoryFactory.createDataInterface();
		dataInterface.setEntityName(dataInterfaceName);
		DataSignature dataSignature = messagingRepositoryFactory.createDataSignature();
		dataSignature.setEntityName(dataInterfaceName + "Signature");
		Parameter parameter = RepositoryFactory.eINSTANCE.createParameter();
		parameter.setParameterName("payload");
		parameter.setDataType__Parameter(addDataType(dataInterfaceName));
		dataSignature.setParameter(parameter);
		dataInterface.setDataSignature(dataSignature);
		repository.getInterfaces__Repository().add(dataInterface);
		return dataInterface;
	}

	@Override
	public EObject createSinkRole(String componentName, String dataInterfaceName, long number) {
		DataSinkRole sinkRole = messagingRepositoryFactory.createDataSinkRole();
		sinkRole.setEntityName("DataSink_" + dataInterfaceName + ModelBuilder.seperatorChar + componentName + number);
		DataInterface dataInterface = (DataInterface) getDataInterface(dataInterfaceName);
		sinkRole.setDataInterface(dataInterface);
		if (getComponent(componentName) != null) {
			BasicComponent providingComponent = (BasicComponent) getComponent(componentName);
			providingComponent.getProvidedRoles_InterfaceProvidingEntity().add(sinkRole);
		} else {
			DataChannel providingComponent = (DataChannel) getDataChannel(componentName);
			providingComponent.getDataSinkRoles().add(sinkRole);
		}
		return sinkRole;
	}

	@Override
	public EObject createSourceRole(String componentName, String dataInterfaceName, long number) {
		DataSourceRole sourceRole = messagingRepositoryFactory.createDataSourceRole();
		sourceRole
				.setEntityName("DataSource_" + dataInterfaceName + ModelBuilder.seperatorChar + componentName + number);
		DataInterface dataInterface = (DataInterface) getDataInterface(dataInterfaceName);
		sourceRole.setDataInterface(dataInterface);
		if ( getComponent(componentName) != null) {
			BasicComponent providingComponent = (BasicComponent) getComponent(componentName);
			providingComponent.getRequiredRoles_InterfaceRequiringEntity().add(sourceRole);
		
		return sourceRole;
		}
			DataChannel providingComponent = (DataChannel) getDataChannel(componentName);
			providingComponent.getDataSourceRoles().add(sourceRole);
		
		return sourceRole;
	}
	
	
	
}
