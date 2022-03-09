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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;
import tools.descartes.pmx.pcm.console.PMXCommandLinePCM;

public class PMXPalladioTest {
	 @Rule
	 public final ExpectedException exception = ExpectedException.none();

	public void assertEveryComponentHasServiceEffectspecification(Repository repository) {
		for (RepositoryComponent component : repository.getComponents__Repository()) {
			assertTrue(0 < ((BasicComponent) component).getServiceEffectSpecifications__BasicComponent().size());
		}
	}
	
	public void assertAssemblyConnectorsToHaveAllParametersSet(org.palladiosimulator.pcm.system.System system) {
		for(Connector connector: system.getConnectors__ComposedStructure()){
			assert(null !=((AssemblyConnector)connector).getProvidedRole_AssemblyConnector());
			assert(null !=((AssemblyConnector)connector).getRequiredRole_AssemblyConnector());
			assert(null !=((AssemblyConnector)connector).getProvidingAssemblyContext_AssemblyConnector());
			assert(null !=((AssemblyConnector)connector).getRequiringAssemblyContext_AssemblyConnector());
		}
	}
	
	public void assertSystemProvidedRolesToHaveAnInterface(org.palladiosimulator.pcm.system.System system) {
		for(ProvidedRole providedRole :system.getProvidedRoles_InterfaceProvidingEntity()){
			providedRole.getProvidingEntity_ProvidedRole();
		}
	}
	
	public void testBasicAssertions(String resultPath) {
		Repository repository = PCMLoader.loadRepository(new File(resultPath + "extracted.repository").toURI().toString());
		assertTrue("No repository component extracted", 0 < repository.getComponents__Repository().size());
		assertEveryComponentHasServiceEffectspecification(repository);
	}

	public static boolean extractModelFromMultipleSources(List<String> inputFiles, String outputdir) {
		ArrayList<String> args = new ArrayList<String>();
		args.add("-i");
		args.addAll(inputFiles);
		args.add("-o");
		args.add(outputdir);
		deleteDirectory(outputdir);		
		return extractModel(args.toArray(new String[args.size()]));		
	}
	
	public static boolean extractModel(String relativeDirectory, ArrayList<String> furtherArgs) {
		BasicConfigurator.configure();
		ArrayList<String> args = new ArrayList<String>();
		args.add("-i");
		args.add(System.getProperty("user.dir")+File.separator+"testFiles"+File.separator+relativeDirectory);
		args.add("-o");
		args.add(System.getProperty("user.dir")+File.separator+"extracted"+File.separator+relativeDirectory);
		if(null != furtherArgs){
			args.addAll(furtherArgs);
		}
		deleteDirectory(System.getProperty("user.dir")+File.separator+"extracted"+File.separator+relativeDirectory);		
		return extractModel(args.toArray(new String[args.size()]));
	}
	
	public String getResultPath(String filename){
		return "extracted"+File.separator +filename + File.separator+ "pcm" + File.separator;
	}
	
	public static boolean extractModel(String directory) {
		return extractModel(directory, null);
	}
	
	private static boolean extractModel(String[] args) {
		PMXCommandLinePCM.execute(args);
		return true;
	}
	
	public static boolean deleteDirectory(String directory) {
		return Util.deleteDirectory(new File(directory));
	}
}
