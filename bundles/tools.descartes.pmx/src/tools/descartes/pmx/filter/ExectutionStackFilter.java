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

import org.apache.log4j.Logger;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
//""
//@Plugin(name = "TraceEventRecords2ExecutionAndMessageTraceFilter", description = "...", outputPorts = { 
		@Plugin(name = "ExecutionStackFilter", description = "...", outputPorts = { 
		@OutputPort(name = TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE,
				description = "Outputs transformed message traces", eventTypes = { MessageTrace.class }), })
public class ExectutionStackFilter extends AbstractTraceAnalysisFilter {
			//AbstractMessageTraceProcessingFilter{
//AbstractFilterPlugin{
//TraceEventRecords2ExecutionAndMessageTraceFilter {
//extends AbstractTraceProcessingFilter
	public static final String INPUT_PORT_NAME_MESSAGE_TRACE = "messageTrace";
	private static final Logger log = Logger.getLogger(ExectutionStackFilter.class);

	public ExectutionStackFilter(Configuration configuration,
			IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@InputPort(name = ExectutionStackFilter.INPUT_PORT_NAME_MESSAGE_TRACE, description = "Extract class-information from monitoring record", eventTypes = { MessageTrace.class })
	public void runFilter(final MessageTrace trace) {
//		List<AbstractMessage> reducedMessageList = new ArrayList<AbstractMessage>();
//		for(AbstractMessage message:trace.getSequenceAsVector()){
//			log.info(""+message.getReceivingExecution().getEss());
//			if(message.getReceivingExecution().getEss() < 2){
//				reducedMessageList.add(message);				
//			}
//		}
//		super.deliver(TraceEventRecords2ExecutionAndMessageTraceFilter.OUTPUT_PORT_NAME_MESSAGE_TRACE, new MessageTrace(trace.getTraceId(), reducedMessageList));
		log.info("DDDD ");
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return this.configuration;
	}
	
	
	@Override
	public void terminate(boolean error) {
		log.info("Terminated");
		super.terminate(error);
	}

}
