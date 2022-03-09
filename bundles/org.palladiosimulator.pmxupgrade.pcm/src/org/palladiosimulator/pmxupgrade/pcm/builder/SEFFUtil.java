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
package org.palladiosimulator.pmxupgrade.pcm.builder;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Interface;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;

public class SEFFUtil {

	public static void addSEFF(BasicComponent component, String stoex) {
		ResourceDemandingSEFF resourceDemandingSeff = SeffFactory.eINSTANCE
				.createResourceDemandingSEFF();
		addResourceDemandingInternalBehavior(resourceDemandingSeff, stoex);

		Repository repository = component.getRepository__RepositoryComponent();
		Interface interface1 = repository.getInterfaces__Repository().get(0);

		OperationSignature signature = RepositoryFactory.eINSTANCE
				.createOperationSignature();
		signature.setEntityName("MySignature");
		signature
				.setInterface__OperationSignature((OperationInterface) interface1);

		resourceDemandingSeff.setDescribedService__SEFF(signature);
		resourceDemandingSeff
				.setBasicComponent_ServiceEffectSpecification(component);

		component.getServiceEffectSpecifications__BasicComponent().add(
				(ServiceEffectSpecification) resourceDemandingSeff);
	}

	private static void addResourceDemandingInternalBehavior(
			ResourceDemandingSEFF resourceDemandingSeff, String stoex) {
		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		startAction.setEntityName("MyStartAction");
		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		InternalAction internalAction = SeffFactory.eINSTANCE
				.createInternalAction();

		startAction.setSuccessor_AbstractAction(internalAction);
		internalAction.setSuccessor_AbstractAction(stopAction);

		addParametricResourceDemand(internalAction, stoex);

		resourceDemandingSeff.getSteps_Behaviour().add(startAction);
		resourceDemandingSeff.getSteps_Behaviour().add(internalAction);
		resourceDemandingSeff.getSteps_Behaviour().add(stopAction);
	}

	private static ParametricResourceDemand addParametricResourceDemand(
			InternalAction internalAction, String stoex) {
		ParametricResourceDemand parametricResourceDemand = SeffPerformanceFactory.eINSTANCE
				.createParametricResourceDemand();
		parametricResourceDemand
				.setAction_ParametricResourceDemand(internalAction);
		addProcessingResourceFromResourceEnvironment(null, parametricResourceDemand);

		PCMRandomVariable resourceDemandValue = CoreFactory.eINSTANCE
				.createPCMRandomVariable();
		resourceDemandValue.setSpecification(stoex);
		parametricResourceDemand
				.setSpecification_ParametericResourceDemand(resourceDemandValue);

		return parametricResourceDemand;
	}

	private static void addProcessingResourceFromResourceEnvironment(ResourceEnvironment resourceEnvironment,
			ParametricResourceDemand parametricResourceDemand) {
		//TODO Fix
		ProcessingResourceSpecification processingResourceSpecification = null;
		ProcessingResourceType processingResourceType = processingResourceSpecification
				.getActiveResourceType_ActiveResourceSpecification();
		parametricResourceDemand
				.setRequiredResource_ParametricResourceDemand(processingResourceType);
	}



}