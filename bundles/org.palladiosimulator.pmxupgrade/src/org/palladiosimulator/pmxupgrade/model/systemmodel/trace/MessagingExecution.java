package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.DataInterface;

public class MessagingExecution extends Execution {
	
	private final String topic;
	private DataInterface dataInterface;

	public MessagingExecution(Operation op, AllocationComponent allocationComponent, String traceId, int eoi, int ess,
			long tin, long tout, boolean assumed, String childOf, String spanId, String topic, DataInterface dataInterface) {
		super(op, allocationComponent, traceId, eoi, ess, tin, tout, assumed, childOf, spanId);
		this.topic=topic;
		this.dataInterface = dataInterface;
	}
	
	public MessagingExecution(final Operation op, final AllocationComponent allocationComponent, final String traceId, String spanId, final String sessionId, String childOf, final int eoi, final int ess,
            final long tin, final long tout, final boolean assumed, String topic, DataInterface dataInterface) {
		super(op, allocationComponent, traceId, spanId, NO_SESSION_ID, childOf, eoi, ess, tin, tout, assumed);
		this.topic = topic;
		this.dataInterface = dataInterface;
	}
	public String getTopic() {
		return topic;
	}
	
	public DataInterface getDataInterface() {
		return this.dataInterface;
	}
	

	

}
