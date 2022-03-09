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
package org.palladiosimulator.pmxupgrade.pcm.builder.persistance;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMSaver {
	private static final Logger log = Logger
			.getLogger(PCMSaver.class);
	private static ResourceSet resourceSet; // = new ResourceSetImpl();

	/**
	 * Saves PCM {@link org.palladiosimulator.pcm.system.System System} to file.
	 * 
	 * @param system model
	 * @param relativePath
	 */
	public static void save(org.palladiosimulator.pcm.system.System system,
			String relativePath) {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("system", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
		resource.getContents().add(system);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			log.error(e);
		}
	}

	/**
	 * Saves PCM {@link Allocation} to file.
	 * 
	 * @param allocation model
	 * @param relativePath
	 */
	public static void save(Allocation allocation, String relativePath) {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("allocation", new XMIResourceFactoryImpl());
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
		resource.getContents().add(allocation);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			log.error(e);
		}
	}

	/**
	 * Saves PCM {@link Repository} to file.
	 * 
	 * @param repository model
	 * @param relativePath
	 */
	public static void save(Repository repository, String relativePath) {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("repository", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
		resource.getContents().add(repository);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			log.error(e);
		} catch (NullPointerException e) {
			log.error(e);
		}
	}

	/**
	 * Saves PCM {@link ResourceEnvironment} to file.
	 * 
	 * @param resourceEnvironment model
	 * @param relativePath
	 */
	public static void save(ResourceEnvironment resourceEnvironment,
			String relativePath) {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("resourceenvironment", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
		resource.getContents().add(resourceEnvironment);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves PCM {@link ResourceEnvironment} to file.
	 * 
	 * @param relativePath
	 */
	public static void save(UsageModel usagemodel,
			String relativePath) {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("usagemodel", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
		resource.getContents().add(usagemodel);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ResourceSet getResourceSet() {
		return resourceSet;
	}
	public static void init(){
		if(resourceSet == null){
			resourceSet = new ResourceSetImpl();
		}
	}

	public static void setResourceSet(ResourceSet resourceSet2) {
		resourceSet = resourceSet2;
	}
	
	
//	/**
//	 * Saves PCM {@link org.palladiosimulator.pcm.system.System System} to file.
//	 * 
//	 * @param system model
//	 * @param relativePath
//	 */
//	public static void save(ResourceRepository resourcerepository,
//			String relativePath) {
//		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
//		Map<String, Object> m = reg.getExtensionToFactoryMap();
//		m.put("resourcerepository", new XMIResourceFactoryImpl());
//		ResourceSet resourceSet = new ResourceSetImpl();
//		Resource resource = resourceSet.createResource(URI.createURI(relativePath));
//		resource.getContents().add(resourcerepository);
//		try {
//			resource.save(Collections.EMPTY_MAP);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
}
