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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Signature;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.ProbabilisticBranchTransition;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.IModelBuilder;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.util.ModelCreationUtils;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.ExternalCall;


public class PCMSEFFFactory {
	private static final Logger log = Logger.getLogger(PCMSEFFFactory.class);

	public static ResourceDemandingSEFF createSEFF(IModelBuilder builder, BasicComponent component, Signature signature,
												   List<ExternalCall> externalCalls, ResourceContainer host, double meanResourceDemand) {

		ResourceDemandingSEFF seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		seff.setBasicComponent_ServiceEffectSpecification(component);
		seff.setDescribedService__SEFF(signature);

		// create start, stop and branchAction
		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		startAction.setEntityName("startAction");
		seff.getSteps_Behaviour().add(startAction);

		AbstractAction currentAction;
		AbstractAction nextAction;
		double minimumResourceDemandThreshhold = 0.00;
		if (meanResourceDemand > minimumResourceDemandThreshhold) {
			InternalAction internalAction = SeffFactory.eINSTANCE.createInternalAction();
			internalAction.setEntityName("method internal");
			seff.getSteps_Behaviour().add(internalAction);

			addResourceDemand(internalAction, meanResourceDemand, host);

			connectActions(startAction, internalAction);

			currentAction = internalAction;
		} else {
			currentAction = startAction;
		}

		/**
		 * Loops [total average calls] times choosing the transaction
		 * probability based
		 */

		double totalAverageCalls = 0;
		for (ExternalCall externalCall : externalCalls) {
			totalAverageCalls += externalCall.getNumCalls();
		}

		// TODO check if methodNames are the same (signature.getEntityName() ==
		// externalCall.getMethodName()) ==> inheritance
		for (ExternalCall call : externalCalls) {
			if (signature == null) {
				log.error("signature is null");
			} else {
				log.info(signature.getEntityName());
				log.info(call.getMethodName());
				if (signature.getEntityName().endsWith(call.getMethodName())) {
					log.info("Same interface: " + component.getEntityName() + " and " + call.getClassName() + " ("
							+ call.getMethodName() + ")" + "<== Candidate for inheritance");
					OperationInterface op = (OperationInterface) builder
							.getInterface("I" + ModelBuilder.applyNameFixes(component.getEntityName()));
				}
			}
		}

		if (externalCalls.size() > 1) {
			if (totalAverageCalls >= 1) {
				if (totalAverageCalls == 1) {
					nextAction = createBranchAction(builder, component, seff, externalCalls);
				} else {
					nextAction = createLoopActionIncludingInternalBranchingAction(builder, component, seff,
							externalCalls);
				}
				connectActions(currentAction, nextAction);
				currentAction = nextAction;
			}
		} else if (externalCalls.size() == 1) {
			if (totalAverageCalls >= 1) {
				if (totalAverageCalls == 1) {
					nextAction = createExternalCallAction(builder, component, seff, externalCalls.get(0));
				} else {
					nextAction = createLoopAction(builder, component, seff, externalCalls.get(0));
				}
				connectActions(currentAction, nextAction);
				currentAction = nextAction;
			}
		}

		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		stopAction.setEntityName("stopAction");
		seff.getSteps_Behaviour().add(stopAction);
		connectActions(currentAction, stopAction);
		return seff;
	}

	private static AbstractAction createLoopActionIncludingInternalBranchingAction(IModelBuilder builder,
			BasicComponent component, ResourceDemandingSEFF seff, List<ExternalCall> externalCalls) {
		LoopAction loopAction = SeffFactory.eINSTANCE.createLoopAction();
		PCMRandomVariable iterationCount = CoreFactory.eINSTANCE.createPCMRandomVariable();
		double totalAverageCalls = 0;
		for (ExternalCall externalCall : externalCalls) {
			totalAverageCalls += externalCall.getNumCalls();
		}
		iterationCount.setSpecification("" + (int) Math.ceil(totalAverageCalls));
		loopAction.setIterationCount_LoopAction(iterationCount);
		loopAction.setEntityName("Loop action with internal branch actions");
		seff.getSteps_Behaviour().add(loopAction);

		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();

		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		startAction.setEntityName("startAction");
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);

		AbstractAction externalAction = null;
		if (externalCalls.size() > 1) {
			externalAction = createBranchAction(builder, component, seff, externalCalls);
		} else if (externalCalls.size() == 1) {
			externalAction = createExternalCallAction(builder, component, seff, externalCalls.get(0));
		}
		resourceDemandingBehaviour.getSteps_Behaviour().add(externalAction);
		connectActions(startAction, externalAction);

		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		stopAction.setEntityName("stopAction");
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);
		connectActions(externalAction, stopAction);

		resourceDemandingBehaviour.setAbstractLoopAction_ResourceDemandingBehaviour(loopAction);

		return loopAction;
	}

	private static AbstractAction createBranchAction(IModelBuilder builder, BasicComponent component,
			ResourceDemandingSEFF seff, List<ExternalCall> externalCalls) {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		branchAction.setEntityName("external branching");
		seff.getSteps_Behaviour().add(branchAction);

		double totalCalls = 0;
		for (ExternalCall externalCall : externalCalls) {
			totalCalls += externalCall.getNumCalls();
		}

		for (ExternalCall externalCall : externalCalls) {
			branchAction.getBranches_Branch()
					.add(getBranchTransition(builder, component, externalCall, (int) totalCalls));
		}

		/** Quick and dirty rounding error fix */
		if (((ProbabilisticBranchTransition) branchAction.getBranches_Branch().get(0))
				.getBranchProbability() == 0.3333333333333333) {
			((ProbabilisticBranchTransition) branchAction.getBranches_Branch().get(0))
					.setBranchProbability(0.3333333333333334);
		}
		return branchAction;
	}

	private static AbstractBranchTransition getBranchTransition(IModelBuilder builder, BasicComponent component,
			ExternalCall externalCall, int totalCalls) {
		// Guarded uses BranchCondition (e.g. file type)
		// AbstractBranchTransition branchTransition =
		// SeffFactory.eINSTANCE.createGuardedBranchTransition();
		ProbabilisticBranchTransition branchTransition = SeffFactory.eINSTANCE.createProbabilisticBranchTransition();
		branchTransition.setEntityName("" + externalCall.getClassName() + " " + externalCall.getMethodName());
		double probability = externalCall.getNumCalls() / totalCalls;
		branchTransition.setBranchProbability(probability);

		ResourceDemandingBehaviour resourceDemandingBehaviour = createResourceDemandingBehaviorWithExternalAction(
				builder, component, externalCall);
		branchTransition.setBranchBehaviour_BranchTransition(resourceDemandingBehaviour);

		return branchTransition;
	}

	private static AbstractAction createLoopAction(IModelBuilder builder, BasicComponent component,
			ResourceDemandingSEFF seff, ExternalCall externalCall) {
		LoopAction loopAction = SeffFactory.eINSTANCE.createLoopAction();
		PCMRandomVariable iterationCount = CoreFactory.eINSTANCE.createPCMRandomVariable();
		iterationCount.setSpecification("" + (int) (Math.ceil(externalCall.getNumCalls())));
		loopAction.setIterationCount_LoopAction(iterationCount);
		loopAction.setEntityName(externalCall.getClassName() + "." + externalCall.getMethodName() + "Loop"); // loop
																												// iteration
																												// has
																												// to
																												// be
																												// int
		seff.getSteps_Behaviour().add(loopAction);

		ResourceDemandingBehaviour resourceDemandingBehaviour = createResourceDemandingBehaviorWithExternalAction(
				builder, component, externalCall);

		resourceDemandingBehaviour.setAbstractLoopAction_ResourceDemandingBehaviour(loopAction);

		return loopAction;
	}

	private static ResourceDemandingBehaviour createResourceDemandingBehaviorWithExternalAction(IModelBuilder builder,
			BasicComponent component, ExternalCall externalCall) {
		ResourceDemandingBehaviour resourceDemandingBehaviour = SeffFactory.eINSTANCE
				.createResourceDemandingBehaviour();

		StartAction startAction = SeffFactory.eINSTANCE.createStartAction();
		startAction.setEntityName("startAction");
		resourceDemandingBehaviour.getSteps_Behaviour().add(startAction);

		AbstractAction externalCallAction = createExternalCallAction(builder, component, resourceDemandingBehaviour,
				externalCall);
		resourceDemandingBehaviour.getSteps_Behaviour().add(externalCallAction);
		connectActions(startAction, externalCallAction);

		StopAction stopAction = SeffFactory.eINSTANCE.createStopAction();
		stopAction.setEntityName("stopAction");
		resourceDemandingBehaviour.getSteps_Behaviour().add(stopAction);
		connectActions(externalCallAction, stopAction);
		return resourceDemandingBehaviour;
	}

	private static AbstractAction createExternalCallAction(IModelBuilder builder, BasicComponent component,
			ResourceDemandingBehaviour seff, ExternalCall externalCall) {
		ExternalCallAction externalCallAction = SeffFactory.eINSTANCE.createExternalCallAction();
		externalCallAction.setEntityName(externalCall.getClassName() + "." + externalCall.getMethodName());
		externalCallAction.setCalledService_ExternalService(
				(OperationSignature) builder.getMethod(ModelCreationUtils.createMethodKey(externalCall.getMethodName(),
						ModelBuilder.applyNameFixes(externalCall.getClassName()))));
		builder.addRequiredRole(component.getEntityName(), "I" + externalCall.getClassName());
		OperationRequiredRole role = (OperationRequiredRole) builder.getRole("Required_" + "I"
				+ externalCall.getClassName() + ModelBuilder.seperatorChar + component.getEntityName());
		externalCallAction.setRole_ExternalService(role);
		seff.getSteps_Behaviour().add(externalCallAction);

		return externalCallAction;
	}

	private static void addResourceDemand(InternalAction action, double meanResourceDemand, ResourceContainer host) {
		ParametricResourceDemand parametricResourceDemand = SeffPerformanceFactory.eINSTANCE
				.createParametricResourceDemand();
		EList<ProcessingResourceSpecification> resources = host.getActiveResourceSpecifications_ResourceContainer();
		ProcessingResourceType cpu = resources.get(0).getActiveResourceType_ActiveResourceSpecification();

		parametricResourceDemand.setRequiredResource_ParametricResourceDemand(cpu);
		parametricResourceDemand.setAction_ParametricResourceDemand(action);
		PCMRandomVariable randomVariable = CoreFactory.eINSTANCE.createPCMRandomVariable();
		// Expression x = randomVariable.getExpression();
		// randomVariable.setVariableCharacterisation_Specification(x);
		// randomVariable.setSpecification("IntPMF[(5; 0.0)(10; 0.051)(20;
		// 0.134)(30; 0.193)(40; 0.212)(50; 0.224)(60; 0.186)]*100000");
		// randomVariable.setSpecification("IntPMF["+"("+Double.toString(meanResourceDemand)+";1)"+"]");
		randomVariable.setSpecification(Double.toString(meanResourceDemand));
		parametricResourceDemand.setSpecification_ParametericResourceDemand(randomVariable);
	}

	private static void connectActions(AbstractAction startAction, AbstractAction stopAction) {
		if (startAction == null) {
			log.warn("StartAction == null");
		}
		if (stopAction == null) {
			log.warn("StopAction == null");
		}

		startAction.setSuccessor_AbstractAction(stopAction);
		stopAction.setPredecessor_AbstractAction(startAction);
	}

}

// private ParametricResourceDemand getParametricResourceDemand(final String
// resourceDemandType,
// final double resourceDemand, final double deviation)
// throws IOException {
// final ParametricResourceDemand parametricResourceDemand =
// SeffPerformanceFactory.eINSTANCE
// .createParametricResourceDemand();
//
// // PCM Random Variable for processingRate
// final PCMRandomVariable pcmRandomVariable = CoreFactory.eINSTANCE
// .createPCMRandomVariable();
//
// // load ResourceRepository
// final ResourceRepository resourceRepository;
// for (final ResourceType resourceType : resourceRepository
// .getAvailableResourceTypes_ResourceRepository()) {
// if (resourceType instanceof ProcessingResourceType) {
// final ProcessingResourceType processingResourceType =
// (ProcessingResourceType) resourceType;
// if (processingResourceType.getEntityName().equals(
// resourceDemandType)) {
// parametricResourceDemand
// .setRequiredResource_ParametricResourceDemand(processingResourceType);
//
// // TODO: Use Deviation
// // if deviation is zero just set the mean resource demand
// pcmRandomVariable.setSpecification(Double
// .toString(resourceDemand));
//
// // pcmRandomVariable.setSpecification("Norm ("
// // + resourceDemand + "," + deviation + ")");
//
// }
// }
// }
// parametricResourceDemand
// .setSpecification_ParametericResourceDemand(pcmRandomVariable);
// return parametricResourceDemand;
// }

// LoopAction loopAction = SeffFactory.eINSTANCE.createLoopAction();
// seff.getSteps_Behaviour().add(loopAction);
// loopAction.setIterationCount_LoopAction("0");

// StoexPackage.eINSTANCE.
// ProbabilityFunction function_ProbabilityFunctionLiteral = (new
// ProbabilityFunctionLiteral()).getFunction_ProbabilityFunctionLiteral();

// first markovState. In this case the first system action must be
// added as well.
// ExternalCallAction firstExternalCallAction = null;
// if (behaviorModel.getInitialState().getService().getName()
// .equals(seff.getDescribedService__SEFF().getEntityName())) {
// firstExternalCallAction = createExternalCallActionSystem(
// seff.getBasicComponent_ServiceEffectSpecification(),
// behaviorModel.getInitialState().getService().getName());
// }

// connect new nodes
// if (firstExternalCallAction != null && branchAction != null) {
// startAction
// .setSuccessor_AbstractAction(firstExternalCallAction);
// firstExternalCallAction
// .setPredecessor_AbstractAction(startAction);
// firstExternalCallAction
// .setSuccessor_AbstractAction(branchAction);
// branchAction
// .setPredecessor_AbstractAction(firstExternalCallAction);
// branchAction.setSuccessor_AbstractAction(stopAction);
// stopAction.setPredecessor_AbstractAction(branchAction);
// } else if (branchAction != null) {
// seff.getSteps_Behaviour().add(branchAction);
// startAction.setSuccessor_AbstractAction(branchAction);
// branchAction.setPredecessor_AbstractAction(startAction);
// branchAction.setSuccessor_AbstractAction(stopAction);
// stopAction.setPredecessor_AbstractAction(branchAction);
// } else {
// startAction.setSuccessor_AbstractAction(stopAction);
// stopAction.setPredecessor_AbstractAction(startAction);
// }
//
// } catch (final Exception e) {
// e.printStackTrace();
// }
