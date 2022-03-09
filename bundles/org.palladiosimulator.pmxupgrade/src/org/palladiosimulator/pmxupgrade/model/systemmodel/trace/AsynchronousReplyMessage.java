package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

public class AsynchronousReplyMessage extends AbstractMessage {

	public AsynchronousReplyMessage(long timestamp, Execution sendingExecution, Execution receivingExecution) {
		super(timestamp, sendingExecution, receivingExecution);
	}

	@Override
	public boolean equals(Object obj) {
		 if (!(obj instanceof AsynchronousReplyMessage)) {
	            return false;
	        }
	        if (this == obj) {
	            return true;
	        }
	        final AsynchronousReplyMessage other = (AsynchronousReplyMessage) obj;

	        return (this.getTimestamp() == other.getTimestamp()) && this.getSendingExecution().equals(other.getSendingExecution())
	                && this.getReceivingExecution().equals(other.getReceivingExecution());
	}

}
