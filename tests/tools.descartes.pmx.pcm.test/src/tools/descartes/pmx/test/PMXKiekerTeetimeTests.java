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

import org.junit.Ignore;
import org.junit.Test;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;

public class PMXKiekerTeetimeTests extends PMXPalladioTest{
	
	@Ignore
	@Test
	public void testKiekerTeetimeTraces2Eprints() throws Exception{
		String filename = "kiekerTeetimeData"+File.separator + "Eprints-logs"+File.separator;
		extractModel(filename);
		String resultPath = getResultPath(filename);

		System.out.println("Testing assertions");
		
		//components
		Repository repository = PCMLoader.loadRepository(new File(resultPath+"extracted.repository").toURI().toString());
		assertEquals(158, repository.getComponents__Repository().size());
		assertEveryComponentHasServiceEffectspecification(repository);
		

		//hosts
		ResourceEnvironment resourceEnvironment = PCMLoader.loadResourceenvironment(new File(resultPath+"extracted.resourceenvironment").toURI().toString());
		assertEquals(1,	resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
		assertEquals("0",
				resourceEnvironment.getResourceContainer_ResourceEnvironment().get(0).getEntityName());
		
		//deployment
		Allocation allocation = PCMLoader.loadAllocation(new File(resultPath+"extracted.allocation").toURI().toString());
		assertEquals(158, allocation.getAllocationContexts_Allocation().size());
		
	}

	/**
	 * 60 trace Files, 57 MB ==> scalability
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testKiekerlogs() throws Exception{
		String filename = "kiekerTeetimeData"+File.separator + "kieker-logs" + File.separator;
		extractModel(filename);
		String resultPath = getResultPath(filename);

		Repository repository = PCMLoader.loadRepository(new File(resultPath + "extracted.repository").toURI().toString());
		assertEveryComponentHasServiceEffectspecification(repository);
		
		

	}
	
	/**
	 * Kieker monitors Kieker
	 * 
	 * Problem: Extract seperate component or not
	 * 	==> LogFactory$Logger (Logger is a private ENUM class of LogFactory)
	 * 
	 */
	@Ignore
	@Test
	public void testKieker2logs(){
		String filename = "kiekerTeetimeData"+File.separator +"kieker2-logs";
		extractModel(filename);
		String resultPath = getResultPath(filename);

		testBasicAssertions(resultPath);
		
		System.out.println("Testing assertions");
		
		//components
		Repository repository = PCMLoader.loadRepository(new File(resultPath+"extracted.repository").toURI().toString());
		assertEquals(34, repository.getComponents__Repository().size());
		assertEveryComponentHasServiceEffectspecification(repository);
		

		//hosts
		ResourceEnvironment resourceEnvironment = PCMLoader.loadResourceenvironment(new File(resultPath+"extracted.resourceenvironment").toURI().toString());
		assertEquals(1,	resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
		assertEquals("Osterinsel",
				resourceEnvironment.getResourceContainer_ResourceEnvironment().get(0).getEntityName());
		
		//deployment
		Allocation allocation = PCMLoader.loadAllocation(new File(resultPath+"extracted.allocation").toURI().toString());
		assertEquals(34, allocation.getAllocationContexts_Allocation().size());
		
	}

}
