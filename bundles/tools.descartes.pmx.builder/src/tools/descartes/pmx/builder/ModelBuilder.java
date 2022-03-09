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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;

import kieker.common.util.signature.Signature;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import tools.descartes.pmx.util.ExternalCall;
import tools.descartes.pmx.util.Util;

public abstract class ModelBuilder implements IModelBuilder{
	private static final Logger log = Logger
			.getLogger(ModelBuilder.class);
	private Map<String,EObject> componentMap;
	private Map<String,EObject> interfaceMap;
	private Map<String,EObject> methodMap;
	private Map<String,EObject> hostMap;
	private Map<String,EObject> allocationMap;
	private Map<String,EObject> roleMap;
	private Map<String,EObject> assemblyMap;
	private Map<String,EObject> connectorMap;
	private Map<String,EObject> seffMap;
	public Map<String, EObject> dataTypeMap;
	
	public static String branding = "extracted using PMX - www.descartes.tools/pmx";

	
	public static String seperatorChar = "-";
	
	public String outputDir;

	public ModelBuilder(String outputDir){
		componentMap = new HashMap<String, EObject>();
		methodMap = new HashMap<String, EObject>();
		hostMap = new HashMap<String, EObject>();
		assemblyMap = new HashMap<String, EObject>();
		allocationMap = new HashMap<String, EObject>();
		interfaceMap = new HashMap<String, EObject>();
		roleMap = new HashMap<String, EObject>();
		seffMap = new HashMap<String, EObject>();
		connectorMap = new HashMap<String, EObject>();
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
		if(!allocationMap.containsKey(componentName+seperatorChar+hostName)){
			allocation = createAllocation(componentName, hostName);
			allocationMap.put(componentName+seperatorChar+hostName, allocation);
		}else{
			allocation = allocationMap.get(componentName+seperatorChar+hostName);
		}
		return allocation;
	}
	
	public void addInterface(String interfaceName){
		interfaceName = applyNameFixes(interfaceName);
		if(!interfaceMap.containsKey(interfaceName)){
			EObject newInterface = createInterface(interfaceName);
			interfaceMap.put(interfaceName, newInterface);			
		}
	}

	public EObject addAssembly(String name) {
		EObject assembly;
		name = applyNameFixes(name);

		if(!assemblyMap.containsKey(name)){
			assembly = createAssembly(name);
			assemblyMap.put(name, assembly);
		}else{
			assembly = assemblyMap.get(name);
		}
		return assembly;
	}

	public EObject getAssembly(String assemblyName){
		assemblyName = applyNameFixes(assemblyName);
		EObject assembly = assemblyMap.get(assemblyName);
		if (assembly == null) {
			log.error("Could not find AssemblyContext: " + assemblyName);
			log.info("Avialable assemblies " + assemblyMap.keySet());
		}
		return assembly;
	}
	
	public void addConnectionToAssemblies(String requiringAssemblyName, String providingAssemblyName){
		// if(providingAssemblyName.equals(requiringAssemblyName)){
		// log.error("Cannot self-connect assembly "+providingAssemblyName);
		// return;
		// }
		String key  = "Connector " + "Assembly_"
				+ requiringAssemblyName + " <" + requiringAssemblyName + ">"
				+ " -> " + "Assembly_" + providingAssemblyName + " <"
				+ providingAssemblyName + ">";
		if(!connectorMap.containsKey(key)){
			EObject connector = connectAssemblies(providingAssemblyName, requiringAssemblyName);
			connectorMap.put(key, connector);
		}
	}

	
	public EObject getHost(String hostName){
		EObject host = hostMap.get(hostName);
		if(host == null){
			log.info("host "+hostName +" not found");
			log.info("HostMap  " + hostMap.keySet());
		}
		return host;
	}
	
	public void addHost(String hostName, int numCores) {
		if(!hostMap.containsKey(hostName)){
			EObject host = createHost(hostName, numCores);
			hostMap.put(hostName, host);
		}
	}

	public EObject getComponent(String componentName){
		componentName = applyNameFixes(componentName);
		EObject component = componentMap.get(componentName);
		if(component == null){
			log.error("requested component ["+componentName+"] has not been created");
			log.error(""+componentMap.keySet());
		}
		return component;
	}
	
	public static String applyNameFixes(String componentName){
		if(componentName.contains("$")){
			//log.info("FIX:" + componentName + " ==> "+componentName.split("\\$")[0]);
			componentName = componentName.split("\\$")[0];	//JAVA $
		}
		return componentName;
	}
	
	public void addComponent(String componentName) {
		componentName = applyNameFixes(componentName);
		if(!hasComponentBeenAdded(componentName)){
			componentMap.put(componentName, createComponent(componentName));
		}
	}

	public void addRequiredRole(String requiredComponentName, String interfaceName) {
		String requiredRoleName = "Required_" + interfaceName + ModelBuilder.seperatorChar
				+ requiredComponentName;
		if(!roleMap.containsKey(requiredRoleName)){
			roleMap.put(requiredRoleName, createRequiredRole(requiredComponentName, interfaceName));
		}
	}
	
	public void addProvidedRole(String providedComponentName, String interfaceName){
		String providedRoleName = "Provided_" + interfaceName + ModelBuilder.seperatorChar
				+ providedComponentName;
		if(!roleMap.containsKey(providedRoleName)){
			roleMap.put(providedRoleName, createProvidedRole(providedComponentName, interfaceName));
		}
	}
	
	public Set<String> getRoles(){
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

	public void addMethod(ComponentType type, Signature signature){ 	
		if(!methodMap.containsKey(Util.createMethodKey(signature.getName(), applyNameFixes(type.getTypeName())))){
			methodMap.put(Util.createMethodKey(signature.getName(), applyNameFixes(type.getTypeName())), createMethod(type, signature));
		}
	}
	
	public EObject getMethod(String methodName) 	{
		EObject method = methodMap.get(methodName);
		if(method == null){
			log.error("Could not find method "+methodName);
			log.info("Available methods are "+ methodMap.keySet());
		}
		return method;
	}
	
	public EObject getRole(String roleName) {
		EObject role = roleMap.get(roleName);
		if(role == null){
			log.error("Could not find role "+roleName);
			log.info("Avialable roles are "+roleMap.keySet());
		}
		return role;
	}
	
	public void addRole(String roleName, EObject role){
		if(!roleMap.containsKey(roleName)){
			roleMap.put(roleName, role);
		}
	}
	
	public boolean isRole(String roleName){
		return roleMap.containsKey(roleName);
	}
	
	public boolean isSEFF(String componentName,String methodName){
		return seffMap.containsKey(Util.createSEFFKey(methodName, componentName));
	}


	public void addSEFF(String componentName,String methodName, EObject seff) {
		seffMap.put(Util.createSEFFKey(methodName, componentName), seff);
	}
	
	public void addSEFF(String componentName,String methodName, List<ExternalCall> externalCalls, String hostName, double meanResourceDemand) {
		if(!seffMap.containsKey(Util.createSEFFKey(methodName, componentName))){
			seffMap.put(Util.createSEFFKey(methodName, componentName), createSEFF(componentName, methodName, externalCalls, hostName, meanResourceDemand));
		}
	}
	
	public EObject getSEFF(String componentName, String methodName) {
		String seffID = Util.createSEFFKey(methodName, componentName);
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
	
}
