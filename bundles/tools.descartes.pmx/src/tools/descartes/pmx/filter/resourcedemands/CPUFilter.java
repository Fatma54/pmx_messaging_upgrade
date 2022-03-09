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
package tools.descartes.pmx.filter.resourcedemands;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.system.CPUUtilizationRecord;

@Plugin(name = "CPU utilization filter", description = "Extracts CPU-utilization-information from incoming monitoring records", outputPorts = {
		@OutputPort(name = CPUFilter.OUTPUT_PORT_NAME_UTILIZATION, description = "Outputs CPUUtilRecords", eventTypes = { IMonitoringRecord.class }),
		@OutputPort(name = CPUFilter.OUTPUT_PORT_NAME_OTHER, description = "Outputs untouched information", eventTypes = { IMonitoringRecord.class }) })
public class CPUFilter extends AbstractFilterPlugin {

	public static final String INPUT_PORT_NAME = "newMonitoringRecord";
	public static final String OUTPUT_PORT_NAME_UTILIZATION = "cpu";
	public static final String OUTPUT_PORT_NAME_OTHER = "untouchedIMonitoringRecords";

	public CPUFilter(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@InputPort(name = CPUFilter.INPUT_PORT_NAME, description = "Extract resource-information from monitoring record", eventTypes = { IMonitoringRecord.class })
	public void runFilter(final Object record) {
		if (record instanceof CPUUtilizationRecord) {
			//log.info("record "+record);
			//final CPUUtilizationRecord cpuUtilizationRecord = (CPUUtilizationRecord) record;
			super.deliver(OUTPUT_PORT_NAME_UTILIZATION, (CPUUtilizationRecord) record);
		} else {
			super.deliver(OUTPUT_PORT_NAME_OTHER, record);
		}
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}
