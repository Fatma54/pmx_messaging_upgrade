package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

public class AsynchronousCallMessage extends AbstractMessage {

	public AsynchronousCallMessage(long timestamp, Execution sendingExecution, Execution receivingExecution) {
		super(timestamp, sendingExecution, receivingExecution);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AsynchronousCallMessage)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final AsynchronousCallMessage other = (AsynchronousCallMessage) obj;

        return (this.getTimestamp() == other.getTimestamp()) && this.getSendingExecution().equals(other.getSendingExecution())
                && this.getReceivingExecution().equals(other.getReceivingExecution());
	}

}
