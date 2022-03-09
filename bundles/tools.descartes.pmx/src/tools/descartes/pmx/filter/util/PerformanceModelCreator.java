/**
 * ==============================================
 *  PMX : Performance Model eXtractor
 * ==============================================
 *
 * (c) Copyright 2014-2015, by Juergen Walter and Contributors.
 *
 * Project Info:   http://descartes.tools/pmx
 *
 * All rights reserved. This software is made available under the terms of the
 * Eclipse Public License (EPL) v1.0 as published by the Eclipse Foundation
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse Public License (EPL)
 * for more details.
 *
 * You should have received a copy of the Eclipse Public License (EPL)
 * along with this software; if not visit http://www.eclipse.org or write to
 * Eclipse Foundation, Inc., 308 SW First Avenue, Suite 110, Portland, 97204 USA
 * Email: license (at) eclipse.org
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */
package tools.descartes.pmx.filter.util;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import kieker.common.util.signature.Signature;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import kieker.tools.traceAnalysis.systemModel.ExecutionContainer;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import tools.descartes.pmx.builder.IModelBuilder;

public class PerformanceModelCreator {
	private static final Logger log = Logger.getLogger(PerformanceModelCreator.class);

	// public static void createPerformanceModel(SystemModelRepository
	// systemModel, IModelBuilder builder) {
	// createExecutionContainers(systemModel, builder);
	// createAssemblies(systemModel, builder);
	// createComponentsAndInterfaces(systemModel, builder);
	// addComponentsToAssemblies(systemModel, builder);
	// createAllocations(systemModel, builder);
	// }

	public static void addComponentsToAssemblies(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<AssemblyComponent> assemblyComponents = systemModel.getAssemblyFactory()
				.getAssemblyComponentInstances();
		for (AssemblyComponent assembly : assemblyComponents) {
			String assemblyName = assembly.getType().getTypeName();
			if (assemblyName != "'Entry'") {
				builder.addComponentToAssembly(assemblyName, assemblyName);
			}
		}
	}

	public static void createAllocations(SystemModelRepository systemModel, IModelBuilder builder) {
		Collection<AllocationComponent> allocationComponents = systemModel.getAllocationFactory()
				.getAllocationComponentInstances();
		for (AllocationComponent allocationComponent : allocationComponents) {
			builder.addAllocationContext(allocationComponent.getAssemblyComponent().getType().getTypeName(),
					allocationComponent.getExecutionContainer().getName());
		}
	}

	public static void createAssemblies(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<AssemblyComponent> assemblyComponents = systemModel.getAssemblyFactory()
				.getAssemblyComponentInstances();
		for (AssemblyComponent assembly : assemblyComponents) {
			String assemblyName = assembly.getType().getTypeName();
			builder.addAssembly(assemblyName);
		}
	}

	public static void createComponentsAndInterfaces(SystemModelRepository systemModel, IModelBuilder builder) {
		final Collection<ComponentType> componentTypes = systemModel.getTypeRepositoryFactory().getComponentTypes();
		for (final ComponentType type : componentTypes) {
			builder.addComponent(type.getTypeName());
			builder.addInterface("I" + type.getTypeName());
			for (Operation opertation : type.getOperations()) {
				Signature signature = opertation.getSignature();
				builder.addMethod(type, signature);
			}
		}
	}

	public static void createExecutionContainers(SystemModelRepository systemModel, IModelBuilder builder,
			HashMap<String, Integer> numCores) {
		final Collection<ExecutionContainer> executionContainers = systemModel.getExecutionEnvironmentFactory()
				.getExecutionContainers();
		for (ExecutionContainer container : executionContainers) {
			int numberOfCores = 0;
			try {
				numberOfCores = numCores.get(container.getName());
			} catch (Exception e) {
				log.error("could not find number of cores for " + container.getName());
				log.info("assumed numberOfCores = 2");
				numberOfCores = 2;
			}
			builder.addHost(container.getName(), numberOfCores);
		}
	}

}
