package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

/**
 * A specified class of messages which represent synchronous replies.
 */
public class SynchronousReplyMessage extends AbstractMessage {

    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param timestamp          The timestamp of the message.
     * @param sendingExecution   The {@link Execution} object which sent the message.
     * @param receivingExecution The {@link Execution} object which received the message.
     */
    public SynchronousReplyMessage(final long timestamp, final Execution sendingExecution, final Execution receivingExecution) {
        super(timestamp, sendingExecution, receivingExecution);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SynchronousReplyMessage)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final SynchronousReplyMessage other = (SynchronousReplyMessage) obj;

        return (this.getTimestamp() == other.getTimestamp()) && this.getSendingExecution().equals(other.getSendingExecution())
                && this.getReceivingExecution().equals(other.getReceivingExecution());
    }

    @Override
    public int hashCode() { // NOPMD (forward hashcode)
        return super.hashCode();
    }
}
