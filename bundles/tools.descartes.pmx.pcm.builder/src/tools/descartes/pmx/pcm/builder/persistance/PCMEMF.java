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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

public class PCMEMF {
	private static final Logger log = Logger
			.getLogger(PCMEMF.class);
	private static ResourceSet resourceSet;

	/**
	 * Saves PCM {@link org.palladiosimulator.pcm.system.System System} to file.
	 * 
	 * @param system
	 *            model
	 * @param path
	 */
	public static void add(org.palladiosimulator.pcm.system.System system,
			String path) {
		SystemPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("system", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI(path));
		resource.getContents().add(system);
	}

	/**
	 * Saves PCM {@link Allocation} to file.
	 * 
	 * @param allocation
	 *            model
	 * @param path
	 */
	public static void add(Allocation allocation, String path) {
		AllocationPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("allocation", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI(path));
		resource.getContents().add(allocation);
	}

	/**
	 * Saves PCM {@link Repository} to file.
	 * 
	 * @param repository
	 *            model
	 * @param path
	 */
	public static void add(Repository repository, String path) {
		RepositoryPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("repository", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI(path));
		resource.getContents().add(repository);
	}

	/**
	 * Saves PCM {@link ResourceEnvironment} to file.
	 * 
	 * @param resourceEnvironment
	 *            model
	 * @param path
	 */
	public static void add(ResourceEnvironment resourceEnvironment,
			String path) {
		ResourceenvironmentPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("resourceenvironment", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI(path));
		resource.getContents().add(resourceEnvironment);
	}

	/**
	 * Saves PCM {@link ResourceEnvironment} to file.
	 * 
	 * @param resourceEnvironment
	 *            model
	 * @param path
	 */
	public static void add(UsageModel usagemodel, String path) {
		UsagemodelPackage.eINSTANCE.eClass();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("usagemodel", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI
				.createURI(path));
		resource.getContents().add(usagemodel);
	}

	public static void saveAll() {
		resourceSet.getResources().remove(0);	// do not save resource types
		
	    HashMap<String, Object> options = new HashMap<String, Object>();
	    options.put(XMLResource.OPTION_ENCODING, "UTF-8"); //Collections.EMPTY_MAP) ==> ANSI

		countElements();
	    for (Resource resource : resourceSet.getResources()) {
			try {
				resource.save(options);	
			} catch (IOException e) {
				log.error(e);
			}
			log.info("saved "+resource.getURI().lastSegment()+" --> (path: "+resource.getURI().devicePath()+")");
			deletePath(resource.getURI());
			//File.resource.getURI()
		}
	}
	
	static void deletePath(URI pathURI){
		Path path = Paths.get(pathURI.path());
		Charset charset = StandardCharsets.UTF_8;
	
		String content;
		try {
			content = new String(Files.readAllBytes(path), charset);			
			String pathToDelete = path.getParent().toString().replace("\\", "\\\\")+"\\\\";		
//			if(!content.contains(pathToDelete)){
//				log.error("Path problems "+pathToDelete);
//				log.info(""+content);
//			}
			content = content.replace(pathToDelete, "");
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			log.error("Error replacing strings in file", e);
			e.printStackTrace();
		}
	}

	public static void countElements() {
		int cntElements = 0;
		int cntCrossreferences = 0;
		for (Resource resource : resourceSet.getResources()) {
			// iterate over all classes
			TreeIterator<EObject> iterator = resource.getAllContents();
			while (iterator.hasNext()) {
				EObject eObject = iterator.next();
				cntElements++;
				cntCrossreferences += eObject.eCrossReferences().size();
			}
		}
		log.info("Complexity of extracted models: #elements " + cntElements + " #references " + cntCrossreferences);
	}

	public static ResourceSet getResourceSet() {
		return resourceSet;
	}

	public static void init() {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
		}
	}

	public static void setResourceSet(ResourceSet resourceSet2) {
		resourceSet = resourceSet2;
	}
}
