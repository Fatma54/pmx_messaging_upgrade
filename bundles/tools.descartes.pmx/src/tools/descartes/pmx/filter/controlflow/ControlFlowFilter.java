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
package tools.descartes.pmx.filter.controlflow;

import java.util.HashMap;
import java.util.Map;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.controlflow.OperationExecutionRecord;
import kieker.common.util.signature.ClassOperationSignaturePair;

/**
 * Takes {@link OperationExecutionRecord} objects an creates PCM control flow
 *  
 * @author J�rgen Walter
 */
public class ControlFlowFilter extends AbstractFilterPlugin {
	/** The name of the input port accepting execution records. */
	public static final String INPUT_PORT_NAME_EVENTS = "monitoringRecordsExecution";
	public static final String OUTPUT_PORT_NAME_COMPONENT = "component";

	private final Map<Integer, String> map;	

	public ControlFlowFilter(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);
		map = new HashMap<Integer, String>();
	}

	@Override
	public Configuration getCurrentConfiguration() {
		OperationExecutionRecord oer;
		return null;
	}
	
	/**
	 * This method represents an input port for operation execution records.
	 * 
	 * @param record
	 *            The next record.
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Receives execution events to be selected by trace ID", eventTypes = { OperationExecutionRecord.class })
	public void inputOperationExecutionRecord(
			final OperationExecutionRecord record) {
		int sessionId;
		try{
			sessionId = Integer.parseInt(record.getSessionId());
		}catch(NumberFormatException ex){
			//<no-session-id>
			sessionId = 0;
		}
		
		String predecessor = map.get(sessionId);
		ClassOperationSignaturePair a = ClassOperationSignaturePair.splitOperationSignatureStr(record.getOperationSignature());
		String className = a.getSimpleClassname();
		String methodName = a.getSignature().getName();
		if(predecessor != null){
			log.info(predecessor +" ==> "+className+methodName);
		}	
		map.put(sessionId, className+methodName);
		
	}
}
