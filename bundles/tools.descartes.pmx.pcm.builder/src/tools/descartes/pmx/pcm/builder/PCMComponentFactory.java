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
package tools.descartes.pmx.pcm.builder;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.repository.impl.BasicComponentImpl;

public class PCMComponentFactory {

	public static Repository createComponentRepository() {
		Repository repository = RepositoryFactory.eINSTANCE.createRepository();
		repository.setEntityName("Repository containing other elements");		
		return repository;
	}
	
//	public static boolean isComponentInRepository(String name, Repository repository){
//		for(RepositoryComponent component :repository.getComponents__Repository()){
//			if(component.getEntityName().equals(name)) {
//				return true;
//			}
//		}
//		return false;
//	}
	public static BasicComponent createComponent(String name,
			Repository repository) {
		BasicComponent component = RepositoryFactory.eINSTANCE
				.createBasicComponent();
		component.setEntityName(name);
		repository.getComponents__Repository().add(component);
		return component;
	}

	static void addSignatureTo(OperationInterface interface1) {
		OperationSignature signature = RepositoryFactory.eINSTANCE
				.createOperationSignature();
		signature.setEntityName("MySignature");
		signature.setInterface__OperationSignature(interface1);
	}
	
	private static boolean inheritsFromBasicComponent(Object class1) {
		return class1.getClass().equals(BasicComponentImpl.class);
	}
}
