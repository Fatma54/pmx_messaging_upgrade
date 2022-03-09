package org.palladiosimulator.pmxupgrade.logic.dataprocessing;

import org.palladiosimulator.pmxupgrade.logic.PMXController;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.ControlFlowService;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.AbstractDependencyGraph;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.DependencyGraphNode;
import org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph.WeightedBidirectionalDependencyGraphEdge;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.MessageBasedTraceReconstructionService;
import org.palladiosimulator.pmxupgrade.logic.tracereconstruction.opentracing.TraceReconstructionService;
import org.palladiosimulator.pmxupgrade.model.exception.InvalidTraceException;
import org.palladiosimulator.pmxupgrade.model.graph.AbstractGraph;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.exception.PMXException;
import org.palladiosimulator.pmxupgrade.model.inputreader.ProcessingObjectWrapper;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AssemblyComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.TraceInformation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ControlFlowTest {

    @Test
    void filter() throws PMXException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/combination4.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        TraceReconstructionService traceReconstructionService = new TraceReconstructionService();

        ProcessingObjectWrapper processingObjectWrapper = traceReconstructionService.reconstructTrace(configuration, pmxController.getTraceRecord());

        ControlFlowService service = new ControlFlowService();

        AbstractDependencyGraph<AllocationComponentOperationPair> graph = service.resolveControlFlow(processingObjectWrapper.getExecutionTraces(), processingObjectWrapper.getSystemModelRepository());

        doWork(graph, processingObjectWrapper.getSystemModelRepository());

        System.out.println("finish");

    }

    private void doWork(
            final AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> graph,
            SystemModelRepository systemModelRepository) {
        Writer fw = null;

        String outputFn = "target/output.txt";


        try {
//			log.info("Logging graph to " + outputFn);
            fw = new FileWriter(outputFn);
            fw.append("AllocationComponentInstances ");
            for (AllocationComponent ac : systemModelRepository
                    .getAllocationFactory().getAllocationComponentInstances()) {
                fw.append("\n ").append(ac.getIdentifier()).append(" ").append("(assembly: ").append(ac.getAssemblyComponent().getIdentifier()).append(") ").append("(execContainer: ").append(ac.getExecutionContainer().getIdentifier()).append(")");

            }
            fw.append("\n").append("AssemblyComponentInstances ");
            for (AssemblyComponent ac : systemModelRepository
                    .getAssemblyFactory().getAssemblyComponentInstances()) {
                fw.append("\n ").append(ac.getIdentifier()); // + " "					+ ac.getName());

            }
            fw.append("\n");

            for (DependencyGraphNode<AllocationComponentOperationPair> node : graph
                    .getVertices()) {

                //AllocationComponent allocationComponent = node.getEntity();
                fw.append("Node ").append(String.valueOf(node.getId())).append(" :: ").append(String.valueOf(node.getEntity().getOperation())).append("\t").append("\n");
                for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outgoingEdge : node
                        .getOutgoingEdges()) {

                    String start = node.getEntity().getAllocationComponent().getAssemblyComponent().getType().getTypeName();
                    String end = outgoingEdge.getTarget().getEntity().getAllocationComponent().getAssemblyComponent().getType().getTypeName();

                    fw.append("\t").append(String.valueOf(outgoingEdge.getTarget().getId())).append(": ").append("(weight=").append(String.valueOf(outgoingEdge.getTargetWeight())).append(")");
                }
                fw.append("\n");
            }
        } catch (IOException e) {
            System.out.println("Could not write file " + outputFn);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    
    //~fat
    @Test
    void filterFL() throws PMXException, InvalidTraceException {
        Configuration configuration = new Configuration();
        configuration.setInputFileName("resources/json/shortenedTraces.json");
        configuration.setOutputDirectory("/test");

        PMXController pmxController = new PMXController(configuration);
        pmxController.readTracingData();

        MessageBasedTraceReconstructionService traceReconstructionService = new MessageBasedTraceReconstructionService();

        ProcessingObjectWrapper processingObjectWrapper = traceReconstructionService.reconstructTrace(configuration, pmxController.getTraceRecord());

        ControlFlowService service = new ControlFlowService();

        AbstractDependencyGraph<AllocationComponentOperationPair> graph = service.resolveControlFlow(processingObjectWrapper.getExecutionTraces(), processingObjectWrapper.getSystemModelRepository());

        doWorkFL(graph, processingObjectWrapper.getSystemModelRepository());

        System.out.println("finish");

    }

    private void doWorkFL(
            final AbstractGraph<DependencyGraphNode<AllocationComponentOperationPair>, WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>, TraceInformation> graph,
            SystemModelRepository systemModelRepository) {
        Writer fw = null;

        String outputFn = "target/outputFL.txt";


        try {
//			log.info("Logging graph to " + outputFn);
            fw = new FileWriter(outputFn);
            fw.append("AllocationComponentInstances ");
            for (AllocationComponent ac : systemModelRepository
                    .getAllocationFactory().getAllocationComponentInstances()) {
                fw.append("\n ").append(ac.getIdentifier()).append(" ").append("(assembly: ").append(ac.getAssemblyComponent().getIdentifier()).append(") ").append("(execContainer: ").append(ac.getExecutionContainer().getIdentifier()).append(")");

            }
            fw.append("\n").append("AssemblyComponentInstances ");
            for (AssemblyComponent ac : systemModelRepository
                    .getAssemblyFactory().getAssemblyComponentInstances()) {
                fw.append("\n ").append(ac.getIdentifier()); // + " "					+ ac.getName());

            }
            fw.append("\n");

            for (DependencyGraphNode<AllocationComponentOperationPair> node : graph
                    .getVertices()) {

                //AllocationComponent allocationComponent = node.getEntity();
                fw.append("Node ").append(String.valueOf(node.getId())).append(" :: ").append(String.valueOf(node.getEntity().getOperation())).append("\t").append("\n");
                for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outgoingEdge : node
                        .getOutgoingEdges()) {

                    String start = node.getEntity().getAllocationComponent().getAssemblyComponent().getType().getTypeName();
                    String end = outgoingEdge.getTarget().getEntity().getAllocationComponent().getAssemblyComponent().getType().getTypeName();

                    fw.append("\t").append(String.valueOf(outgoingEdge.getTarget().getId())).append(": ").append("(weight=").append(String.valueOf(outgoingEdge.getTargetWeight())).append(")");
                }
                fw.append("\n");
            }
        } catch (IOException e) {
            System.out.println("Could not write file " + outputFn);
        } finally {
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
