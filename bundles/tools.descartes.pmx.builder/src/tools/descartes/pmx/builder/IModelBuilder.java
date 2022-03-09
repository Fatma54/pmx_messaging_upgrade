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
package tools.descartes.pmx.builder;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import kieker.common.util.signature.Signature;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import tools.descartes.pmx.util.ExternalCall;

public interface IModelBuilder {
	
	public EObject createAssembly(String asseblyName);
	public EObject createComponent(String componentName);
	public void addComponentToAssembly(String assemblyName, String componentName);
	public EObject createInterface(String InterfaceName);
	public EObject createMethod(ComponentType type, Signature signature);

	public EObject createHost(String hostName, int numCores);
	public EObject createAllocation(String assemblyName, String hostName);
	public EObject createProvidedRole(String componentName, String interfaceName);
	public EObject createRequiredRole(String componentName, String interfaceName);
	public EObject createSEFF(String componentName, String methodName, List<ExternalCall> externalCalls, String processingResource, double meanResourceDemand);
	
	public void addConnectionToAssemblies(String requiringAssemblyName, String providingAssemblyName);

	public EObject connectAssemblies(String providingAssemblyName, String requiringAssemblyName);

	public void addResourceDemand(String service);

	public EObject getRole(String role);
	public EObject getAssembly(String assemblyName);
	public EObject getMethod(String methodName);
	public EObject getInterface(String interfaceName);
	public EObject getSEFF(String componentName,String methodName);

	public void addProvidedRole(String componentName, String interfaceName);
	public void addRequiredRole(String componentName, String interfaceName);
	public void addComponent(String componentName);

	public void addHost(String name, int numCores);
	public EObject addAllocationContext(String componentName, String hostName);
	public EObject addAssembly(String name);
	public void addInterface(String typeName);
	public void addSEFF(String componentName, String methodName, List<ExternalCall> externalCalls, String processingResource, double meanResourceDemand);
	public void addSEFF(String componentName,String methodName, EObject seff);

	public boolean isSEFF(String componentName,String methodName);

	public void saveToFile();
	public void saveToFile(String path);
	public void addMethod(ComponentType type, Signature signature);
	public void createNetwork(double averageNetworkDelay, double throughtput);

	public String getOutputDirectory();

	// public void addUsageScenario(String assemblyName, String interfaceName,
	// String methodName);
	public void addUsageScenario(HashMap<String, List<Double>> workload);

}
