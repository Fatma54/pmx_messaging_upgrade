package org.palladiosimulator.pmxupgrade.logic.modelcreation.creator;

import org.palladiosimulator.indirections.repository.DataSignature;
import org.palladiosimulator.indirections.repository.impl.DataSignatureImpl;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.IModelBuilder;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.DataInterface;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ExecutionContainer;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationBasicComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationDataChannel;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AssemblyBasicComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.BasicComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.DataChannelType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessagingOperation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;

/**
 * Builds the performance Models by invocating the specific implementations of
 * the builder pattern architecture.
 *
 * @author Patrick Treyer
 */
public class PerformanceModelCreator {

	private static final Logger log = LogManager.getLogger(PerformanceModelCreator.class);

	public static void addComponentsToAssemblies(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<AssemblyBasicComponent> assemblyComponents = systemModel.getAssemblyFactory()
				.getAssemblyComponentInstances();
		for (AssemblyComponent assembly : assemblyComponents) {
			String assemblyName = assembly.getType().getTypeName();
			if (!StringUtils.equalsIgnoreCase(assemblyName, "'Entry'")) {
				builder.addComponentToAssembly(assemblyName, assemblyName);
			}
		}
	}

	public static void createAllocations(SystemModelRepository systemModel, IModelBuilder builder) {
		Collection<AllocationBasicComponent> allocationComponents = systemModel.getAllocationFactory()
				.getAllocationComponentInstances();
		for (AllocationBasicComponent allocationComponent : allocationComponents) {
			builder.addAllocationContext(allocationComponent.getAssemblyComponent().getType().getTypeName(),
					allocationComponent.getExecutionContainer().getName());
		}
		Collection<AllocationDataChannel> allocationChannels = systemModel.getAllocationFactory()
				.getAllocationChannelInstances();
		for (AllocationDataChannel allocationChannel : allocationChannels) {
			builder.addAllocationContext(allocationChannel.getAssemblyComponent().getType().getTypeName(),
					allocationChannel.getExecutionContainer().getName());
		}
	}

	public static void createAssemblies(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<AssemblyBasicComponent> assemblyComponents = systemModel.getAssemblyFactory()
				.getAssemblyComponentInstances();
		for (AssemblyComponent assembly : assemblyComponents) {
			String assemblyName = assembly.getType().getTypeName();
			builder.addAssembly(assemblyName);
		}
	}

	public static void createComponentsAndInterfaces(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<BasicComponentType> componentTypes = systemModel.getTypeRepositoryFactory()
				.getComponentTypes();
		/*
		 * final Collection<ComponentType> componentTypes =
		 * systemModel.getTypeRepositoryFactory().getComponentTypes()
		 * .stream().filter(it -> it instanceof
		 * BascicomponentType).collect(Collectors.toList());
		 * 
		 */
	
		for (final ComponentType type : componentTypes) {
			builder.addComponent(type.getTypeName());
			boolean onlyMessaging = true;
			for (Operation op: type.getOperations()) {
				if(! (op instanceof MessagingOperation)) {
					onlyMessaging = false;
				}
			}
			if (! onlyMessaging) {
				builder.addInterface("I" + type.getTypeName());
			}

			for (Operation operation : type.getOperations()) {

				Signature signature = operation.getSignature();
				if (operation instanceof MessagingOperation) {
					continue;
				} else {
					builder.addMethod(type, signature, false);

				}
				// and then maybe here add messaging Method and normal method so that the
				// messaging one can be added to te data interface not the operation interface
				// ~fat
			}
		}
		/*
		 * final Collection<ComponentType> dataComponentTypes =
		 * systemModel.getTypeRepositoryFactory().getComponentTypes()
		 * .stream().filter(it -> it instanceof
		 * DataChannelComponentType).collect(Collectors.toList());
		 * 
		 */
		/*
		 * for (...) { builder.addDatachannel(type) }
		 */
	}

	public static void createDataChannelsAndDataInterfaces(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<DataChannelType> channelTypes = systemModel.getTypeRepositoryFactory().getChannelTypes();
		final Collection<DataInterface> dataInterfaces = systemModel.getDataInterfacesRepository().getDataInterfaces();
		for (final DataChannelType type : channelTypes) {
			builder.addDataChannel(type.getTypeName());
		}
		for (final DataInterface dataInterface : dataInterfaces) {
			builder.addDataInterface(systemModel.getDataInterfacesRepository()
					.getDataInterfaceNameByComponents(dataInterface.getComponents()));			

		}
		

	}

	public static void createExecutionContainers(SystemModelRepository systemModel, IModelBuilder builder,
			HashMap<String, Integer> numCores) {
		final Collection<ExecutionContainer> executionContainers = systemModel.getExecutionEnvironmentFactory()
				.getExecutionContainers();
		for (ExecutionContainer container : executionContainers) {
			int numberOfCores;
			try {
				numberOfCores = numCores.get(container.getName());
			} catch (Exception e) {
				log.info("could not find number of cores for " + container.getName());
				log.info("assumed numberOfCores = 2");
				numberOfCores = 2;
			}
			builder.addHost(container.getName(), numberOfCores);
		}
	}

}
