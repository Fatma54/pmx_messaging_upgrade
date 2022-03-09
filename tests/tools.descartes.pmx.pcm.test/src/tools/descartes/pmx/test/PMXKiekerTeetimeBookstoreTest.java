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
package tools.descartes.pmx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;

import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;

public class PMXKiekerTeetimeBookstoreTest extends PMXPalladioTest{
	
	/**
	 * Traces from Kieker Teetime project - bookstore
	 *  
	 * Can be downloaded from 
	 * https://github.com/teetime-framework/Kieker-Teetime-Stages
	 */
	@Test
	public void testKiekerTeetimeTraces1Bookstore() throws Exception{
		String filename = "kiekerTeetimeData"+File.separator + "bookstore-logs" + File.separator;
		extractModel(filename);
		String resultPath = getResultPath(filename);

		//components
		Repository repository = PCMLoader.loadRepository(new File(resultPath + "extracted.repository").toURI().toString());
		assertEveryComponentHasServiceEffectspecification(repository);
		assertEquals(3, repository.getComponents__Repository().size());
		
		//system
		System system = PCMLoader.loadSystem(new File(resultPath+"extracted.system").toURI().toString());
		assertEquals(1, system.getProvidedRoles_InterfaceProvidingEntity().size());
		assertSystemProvidedRolesToHaveAnInterface(system);
		assertEquals(4, system.getAssemblyContexts__ComposedStructure().size());
		assertAssemblyConnectorsToHaveAllParametersSet(system);

		//hosts
		ResourceEnvironment resourceEnvironment = PCMLoader.loadResourceenvironment(new File(resultPath +"extracted.resourceenvironment").toURI().toString());
		assertEquals(2,resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
		List<String> hostNames = new ArrayList<>();
		hostNames.add("SRV0");
		hostNames.add("SRV1");
		for(ResourceContainer x: resourceEnvironment.getResourceContainer_ResourceEnvironment()){
			String name = x.getEntityName();
			assertTrue(hostNames.contains(name));
			hostNames.remove(name);
		}
		assertTrue(hostNames.isEmpty());

		//deployment
		Allocation allocation = PCMLoader.loadAllocation(new File(resultPath+"extracted.allocation").toURI().toString());
		List<String> allocationNames = new ArrayList<>();
		allocationNames.add("Allocation_Assembly_Bookstore#SRV0 <Bookstore> <Bookstore>");
		allocationNames.add("Allocation_Assembly_CRM#SRV0 <CRM> <CRM>");
		allocationNames.add("Allocation_Assembly_Catalog#SRV0 <Catalog> <Catalog>");
		allocationNames.add("Allocation_Assembly_Catalog#SRV1 <Catalog> <Catalog>");
		for(AllocationContext x: allocation.getAllocationContexts_Allocation()){
			String name = x.getEntityName();
			assertTrue(allocationNames.contains(name));
			allocationNames.remove(name);
		}
		assertTrue(allocationNames.isEmpty());
	}

}
