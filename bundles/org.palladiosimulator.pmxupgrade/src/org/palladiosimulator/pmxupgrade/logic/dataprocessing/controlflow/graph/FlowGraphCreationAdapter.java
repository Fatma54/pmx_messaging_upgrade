package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.AllocationComponentOperationPairFactory;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.OperationRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AsynchronousReplyMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessageTrace;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Operation;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.SynchronousCallMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.SynchronousReplyMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.util.AllocationComponentOperationPair;

/**
 * Adapter for executing the extraction of the control flow statistics and therefore the creation of an operation callgraph.
 *
 * @author Patrick Treyer
 */
public class FlowGraphCreationAdapter extends AbstractDependencyGraphFilter<AllocationComponentOperationPair> {

    /**
     * Creates a new dependency graph using the given data.
     *
     * @param graph The graph to produce / extend
     */
    public FlowGraphCreationAdapter(AbstractDependencyGraph<AllocationComponentOperationPair> graph) {
        super(graph);
    }

    public void inputMessageTraces(final MessageTrace t, SystemModelRepository systemModelRepository) {
        for (final AbstractMessage m : t.getSequenceAsVector()) {
        	//SynchronousReplyMessage indicated that the calleee has terminated and returned the result. added for understanding
            if (m instanceof SynchronousReplyMessage || m instanceof AsynchronousReplyMessage) {
                continue;
            }
            if (m instanceof SynchronousCallMessage) {
            	
            }
            final AllocationComponent senderComponent = m.getSendingExecution().getAllocationComponent();
            final AllocationComponent receiverComponent = m.getReceivingExecution().getAllocationComponent();
            final int rootOperationId = OperationRepository.ROOT_OPERATION.getId();
            final Operation senderOperation = m.getSendingExecution().getOperation();
            final Operation receiverOperation = m.getReceivingExecution().getOperation();

            // The following two get-calls to the factory return s.th. in either case
            final AllocationComponentOperationPairFactory pairFactory = systemModelRepository.getAllocationPairFactory();

            AllocationComponentOperationPair senderPair;
            if (senderOperation.getId() == rootOperationId) {
                senderPair = this.getGraph().getRootNode().getEntity();
            } else {
                senderPair = pairFactory.getPairInstanceByPair(senderComponent, senderOperation);
            }

            AllocationComponentOperationPair receiverPair;
            if (receiverOperation.getId() == rootOperationId) {
                receiverPair = this.getGraph().getRootNode().getEntity();
            } else {
                receiverPair = pairFactory.getPairInstanceByPair(receiverComponent, receiverOperation);
            }

            DependencyGraphNode<AllocationComponentOperationPair> senderNode = this.getGraph().getNode(senderPair
                    .getId());
            DependencyGraphNode<AllocationComponentOperationPair> receiverNode = this.getGraph().getNode(receiverPair.getId());
            if (senderNode == null) {
                senderNode = new DependencyGraphNode<>(senderPair.getId(), senderPair, t.getTraceInformation(),
                        this.getOriginRetentionPolicy());

                if (m.getSendingExecution().isAssumed()) {
                    senderNode.setAssumed();
                }

                this.getGraph().addNode(senderNode.getId(), senderNode);
            } else {
                this.handleOrigin(senderNode, t.getTraceInformation());
            }

            if (receiverNode == null) {
                receiverNode = new DependencyGraphNode<>(receiverPair.getId(), receiverPair, t.getTraceInformation(),
                        this.getOriginRetentionPolicy());

                if (m.getReceivingExecution().isAssumed()) {
                    receiverNode.setAssumed();
                }

                this.getGraph().addNode(receiverNode.getId(), receiverNode);
            } else {
                this.handleOrigin(receiverNode, t.getTraceInformation());
            }

            final boolean assumed = this.isDependencyAssumed(senderNode, receiverNode);

            senderNode.addOutgoingDependency(receiverNode, assumed, t.getTraceInformation(), this.getOriginRetentionPolicy());
            receiverNode.addIncomingDependency(senderNode, assumed, t.getTraceInformation(), this.getOriginRetentionPolicy());

            this.invokeDecorators(m, senderNode, receiverNode);
        }
    }

}
