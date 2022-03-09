package org.palladiosimulator.pmxupgrade.pcm.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
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

import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.util.ModelCreationUtils;
import org.palladiosimulator.pmxupgrade.pcm.builder.util.cmbg.BehaviorModel;
import org.palladiosimulator.pmxupgrade.pcm.builder.util.cmbg.CMBG;

public class PCMUsageModelFactory2 {
	private static final Logger log = Logger.getLogger(PCMUsageModelFactory2.class);

	public static void createWorkload(HashMap<String, List<Double>> workloadMap, ModelBuilder builder, UsageModel usage,
			System system) {
		UsageScenario usageScenario = addUsageScenario(usage, "usageScenario");
		ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();

		HashMap<Double, String> map = new HashMap<Double, String>();
		double callsMin = Double.MAX_VALUE;
		double startMin = Double.MAX_VALUE;
		double endMax = Double.MIN_VALUE;
		for (String key : workloadMap.keySet()) {
			log.info(key + " " + workloadMap.get(key).size());
			if (workloadMap.get(key).size() > 0) {
				// consider method only if it has been called
				// more than 15 times
				double start = workloadMap.get(key).get(0);
				double calls = workloadMap.get(key).size();
				double end = workloadMap.get(key).get((int) calls - 1);
				callsMin = Math.min(callsMin, calls);
				startMin = Math.min(startMin, start);
				endMax = Math.max(endMax, end);
				log.info("==>" + calls + " " + start);
				map.put(start, key);
			}
		}
		double duration = (endMax - startMin) / 1000000000;
		log.info(" === durartion === " + duration);

		List<Double> list = new ArrayList<>(map.keySet());
		Collections.sort(list);
		log.info(" === message chain ===");
		// EntryLevelSystemCall current =
		AbstractUserAction current = UsagemodelFactory.eINSTANCE.createStart();
		current.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);

		for (double timeStamp : list) {
			log.info(map.get(timeStamp));
			String key = map.get(timeStamp);
			String methodName = key.split(ModelBuilder.seperatorChar)[0];
			String assemblyName = ModelBuilder.applyNameFixes(key.split(ModelBuilder.seperatorChar)[1]);
			String hostName = key.split(ModelBuilder.seperatorChar)[2];
			String interfaceName = "I" + assemblyName;
			if (builder.getInterface(interfaceName) != null) { //ignore for messaging ~fat

				OperationProvidedRole systemProvidedRole = (OperationProvidedRole) builder
						.getRole("Provided_" + interfaceName + ModelBuilder.seperatorChar + hostName);
				if (systemProvidedRole == null) {
					OperationInterface operationInterface = (OperationInterface) builder.getInterface(interfaceName);
					systemProvidedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
					systemProvidedRole
							.setEntityName("Provided_" + interfaceName + ModelBuilder.seperatorChar + hostName);
					system.getProvidedRoles_InterfaceProvidingEntity().add(systemProvidedRole);
					builder.addRole("Provided_" + interfaceName + ModelBuilder.seperatorChar + hostName,
							systemProvidedRole);

					systemProvidedRole
							.setEntityName("Provided_" + interfaceName + ModelBuilder.seperatorChar + hostName);
					systemProvidedRole.setProvidedInterface__OperationProvidedRole(operationInterface);
					system.getProvidedRoles_InterfaceProvidingEntity().add(systemProvidedRole);

					ProvidedDelegationConnector providedDelegationConnector = CompositionFactory.eINSTANCE
							.createProvidedDelegationConnector();
					providedDelegationConnector.setParentStructure__Connector(system);
					providedDelegationConnector.setEntityName("ProvDelegation " + interfaceName + " -> " + interfaceName
							+ ModelBuilder.seperatorChar + assemblyName);
					providedDelegationConnector.setAssemblyContext_ProvidedDelegationConnector((AssemblyContext) builder
							.getAssembly(assemblyName + ModelBuilder.seperatorChar + hostName));
					builder.addProvidedRole(assemblyName, interfaceName);
					providedDelegationConnector
							.setInnerProvidedRole_ProvidedDelegationConnector((OperationProvidedRole) builder
									.getRole("Provided_" + interfaceName + ModelBuilder.seperatorChar + assemblyName));
					providedDelegationConnector.setOuterProvidedRole_ProvidedDelegationConnector(systemProvidedRole);

				}
				double calls = workloadMap.get(key).size();
				log.info("NUMCALLS " + Math.round(calls / callsMin));
				for (int i = 0; i < Math.round(calls / callsMin); i++) {
					AbstractUserAction newCurrent;
					if (i >= 1) {
						newCurrent = addSystemUserCallAction(
								(OperationSignature) builder
										.getMethod(ModelCreationUtils.createMethodKey(methodName, assemblyName)),
								systemProvidedRole, scenarioBehaviour, "" + i);
					} else {
						newCurrent = addSystemUserCallAction(
								(OperationSignature) builder
										.getMethod(ModelCreationUtils.createMethodKey(methodName, assemblyName)),
								systemProvidedRole, scenarioBehaviour, "");
					}
					current.setSuccessor(newCurrent);
					newCurrent.setPredecessor(current);
					current = newCurrent;

				}

			}
			AbstractUserAction stop = UsagemodelFactory.eINSTANCE.createStop();
			stop.setScenarioBehaviour_AbstractUserAction(scenarioBehaviour);
			current.setSuccessor(stop);

			usageScenario.setScenarioBehaviour_UsageScenario(scenarioBehaviour);

			final ClosedWorkload workload = createClosedWorkload(10);
			// TODO should be replay file soon
			usageScenario.setWorkload_UsageScenario(workload);
		}
	}

	public static EntryLevelSystemCall addSystemUserCallAction(OperationSignature signature, OperationProvidedRole role,
			ScenarioBehaviour scenarioBehavior, String id) {
		EntryLevelSystemCall systemCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
		systemCall.setEntityName(signature.getEntityName());
		systemCall.setId(signature.getEntityName() + id);
		systemCall.setOperationSignature__EntryLevelSystemCall(signature);
		systemCall.setProvidedRole_EntryLevelSystemCall(role);
		scenarioBehavior.getActions_ScenarioBehaviour().add(systemCall);
		return systemCall;
	}

	public static UsageScenario addUsageScenario(UsageModel usageModel, String name) {
		final UsageScenario usageScenario = UsagemodelFactory.eINSTANCE.createUsageScenario();
		usageScenario.setEntityName(name);
		usageScenario.setId(name);
		usageModel.getUsageScenario_UsageModel().add(usageScenario);
		return usageScenario;
	}

	public static OpenWorkload createOpenWorkload(double interArrivalTime) {
		OpenWorkload wl = UsagemodelFactory.eINSTANCE.createOpenWorkload();
		PCMRandomVariable interArrival = CoreFactory.eINSTANCE.createPCMRandomVariable();
		interArrival.setSpecification("" + interArrivalTime);
		wl.setInterArrivalTime_OpenWorkload(interArrival);
		return wl;
	}

	public static ClosedWorkload createClosedWorkload(int population) {
		ClosedWorkload workload = UsagemodelFactory.eINSTANCE.createClosedWorkload();

		PCMRandomVariable pcmRandomVariableThinkTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		pcmRandomVariableThinkTime.setSpecification("0");
		workload.setThinkTime_ClosedWorkload(pcmRandomVariableThinkTime);
		workload.setPopulation(population);

		return workload;
	}

	private static ScenarioBehaviour createScenarioBehaviour(CMBG cmbg) {
		final ScenarioBehaviour scenarioBehaviour = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		scenarioBehaviour.setEntityName("DefaultScenario");

		// create start, stop and branch element
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		Branch branch = createBranch(cmbg);

		// add elements to ScenarioBehaviour
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
			createBranchTransition(branch, relativeFrequencies.get(i), behaviorModelsList.get(i));
		}
		return branch;
	}

	private static Branch createBranch() {
		Branch branch = UsagemodelFactory.eINSTANCE.createBranch();
		branch.setEntityName("BehaviorMix");
		BranchTransition branchTransition = UsagemodelFactory.eINSTANCE.createBranchTransition();
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
	private static void createBranchTransition(final Branch branch, final double probability,
			BehaviorModel behaviorModel) {

		BranchTransition branchTransition = UsagemodelFactory.eINSTANCE.createBranchTransition();

		ScenarioBehaviour sb = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();

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
	private static EntryLevelSystemCall createEntryLevelSystemCall(final BehaviorModel behaviorModel) {

		// create new EntryLevelSystemCall
		EntryLevelSystemCall entryLevelSystemCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();

		// get available Operations and call each workflow operation once
		final EList<ProvidedRole> providedRoles = null; // TODO add
														// providedRoles
		// creatorTools.getThisSystem().getProvidedRoles_InterfaceProvidingEntity();
		for (final ProvidedRole providedRole : providedRoles) {
			final OperationProvidedRole opr = (OperationProvidedRole) providedRole;
			if (opr.getEntityName().equals("Provided_" + behaviorModel.getName())) {

				final OperationInterface oi = opr.getProvidedInterface__OperationProvidedRole();
				final EList<OperationSignature> operationSignatures = oi.getSignatures__OperationInterface();

				for (OperationSignature operationSignature : operationSignatures) {

					if (behaviorModel.getInitialStateGetService().getName()
							.equals(operationSignature.getEntityName())) {
						entryLevelSystemCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
						entryLevelSystemCall.setEntityName(behaviorModel.getInitialStateGetService().getName());
						entryLevelSystemCall.setOperationSignature__EntryLevelSystemCall(operationSignature);
						entryLevelSystemCall.setProvidedRole_EntryLevelSystemCall(opr);
						break;
					}

				}
			}
		}
		return entryLevelSystemCall;
	}

}
