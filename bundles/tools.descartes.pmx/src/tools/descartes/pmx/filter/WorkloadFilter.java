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
package tools.descartes.pmx.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kieker.tools.traceAnalysis.systemModel.AbstractMessage;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import tools.descartes.pmx.builder.ModelBuilder;

@Plugin(name = "WorkloadFilter", description = "...", outputPorts = { @OutputPort(name = WorkloadFilter.OUTPUT_PORT_NAME_WORKLOAD, description = "Outputs workload description", eventTypes = { HashMap.class }), })
public class WorkloadFilter extends AbstractMessageTraceProcessingFilter {

	public static final String INPUT_PORT_NAME_MESSAGE_TRACE = "messageTrace";
	public static final String OUTPUT_PORT_NAME_WORKLOAD = "workload";

	private static final Logger log = Logger.getLogger(WorkloadFilter.class);
	private static HashMap<String, List<Double>> workloadTimeSeriesMap;

	public WorkloadFilter(Configuration configuration,
			IProjectContext projectContext) {
		super(configuration, projectContext);
		workloadTimeSeriesMap = new HashMap<String, List<Double>>();
	}

	@Override
	@InputPort(name = AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES, description = "Receives the message traces to be processed",
			eventTypes = { MessageTrace.class })
	public void inputMessageTraces(final MessageTrace mt) {
		AbstractMessage startMessage = getStartMessage(mt);// mt.getSequenceAsVector().get(0);
		Execution x = startMessage.getReceivingExecution();
		String host = x.getAllocationComponent().getExecutionContainer().getName();
		String function = x.getOperation().getSignature().getName();
		String component = x.getAllocationComponent().getAssemblyComponent().getType().getTypeName();
		addTimeStamp(host, component, function, x.getTin());
	}
	
	private AbstractMessage getStartMessage(final MessageTrace mt) {
		for (AbstractMessage message : mt.getSequenceAsVector()) {
			// System.out.println("\t" +
			// message.getReceivingExecution().getOperation().getSignature().getName()
			// + " "
			// + message.getTimestamp() + " " + mt.getStartTimestamp());
			if (message.getTimestamp() == mt.getStartTimestamp()) {
				return message;
			}
		}
		return mt.getSequenceAsVector().get(0);
	}

	private void sortMessageTrace(final MessageTrace mt) {
		List<AbstractMessage> messages = mt.getSequenceAsVector();
		Comparator<AbstractMessage> timeComparator = new Comparator<AbstractMessage>() {
			@Override
			public int compare(AbstractMessage arg0, AbstractMessage arg1) {
				if (arg0.getTimestamp() > arg1.getTimestamp()) {
					return 1;
				} else if (arg0.getTimestamp() < arg1.getTimestamp()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Collections.sort(messages, timeComparator);
		// for (AbstractMessage message : messages) {
		// System.out.println(
		// "" + message.getSendingExecution().getOperation() + " " +
		// message.getSendingExecution().getEoi());
		// }
	}

	private synchronized void addTimeStamp(String host, String component, String function, double timestamp){
		List<Double> timeSeries;
		String key = function+ModelBuilder.seperatorChar+component+ModelBuilder.seperatorChar+host;
		if (!workloadTimeSeriesMap.containsKey(key)) {
			timeSeries = new ArrayList<>();
			timeSeries.add(timestamp);
			workloadTimeSeriesMap.put(key, timeSeries);
		} else {
			timeSeries = workloadTimeSeriesMap.get(key);
			timeSeries.add(timestamp);
		}
	}


	@Override
	public void terminate(boolean errorBeforeTermination) {
		//log.info("workloadddd "+workloadTimeSeriesMap);
		if(workloadTimeSeriesMap.isEmpty()){
			log.error("Could not extract incoming requests to create workload model");
		}
		super.deliver(WorkloadFilter.OUTPUT_PORT_NAME_WORKLOAD, workloadTimeSeriesMap);
		super.terminate(errorBeforeTermination);
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return this.configuration;
	}

}
