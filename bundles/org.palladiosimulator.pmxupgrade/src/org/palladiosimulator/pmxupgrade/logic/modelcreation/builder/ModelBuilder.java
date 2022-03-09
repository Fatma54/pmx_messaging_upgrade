package org.palladiosimulator.pmxupgrade.logic.modelcreation.builder;

import org.palladiosimulator.indirections.repository.DataSignature;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.util.ModelCreationUtils;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.ComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.BasicComponentType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.DataChannelType;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExternalCall;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.Signature;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of the {@link IModelBuilder} for extracting language
 * independent concepts.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public abstract class ModelBuilder implements IModelBuilder {
	private static final Logger log = LogManager.getLogger(ModelBuilder.class);

	private final Map<String, EObject> componentMap;
	private final Map<String, EObject> dataChannelMap;
	private final Map<String, EObject> dataInterfaceMap;
	private final Map<String, EObject> interfaceMap;
	private final Map<String, EObject> methodMap;
	private final Map<String, EObject> hostMap;
	private final Map<String, EObject> allocationMap;
	private final Map<String, EObject> roleMap;
	private final Map<String, EObject> roleMapCopy;
	private final Map<String, EObject> assemblyMap;
	private final Map<String, EObject> connectorMap;
	private final Map<String, EObject> seffMap;
	public Map<String, EObject> dataTypeMap;
	

	public static String seperatorChar = "#";

	public String outputDir;

	public ModelBuilder(String outputDir) {
		componentMap = new HashMap<>();
		dataChannelMap = new HashMap<>();
		methodMap = new HashMap<>();
		hostMap = new HashMap<>();
		assemblyMap = new HashMap<>();
		allocationMap = new HashMap<>();
		interfaceMap = new HashMap<>();
		dataInterfaceMap = new HashMap<>();
		roleMap = new HashMap<>();
		seffMap = new HashMap<>();
		connectorMap = new HashMap<>();
		roleMapCopy = new HashMap<>();
		this.outputDir = outputDir;
		File dir = new File(this.outputDir);
		dir.mkdirs();
		log.info("Output directory: " + dir.getAbsolutePath());
	}

	public String getOutputDirectory() {
		return outputDir;
	}

	public EObject addAllocationContext(String componentName, String hostName) {
		EObject allocation;
		componentName = applyNameFixes(componentName);
		if (!allocationMap.containsKey(componentName + seperatorChar + hostName)) {
			allocation = createAllocation(componentName, hostName);
			allocationMap.put(componentName + seperatorChar + hostName, allocation);
		} else {
			allocation = allocationMap.get(componentName + seperatorChar + hostName);
		}
		return allocation;
	}

	public void addInterface(String interfaceName) {
		interfaceName = applyNameFixes(interfaceName);
		if (!interfaceMap.containsKey(interfaceName)) {
			EObject newInterface = createInterface(interfaceName);
			interfaceMap.put(interfaceName, newInterface);
		}
	}

	public void addDataInterface(String dataInterfaceName) {
		dataInterfaceName = applyNameFixes(dataInterfaceName);
		if (!dataInterfaceMap.containsKey(dataInterfaceName)) {
			EObject newDataInterface = createDataInterface(dataInterfaceName);
			dataInterfaceMap.put(dataInterfaceName, newDataInterface);
		}

	}

	public EObject addAssembly(String name) {
		EObject assembly;
		name = applyNameFixes(name);

		if (!assemblyMap.containsKey(name)) {
			assembly = createAssembly(name);
			assemblyMap.put(name, assembly);
		} else {
			assembly = assemblyMap.get(name);
		}
		return assembly;
	}

	public EObject getAssembly(String assemblyName) {
		assemblyName = applyNameFixes(assemblyName);
		EObject assembly = assemblyMap.get(assemblyName);
		if (assembly == null) {
			log.error("Could not find AssemblyContext: " + assemblyName);
			log.info("Avialable assemblies " + assemblyMap.keySet());
		}
		return assembly;
	}

	public void addConnectionToAssemblies(String requiringAssemblyName, String providingAssemblyName) {
		String key = "Connector " + "Assembly_" + requiringAssemblyName + " <" + requiringAssemblyName + ">" + " -> "
				+ "Assembly_" + providingAssemblyName + " <" + providingAssemblyName + ">";
		if (!connectorMap.containsKey(key)) {
			EObject connector = connectAssemblies(providingAssemblyName, requiringAssemblyName);
			connectorMap.put(key, connector);
		}
	}
	
	public void addDataConnectionToAssemblies(String sourceAssemblyName, String sinkAssemblyName, String dataInterface) {
		int i = 0;
		String key = "DataConnector " + "Assembly_" + sourceAssemblyName + " <" + sourceAssemblyName + ">" + " -> "
				+ "Assembly_" + sinkAssemblyName + " <" + sinkAssemblyName + ">";
		EObject connector = connectAssembliesMessaging(sinkAssemblyName, sourceAssemblyName, dataInterface);	
		while (connectorMap.containsKey(key+i) && connector != null) {
			i ++;
			connectorMap.put(key, connector);
			
		}
	}
	
	public EObject getHost(String hostName) {
		EObject host = hostMap.get(hostName);
		if (host == null) {
			log.info("host " + hostName + " not found");
			log.info("HostMap  " + hostMap.keySet());
		}
		return host;
	}

	public void addHost(String hostName, int numCores) {
		if (!hostMap.containsKey(hostName)) {
			EObject host = createHost(hostName, numCores);
			hostMap.put(hostName, host);
		}
	}

	public EObject getComponent(String componentName) {
		componentName = applyNameFixes(componentName);
		EObject component = componentMap.get(componentName);
		if (component == null) {
			log.error("requested component [" + componentName + "] has not been created");
			log.error("" + componentMap.keySet());
		}
		return component;
	}

	public static String applyNameFixes(String componentName) {
		if (componentName.contains("$")) {
			componentName = componentName.split("\\$")[0]; // JAVA $
		}
		return componentName;
	}

	public void addComponent(String componentName) {
		componentName = applyNameFixes(componentName);
		if (!hasComponentBeenAdded(componentName)) {
			componentMap.put(componentName, createComponent(componentName));
		}
	}

	public void addDataChannel(String dataChannelName) {
		dataChannelName = applyNameFixes(dataChannelName);
		if (!dataChannelMap.containsKey(dataChannelName)) {
			dataChannelMap.put(dataChannelName, createDataChannel(dataChannelName));
		}
	}

	public void addRequiredRole(String requiredComponentName, String interfaceName) {
		String requiredRoleName = "Required_" + interfaceName + ModelBuilder.seperatorChar + requiredComponentName;
		if (!roleMap.containsKey(requiredRoleName)) {
			roleMap.put(requiredRoleName, createRequiredRole(requiredComponentName, interfaceName));
		}
	}

	public void addProvidedRole(String providedComponentName, String interfaceName) {
		String providedRoleName = "Provided_" + interfaceName + ModelBuilder.seperatorChar + providedComponentName;
		if (!roleMap.containsKey(providedRoleName)) {
			roleMap.put(providedRoleName, createProvidedRole(providedComponentName, interfaceName));
		}
	}

	
	@Override
	public boolean addSinkRole(String sinkComponentName, String dataInterfaceName) {
		String sinkRoleName = "DataSink_" + dataInterfaceName + ModelBuilder.seperatorChar + sinkComponentName;
		if (!roleMap.containsKey(sinkRoleName + 0)) {
			roleMap.put(sinkRoleName + 0 , createSinkRole(sinkComponentName, dataInterfaceName, 0));
			return true;
	
		}
		return false;
	}

	@Override
	public boolean addSourceRole(String sourceComponentName, String dataInterfaceName) {
		String sourceRoleName = "DataSource_" + dataInterfaceName + ModelBuilder.seperatorChar + sourceComponentName;
		if ((getComponent(sourceComponentName)) != null) {
			if (!roleMap.containsKey(sourceRoleName + 0)) {
				roleMap.put(sourceRoleName + 0, createSourceRole(sourceComponentName, dataInterfaceName, 0));
				return true;
			}
		} else {
			long number = roleMap.entrySet().stream().filter(x -> x.getKey().contains(sourceRoleName)).count();
			if (!roleMap.containsKey(sourceRoleName + number)) {
				roleMap.put(sourceRoleName + number, createSourceRole(sourceComponentName, dataInterfaceName, number));
				return true;
			}
		}
		
		return false;
	}
	public Set<String> getRoles() {
		return roleMap.keySet();
	}

	public EObject getInterface(String interfaceName) {
		EObject operationInterface = interfaceMap.get(interfaceName);
		if (operationInterface == null) {
			log.error("Could not find interface " + interfaceName);
			log.info("Available interfaces" + interfaceMap.keySet());
		}

		return operationInterface;
	}

	public EObject getDataInterface(String interfaceName) {
		EObject dataInterface = dataInterfaceMap.get(interfaceName);
		if (dataInterface == null) {
			log.error("Could not find data interface " + interfaceName);
			log.info("Available data interfaces" + dataInterfaceMap.keySet());
		}

		return dataInterface;
	}

	public EObject getDataChannel(String channelName) {
		EObject dataChannel = dataChannelMap.get(channelName);
		if (dataChannel == null) {
			log.error("Could not find data channel " + channelName);
			log.info("Available data channls" + dataChannelMap.keySet());
		}

		return dataChannel;
	}
	public void addMethod(ComponentType type, Signature signature, boolean messaging) {

		if (!methodMap.containsKey(
				ModelCreationUtils.createMethodKey(signature.getName(), applyNameFixes(type.getTypeName())))) {
			methodMap.put(ModelCreationUtils.createMethodKey(signature.getName(), applyNameFixes(type.getTypeName())),
					createMethod(type, signature, messaging));
		}

	}
	

	public EObject getMethod(String methodName) {
		EObject method = methodMap.get(methodName);
		if (method == null) {
			log.error("Could not find method " + methodName);
			log.info("Available methods are " + methodMap.keySet());
		}
		return method;
	}

	public EObject getRole(String roleName) {
		EObject role = roleMap.get(roleName);
		if (role == null) {
			log.error("Could not find role " + roleName);
			log.info("Avialable roles are " + roleMap.keySet());
		}
		return role;
	}

	public void addRole(String roleName, EObject role) {
		if (!roleMap.containsKey(roleName)) {
			roleMap.put(roleName, role);
		}
	}

	public boolean isRole(String roleName) {
		return roleMap.containsKey(roleName);
	}

	public boolean isSEFF(String componentName, String methodName) {
		return seffMap.containsKey(ModelCreationUtils.createSEFFKey(methodName, componentName));
	}

	public void addSEFF(String componentName, String methodName, EObject seff) {
		seffMap.put(ModelCreationUtils.createSEFFKey(methodName, componentName), seff);
	}

	public void addSEFF(String componentName, String methodName, List<ExternalCall> externalCalls, String hostName,
			double meanResourceDemand) {
		if (!seffMap.containsKey(ModelCreationUtils.createSEFFKey(methodName, componentName))) {
			seffMap.put(ModelCreationUtils.createSEFFKey(methodName, componentName),
					createSEFF(componentName, methodName, externalCalls, hostName, meanResourceDemand));
		}
	}
	
	public void addDataSEFF(String componentName, String interfaceName, List<ExternalCall> externalCalls, String hostName,
			double meanResourceDemand) {
		if (!seffMap.containsKey(ModelCreationUtils.createSEFFKey(interfaceName, componentName))) {
			seffMap.put(ModelCreationUtils.createSEFFKey(interfaceName, componentName),
					createDataSEFF(componentName, interfaceName, externalCalls, hostName, meanResourceDemand));
		}
	}

	public EObject getSEFF(String componentName, String methodName) {
		String seffID = ModelCreationUtils.createSEFFKey(methodName, componentName);
		EObject seff = seffMap.get(seffID);
		if (seff == null) {
			log.error("Could not find SEFF " + seffID);
			log.info("Available SEFFs " + seffMap.keySet());
		}

		return seff;
	}

	private boolean hasComponentBeenAdded(String componentName) {
		return componentMap.containsKey(componentName);
	}
	
	public EObject getRoleFromCopy(String key) {
		 for (Map.Entry<String, EObject> entry : roleMap.entrySet()) {
			 String entryKey = entry.getKey().substring(0, entry.getKey().length()-1);
	            if (entryKey.equalsIgnoreCase(key) && !roleMapCopy.containsKey(entry.getKey())) {
	                roleMapCopy.put(entry.getKey(),entry.getValue());
	                return roleMap.get(entry.getKey());
	            }
	        }
		 return null;
	}
	
}
