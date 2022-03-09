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
package tools.descartes.pmx.pcm.builder.persistance;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.system.SystemPackage;

public class PCMLoader {
	
	/**
	 * Loads PCM {@link org.palladiosimulator.pcm.system.System System} model from file.
	 * @param relativePath to file
	 * @return loaded system specification
	 */
	public static org.palladiosimulator.pcm.system.System loadSystem(String relativePath){
		SystemPackage.eINSTANCE.eClass();
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry reg = org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("system", new XMIResourceFactoryImpl());
	    ResourceSet resSet = new ResourceSetImpl();
	    org.eclipse.emf.ecore.resource.Resource resource = resSet.getResource(URI
	        .createURI(relativePath), true);
	    org.palladiosimulator.pcm.system.System emfElement = (org.palladiosimulator.pcm.system.System) resource.getContents().get(0);
	    return emfElement;
	}

	/**
	 * Loads PCM {@link Repository} model from file.
	 * @param relativePath to file
	 * @return loaded {@link Repository} specification
	 */
	public static Repository loadRepository(String relativePath){
		RepositoryPackage.eINSTANCE.eClass();
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry reg = org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("repository", new XMIResourceFactoryImpl());
	    ResourceSet resSet = new ResourceSetImpl();
	    org.eclipse.emf.ecore.resource.Resource resource = resSet.getResource(URI
	        .createURI(relativePath), true);
	    	Repository emfElement = (Repository) resource.getContents().get(0);
	    return emfElement;
	}

	/**
	 * Loads PCM {@link Allocation} model from file.
	 * @param relativePath to file
	 * @return loaded system specification
	 */
	public static Allocation loadAllocation(String relativePath){
		AllocationPackage.eINSTANCE.eClass();
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry reg = org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("allocation", new XMIResourceFactoryImpl());
	    ResourceSet resSet = new ResourceSetImpl();
	    org.eclipse.emf.ecore.resource.Resource resource = resSet.getResource(URI
	        .createURI(relativePath), true);
	    Allocation emfElement = (Allocation) resource.getContents().get(0);
	    return emfElement;
	}

	/**
	 * Loads PCM {@link ResourceEnvironment} model from file.
	 * @param relativePath to file
	 * @return loaded {@link ResourceEnvironment} specification
	 */
	public static ResourceEnvironment loadResourceenvironment(String relativePath){
		ResourceenvironmentPackage.eINSTANCE.eClass();
		org.eclipse.emf.ecore.resource.Resource.Factory.Registry reg = org.eclipse.emf.ecore.resource.Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("resourceenvironment", new XMIResourceFactoryImpl());
	    ResourceSet resSet = new ResourceSetImpl();
	    org.eclipse.emf.ecore.resource.Resource resource = resSet.getResource(URI
	        .createURI(relativePath), true);
	    ResourceEnvironment emfElement = (ResourceEnvironment) resource.getContents().get(0);
	    return emfElement;
	}
	
}
