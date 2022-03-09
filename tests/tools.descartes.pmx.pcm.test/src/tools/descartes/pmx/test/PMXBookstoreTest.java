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

import java.io.File;

import org.junit.Test;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;

import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;

public class PMXBookstoreTest extends PMXPalladioTest{
	
	/**
	 * The bookstore application is a non-distributed small application that has
	 * been used to test the .Net adapter of Kieker.
	 */
	@Test
	public void testWithBookstoreTrace() {
		String filename = "bookstore"+File.separator+"kieker-20151008-124004394-UTC-WIN-JQHNDE89VN4-KIEKER"+File.separator;
		extractModel(filename);
		String resultPath = getResultPath(filename);

		//components
		Repository repository = PCMLoader.loadRepository(new File(resultPath+"extracted.repository").toURI().toString());
		assertEquals(4, repository.getComponents__Repository().size());
		assertEveryComponentHasServiceEffectspecification(repository);
		
		//system
		System system = PCMLoader.loadSystem(new File(resultPath+"extracted.system").toURI().toString());
		assertEquals(1, system.getProvidedRoles_InterfaceProvidingEntity().size());
		assertSystemProvidedRolesToHaveAnInterface(system);
		assertEquals(4, system.getAssemblyContexts__ComposedStructure().size());
		assertAssemblyConnectorsToHaveAllParametersSet(system);

		//hosts
		ResourceEnvironment resourceEnvironment = PCMLoader.loadResourceenvironment(new File(resultPath+"extracted.resourceenvironment").toURI().toString());
		assertEquals(1,	resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
		assertEquals("WIN-JQHNDE89VN4",
				resourceEnvironment.getResourceContainer_ResourceEnvironment().get(0).getEntityName());
		
		//deployment
		Allocation allocation = PCMLoader.loadAllocation(new File(resultPath+"extracted.allocation").toURI().toString());
		assertEquals(4, allocation.getAllocationContexts_Allocation().size());	
	}
}
