package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.repository.SystemModelRepository;

/**
 * An abstract base for messages which can later be used and combined in a  kieker.tools.traceAnalysis.systemModel.MessageTrace.
 * 
 * @author Patrick Treyer
 */
public abstract class AbstractMessage {

	private final long timestamp;
	private final Execution sendingExecution;
	private final Execution receivingExecution;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            The timestamp of the message.
	 * @param sendingExecution
	 *            The {@link Execution} object which sent the message.
	 * @param receivingExecution
	 *            The {@link Execution} object which received the message.
	 */
	public AbstractMessage(final long timestamp, final Execution sendingExecution, final Execution receivingExecution) {
		this.timestamp = timestamp;
		this.sendingExecution = sendingExecution;
		this.receivingExecution = receivingExecution;
	}

	/**
	 * Delivers the object which received the message.
	 * 
	 * @return The receiving object.
	 */
	public final Execution getReceivingExecution() {
		return this.receivingExecution;
	}

	/**
	 * Delivers the object which sent the message.
	 * 
	 * @return The sending object.
	 */
	public final Execution getSendingExecution() {
		return this.sendingExecution;
	}

	/**
	 * Delivers the timestamp at which the message was created.
	 * 
	 * @return The timestamp of the message.
	 */
	public final long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public String toString() {
		final StringBuilder strBuild = new StringBuilder();

		if (this instanceof SynchronousCallMessage) {
			strBuild.append("SYNC-CALL ");
		} else if (this instanceof SynchronousReplyMessage){
			strBuild.append("SYNC-RPLY ");
		} else if (this instanceof AsynchronousReplyMessage){
			strBuild.append("ASYNC-RPLY ");
		} else {
			strBuild.append("ASYNC-CALL ");
		}

		strBuild.append(this.timestamp);
		strBuild.append(' ');
		if (this.getSendingExecution().getOperation().getId() == Operation.ROOT_OPERATION_ID) {
			strBuild.append(SystemModelRepository.ROOT_NODE_LABEL);
		} else {
			strBuild.append(this.getSendingExecution());
		}
		strBuild.append(" --> ");
		if (this.getReceivingExecution().getOperation().getId() == Operation.ROOT_OPERATION_ID) {
			strBuild.append(SystemModelRepository.ROOT_NODE_LABEL);
		} else {
			strBuild.append(this.getReceivingExecution());
		}
		return strBuild.toString();
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public int hashCode() {
		return (int) (this.timestamp ^ (this.timestamp >>> 32));
	}
}
