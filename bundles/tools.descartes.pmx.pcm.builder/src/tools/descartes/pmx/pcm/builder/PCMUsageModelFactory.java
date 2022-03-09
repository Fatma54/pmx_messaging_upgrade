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

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.OpenWorkload;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;
import org.palladiosimulator.pcm.usagemodel.Workload;

import tools.descartes.pmx.pcm.builder.util.cmbg.BehaviorModel;
import tools.descartes.pmx.pcm.builder.util.cmbg.CMBG;

public class PCMUsageModelFactory {

	
	public static final UsageScenario addUsageScenario(UsageModel usageModel){
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE
				.createUsageScenario();
		usageScenario.setEntityName("extractedBehavior");
		usageModel.getUsageScenario_UsageModel().add(usageScenario);
		return usageScenario;
	}

	public static final UsageScenario addUsageScenario(UsageModel usageModel, OperationSignature signature, OperationProvidedRole role){
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE
				.createUsageScenario();
		usageScenario.setEntityName("extractedBehavior_"+role.getEntityName());
		usageModel.getUsageScenario_UsageModel().add(usageScenario);

		final ScenarioBehaviour scenarioBehaviour = createSimpleScenarioBehavior(signature, role);
		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		
		final Workload workload = createClosedWorkload(1);
		workload.setUsageScenario_Workload(usageScenario);
		return usageScenario;
	}
	
	public static final UsageModel createUsageModel(CMBG cmbg) {
		final UsageModel usageModel = UsagemodelFactory.eINSTANCE
				.createUsageModel();
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE
				.createUsageScenario();
		final ScenarioBehaviour scenarioBehaviour = createScenarioBehavior(cmbg);
		final Workload workload;
		boolean isOpenWorkload = true;
		if (isOpenWorkload) {
			workload = createOpenWorkload();
		} else {
			workload = createClosedWorkload(cmbg.getPopulation());
		}
		usageScenario.setWorkload_UsageScenario(workload);

		usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);
		usageModel.getUsageScenario_UsageModel().add(usageScenario);

		return usageModel;
	}

	public static final OpenWorkload createOpenWorkload() {
		return UsagemodelFactory.eINSTANCE.createOpenWorkload();
	}

	public static final ClosedWorkload createClosedWorkload(int population) {
		ClosedWorkload workload = UsagemodelFactory.eINSTANCE
				.createClosedWorkload();

		PCMRandomVariable pcmRandomVariableThinkTime = CoreFactory.eINSTANCE
				.createPCMRandomVariable();
		pcmRandomVariableThinkTime.setSpecification("0");
		workload.setThinkTime_ClosedWorkload(pcmRandomVariableThinkTime);
		workload.setPopulation(population);

		return workload;
	}

	private static final ScenarioBehaviour createScenarioBehavior(CMBG cmbg) {
		final ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();
		scenarioBehaviour.setEntityName("DefaultScenario");

		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		Branch branch = createBranch(cmbg);

		// add elements to scenarioBehavior
		start.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stop.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		branch.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);

		// connect Elements
		start.setSuccessor(branch);
		branch.setPredecessor(start);
		branch.setSuccessor(stop);
		stop.setPredecessor(branch);
		return scenarioBehaviour;
	}
	
	public static final ScenarioBehaviour createSimpleScenarioBehavior(OperationSignature signature, OperationProvidedRole role) {
		final ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();
		scenarioBehaviour.setEntityName(signature.getEntityName()+"Scenario");

		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		
		EntryLevelSystemCall systemCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		String name = signature.getEntityName();//"main";
		systemCall.setEntityName(name);
		systemCall.setOperationSignature__EntryLevelSystemCall(signature);
		systemCall.setProvidedRole_EntryLevelSystemCall(role);

		// add elements to scenarioBehavior
		start.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		stop.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
		systemCall.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);

		// connect Elements
		start.setSuccessor(systemCall);
		systemCall.setPredecessor(start);
		systemCall.setSuccessor(stop);
		stop.setPredecessor(systemCall);
		return scenarioBehaviour;
	}

	/**
	 * Create new branch. Each Transition within this branch is a separate
	 * behaviorModel.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Branch createBranch(CMBG cmbg) {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName("BehaviorMix");
		// TODO
		List<BehaviorModel> behaviorModelsList = cmbg.getBehaviorModels();
		List<Double> relativeFrequencies = cmbg.getrelativeFrequencies();
		for (int i = 0; i < relativeFrequencies.size(); i++) {
			createBranchTransition(branch, relativeFrequencies.get(i),
					behaviorModelsList.get(i));
		}
		return branch;
	}

	private static Branch createBranch() {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName("BehaviorMix");
		BranchTransition branchTransition = UsagemodelFactory.eINSTANCE
				.createBranchTransition();
		branch.getBranchTransitions_Branch().add(branchTransition);
		return branch;
	}
	
	/**
	 * Create a new transition for each behaviorModel.
	 * 
	 * @param branch
	 * @param probability
	 * @param behaviorModel
	 * @throws IOException
	 */
	private static void createBranchTransition(final Branch branch,
			final double probability, BehaviorModel behaviorModel) {

		BranchTransition branchTransition = UsagemodelFactory.eINSTANCE
				.createBranchTransition();

		ScenarioBehaviour sb = UsagemodelFactory.eINSTANCE
				.createScenarioBehaviour();

		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();

		// add to scenario
		sb.getActions_ScenarioBehaviour().add(start);
		sb.getActions_ScenarioBehaviour().add(stop);

		EntryLevelSystemCall entryLevelSystemCall = createEntryLevelSystemCall(behaviorModel);
		entryLevelSystemCall.setScenarioBehaviour_AbstractUserAction(sb);

		// connect element
		start.setSuccessor(entryLevelSystemCall);
		entryLevelSystemCall.setPredecessor(start);
		entryLevelSystemCall.setSuccessor(stop);
		stop.setPredecessor(entryLevelSystemCall);

		// add transition and scenario to branch
		branchTransition.setBranchedBehaviour_BranchTransition(sb);
		branchTransition.setBranch_BranchTransition(branch);
		branchTransition.setBranchProbability(probability);
	}

	/**
	 * Create a EntryLevelSystemCall to the initial state of the behaviorModel
	 * component in the repository model.
	 * 
	 * @param behaviorModel
	 * @return
	 * @throws IOException
	 */
	private static EntryLevelSystemCall createEntryLevelSystemCall(
			final BehaviorModel behaviorModel) {

		// create new EntryLevelSystemCall
		EntryLevelSystemCall entryLevelSystemCall = UsagemodelFactory.eINSTANCE
				.createEntryLevelSystemCall();

		// get available Operations and call each workflow operation once
		final EList<ProvidedRole> providedRoles = null; // TODO add
														// providedRoles
		// creatorTools.getThisSystem().getProvidedRoles_InterfaceProvidingEntity();
		for (final ProvidedRole providedRole : providedRoles) {
			final OperationProvidedRole opr = (OperationProvidedRole) providedRole;
			if (opr.getEntityName().equals(
					"Provided_" + behaviorModel.getName())) {

				final OperationInterface oi = opr
						.getProvidedInterface__OperationProvidedRole();
				final EList<OperationSignature> operationSignatures = oi
						.getSignatures__OperationInterface();

				for (OperationSignature operationSignature : operationSignatures) {

					if (behaviorModel.getInitialStateGetService().getName()
							.equals(operationSignature.getEntityName())) {
						entryLevelSystemCall = UsagemodelFactory.eINSTANCE
								.createEntryLevelSystemCall();
						entryLevelSystemCall.setEntityName(behaviorModel
								.getInitialStateGetService().getName());
						entryLevelSystemCall
								.setOperationSignature__EntryLevelSystemCall(operationSignature);
						entryLevelSystemCall
								.setProvidedRole_EntryLevelSystemCall(opr);
						break;
					}

				}
			}
		}
		return entryLevelSystemCall;
	}

}
