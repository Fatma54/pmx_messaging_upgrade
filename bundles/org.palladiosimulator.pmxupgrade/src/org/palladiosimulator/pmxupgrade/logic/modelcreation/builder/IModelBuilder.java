package org.palladiosimulator.pmxupgrade.logic.modelcreation.builder;

import org.palladiosimulator.indirections.repository.DataSignature;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExternalCall;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;
import org.eclipse.emf.ecore.EObject;

import java.util.HashMap;
import java.util.List;

/**
 * Interface for the Model builder, which can be implemented for specific
 * performance model representations.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public interface IModelBuilder {    
	
	void addDataConnectionToAssemblies(String sourceAssemblyName, String sinkAssemblyName, String dataInterface);
	
	EObject createAssembly(String assemblyName);

	EObject createComponent(String componentName);

	void addComponentToAssembly(String assemblyName, String componentName);

	EObject createInterface(String InterfaceName);

	EObject createMethod(ComponentType type, Signature signature, boolean messaging);

	EObject createHost(String hostName, int numCores);

	EObject createAllocation(String assemblyName, String hostName);

	EObject createProvidedRole(String componentName, String interfaceName);

	EObject createRequiredRole(String componentName, String interfaceName);

	EObject createSourceRole(String componentName, String dataInterfaceName, long number);

	EObject createSinkRole(String componentName, String dataInterfaceName, long number);


	EObject createSEFF(String componentName, String methodName, List<ExternalCall> externalCalls,
			String processingResource, double meanResourceDemand);
		
	EObject createDataSEFF(String componentName, String dataInterfaceName, List<ExternalCall> externalCalls,
			String processingResource, double meanResourceDemand);

	void addConnectionToAssemblies(String requiringAssemblyName, String providingAssemblyName);

	EObject connectAssemblies(String providingAssemblyName, String requiringAssemblyName);

	void addResourceDemand(String service);

	EObject getRole(String role);

	EObject getAssembly(String assemblyName);

	EObject getMethod(String methodName);

	EObject getInterface(String interfaceName);

	EObject getSEFF(String componentName, String methodName);

	void addProvidedRole(String componentName, String interfaceName);

	void addRequiredRole(String componentName, String interfaceName);
	
	boolean addSinkRole(String componentName, String dataInterfaceName);

	boolean addSourceRole(String componentName, String dataInterfaceName);

	void addComponent(String componentName);

	void addHost(String name, int numCores);

	EObject addAllocationContext(String componentName, String hostName);

	EObject addAssembly(String name);

	void addInterface(String typeName);

	void addSEFF(String componentName, String methodName, List<ExternalCall> externalCalls, String processingResource,
			double meanResourceDemand);
	
	void addDataSEFF(String componentName, String dataInterfaceName, List<ExternalCall> externalCalls, String processingResource,
			double meanResourceDemand);

	void addSEFF(String componentName, String methodName, EObject seff);

	boolean isSEFF(String componentName, String methodName);

	void saveToFile();

	void saveToFile(String path);

	void addMethod(ComponentType type, Signature signature, boolean Messaging);

	void addDataChannel(String typeName);

	void addDataInterface(String dataInterfaceName);

	void createNetwork(double averageNetworkDelay, double throughtput);

	String getOutputDirectory();

	void addUsageScenario(HashMap<String, List<Double>> workload);

	EObject createDataChannel(String typeName);

	EObject createDataInterface(String string);
	
	public EObject connectAssembliesMessaging(String sendingAssemblyName, String receivingAssemblyName, String dataInterface);

}
