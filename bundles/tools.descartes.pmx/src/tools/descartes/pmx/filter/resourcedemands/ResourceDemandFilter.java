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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.system.CPUUtilizationRecord;
import kieker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kieker.tools.traceAnalysis.systemModel.AbstractMessage;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import tools.descartes.librede.LibredeResults;
import tools.descartes.librede.ResultTable;
import tools.descartes.librede.approach.IEstimationApproach;
import tools.descartes.librede.approach.ResponseTimeApproximationApproach;
import tools.descartes.librede.approach.ServiceDemandLawApproach;
import tools.descartes.librede.linalg.LinAlg;
import tools.descartes.librede.linalg.Matrix;
import tools.descartes.librede.linalg.Vector;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Time;
import tools.descartes.pmx.builder.ModelBuilder;
import tools.descartes.pmx.filter.resourcedemands.adapter.LibReDEAdapter;

@Plugin(name = "ResourceDemandFilter", 
description = "Subtracts times for external calls from total time for resource demand estimation.",
outputPorts = { 
				@OutputPort(name = ResourceDemandFilter.OUTPUT_PORT_NAME_DEMANDS,
				description = "Outputs resource demands", eventTypes = { HashMap.class })
})
public class ResourceDemandFilter extends AbstractMessageTraceProcessingFilter {
	public static final String CONFIG_PROPERTY_NAME_OUTPUT_FN = "outputDirectory";
	public static final String OUTPUT_PORT_NAME_DEMANDS = "resourceDemands";
	private static Map<String, TimeSeries> serviceTimeSeriesMap;
	private static Map<String, TimeSeries> resourceTimeSeriesMap;
	private static TimeSeries networkTimeSeries;
	private static Set<String> hosts = new HashSet<String>();
	private final String outputPath;
	private HashMap<String, Integer> numCores = new HashMap<String, Integer>();

	public void addCPUCoreNumber(String host, Integer number) {
		if (numCores == null) {
			numCores = new HashMap<String, Integer>();
		}
		numCores.put(host, number);
	}
	
	public static final String INPUT_PORT_NAME_MESSAGE_TRACE = "messageTrace";
	public static final String INPUT_PORT_NAME_EXECUTION_TRACES = "executionTraces";
	public static final String INPUT_PORT_NAME_UTILIZATION = "cpu";
	private static final Logger log = Logger
			.getLogger(ResourceDemandFilter.class);
	
	public ResourceDemandFilter(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.outputPath = configuration
				.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_FN);
		serviceTimeSeriesMap = new HashMap<String, TimeSeries>();
		resourceTimeSeriesMap = new HashMap<String, TimeSeries>();
	}


	@InputPort(name = ResourceDemandFilter.INPUT_PORT_NAME_UTILIZATION, description = "...", eventTypes = { CPUUtilizationRecord.class })
	public void inputUtilizationLogs(final CPUUtilizationRecord record) {
		//TODO Check it is loggingTimestamp or timestamp
		addResourceLog(Time.NANOSECONDS.convertTo(record.getLoggingTimestamp(), Time.SECONDS), record.getHostname(), "CPU", record.getTotalUtilization());
	}

	@Override
	@InputPort(name = AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES, description = "Receives the message traces to be processed",
			eventTypes = { MessageTrace.class })
	public void inputMessageTraces(final MessageTrace mt) {
		Map<Execution, Double> externalCallTime = new HashMap<Execution, Double>();
		Map<Execution, List<Execution>> externalCallMethods = new HashMap<Execution, List<Execution>>();

		List<AbstractMessage> messages = mt.getSequenceAsVector();
		for (AbstractMessage message : messages) {
			Execution sender = message.getSendingExecution();
			Execution receiver = message.getReceivingExecution();
			if (sender.getEss() < receiver.getEss()) {
				if (!externalCallTime.containsKey(sender)) {
					externalCallTime.put(sender, (double) 0);
				}
				if (!externalCallTime.containsKey(receiver)) {
					externalCallTime.put(receiver, (double) 0);
				}
				// Time lost at linking resources
				if (!sender.getAllocationComponent()
						.getExecutionContainer()
						.equals(receiver.getAllocationComponent()
								.getExecutionContainer())) {
					if(!sender.getAllocationComponent().getAssemblyComponent().getName().equals("'Entry'")){
						// sender != receiver 
						//log.info("Network ("+sender.getAllocationComponent().getExecutionContainer().getName()+ " == >" + receiver.getAllocationComponent().getExecutionContainer().getName()+"): "
						//	+ (sender.getTout() - receiver.getTin())
 						//);
					double networkDelay = (receiver.getTin() - sender.getTout());
					double timestamp = sender.getTout();
					addNetworkLog(timestamp, networkDelay);
					}
				}

				double externalTime = (receiver.getTout() - receiver.getTin()); 
				externalCallTime.put(sender, externalCallTime.get(sender) + externalTime);

				List<Execution> list = externalCallMethods.get(sender);
				list = (list == null) ? new ArrayList<Execution>() : list;
				list.add(receiver);
				externalCallMethods.put(sender, list);
			}
		}

		for (Execution execution : externalCallTime.keySet()) {
			if (execution.getAllocationComponent().getAssemblyComponent().getType().getTypeName().contains("Entry")) {
				continue;
			}

			double time = (execution.getTout() - execution.getTin());
			double externalTime = externalCallTime.get(execution);
			if (time < 0) {
				log.error("time < 0: time = " + time);
			}

			//if(true){
			if(externalTime - time > 0.001 * time) {		// measurement >10% uncertain 
				//TODO External calls
				boolean error = true;
				AllocationComponent ac = execution.getAllocationComponent();
				for(Execution sub: externalCallMethods.get(execution)){
					if(!ac.equals(sub.getAllocationComponent())){
						error = false;
					};
				}
				
				if(error){
					log.error("time < external time (trace id "+execution.getTraceId()+") "+execution.getAllocationComponent().getAssemblyComponent().getType().getTypeName() + " "+execution.getOperation().getSignature().getName());
					log.error("\t"+ "time = " + time  );
					log.error("\t"+ "exte = " + externalTime);
					if(externalCallMethods.get(execution) != null){
						for(Execution sub : externalCallMethods.get(execution)){
							log.error("\t"+"       "+(sub.getTout() -sub.getTin()) +" << "+sub.getAllocationComponent().getAssemblyComponent().getType().getTypeName() + " "+ sub.getOperation().getSignature().getName() + "(trace id "+sub.getTraceId()+") ");
						}
					}
				}else{
					log.warn(execution.getAllocationComponent().getAssemblyComponent().getType().getTypeName() + " "
							+ execution.getOperation().getSignature().getName()
							+ " has been abortet before external call response (trace id " + execution.getTraceId()
							+ ")");
				}
				externalTime = 0.0;
			}

			// Data connection to superclass
			addExecutionLog(Time.NANOSECONDS.convertTo(execution.getTin(), Time.SECONDS),
					execution.getAllocationComponent().getAssemblyComponent()
					.getType().getTypeName()
					+ ModelBuilder.seperatorChar + execution.getOperation().getSignature().getName(), execution.getAllocationComponent()
							.getExecutionContainer().getName(),
					(time - externalTime));
		}
		// //Aufl�sung der Zeimessung < 1ms
		// // Michael Kupperberg aufl�sung von Timern
	}	

	public static void addNetworkLog(double timestamp, double delay) {
		if(networkTimeSeries== null){
			double[] timeValue = new double[1];
			timeValue[0] = timestamp;
			Vector time = LinAlg.vector(timeValue);

			double[] values = new double[1];
			values[0] = delay;
			Matrix data = LinAlg.matrix(values);
			networkTimeSeries = new TimeSeries(time, data);
		}else{
			networkTimeSeries = networkTimeSeries.addSample(timestamp, delay);
		}
	}
	
	public static synchronized void addResourceLog(double timestamp, String host, String resource, double utilization){
		TimeSeries timeSeries;
		String key = resource+"_"+host;
		if (!resourceTimeSeriesMap.containsKey(key)) {
			double[] timeValue = new double[1];
			timeValue[0] = timestamp;
			Vector time = LinAlg.vector(timeValue);

			double[] values = new double[1];
			values[0] = utilization;
			Matrix data = LinAlg.matrix(values);
			timeSeries = new TimeSeries(time, data);
			resourceTimeSeriesMap.put(key, timeSeries);
		} else {
			timeSeries = resourceTimeSeriesMap.get(key);
			timeSeries = timeSeries.addSample(timestamp, utilization);
			resourceTimeSeriesMap.put(key, timeSeries);
		}
		
	}
	
	public static synchronized void addExecutionLog(double timestamp,
			String interfaceName, String host, double exTime) {
		TimeSeries timeSeries = null;
		String key = interfaceName+ModelBuilder.seperatorChar+host;
		if(exTime < 0 ){
			//log.error("negative execution time. " +host+" "+interfaceName + ": "+exTime);
			//exTime = Math.abs(exTime);
			exTime = 0;
		}
				
		hosts.add(host);
		if (!serviceTimeSeriesMap.containsKey(key)) {
			double[] timeValue = new double[1];
			timeValue[0] = timestamp;
			Vector time = LinAlg.vector(timeValue);

			double[] values = new double[1];
			values[0] = exTime;
			Matrix data = LinAlg.matrix(values);
			timeSeries = new TimeSeries(time, data);
			serviceTimeSeriesMap.put(key, timeSeries);
		} else {
			timeSeries = serviceTimeSeriesMap.get(key);
			timeSeries = timeSeries.addSample(timestamp, exTime);
			serviceTimeSeriesMap.put(key, timeSeries);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate(final boolean errorBeforeTermination) {
		super.terminate(errorBeforeTermination);
		if(serviceTimeSeriesMap.keySet().isEmpty()){
			log.error("could not extract service times to estimate resource demands");	//No service execution logs could be found
			return;
		}
	
		HashMap<String, Double> resourceDemandMap = new HashMap<String, Double>();
		// TODO Librede logging in separate file
		for (String host : hosts) {
			StringBuffer sb = new StringBuffer();
			for (String service : serviceTimeSeriesMap.keySet()) {
				if (service.endsWith(host)) {
					sb.append(service.replace(ModelBuilder.seperatorChar + host, "") + " | ");
				}
			}
			log.info("Estimate resource demands on [" + host + "]");
			log.info("\tservices: |" + sb.toString());

			/** Run LibReDE */
			try{
				Integer cores = null;
				if (numCores.containsKey(host)) {
					cores = numCores.get(host);
				}
				if (cores == null) {
					log.warn("no value passed for numer of cores at " + host);
					log.info("did set number of cores to " + 2);
					cores = 2;
				}

				LibredeResults estimates = LibReDEAdapter.initAndRunLibrede(host, serviceTimeSeriesMap,
						resourceTimeSeriesMap, outputPath, cores);
	
				Set<Class<? extends IEstimationApproach>> approaches = estimates.getApproaches();
				Class<? extends IEstimationApproach> approach;
				if (approaches.contains(ServiceDemandLawApproach.class)) {
					approach = ServiceDemandLawApproach.class;
				} else {
					approach = ResponseTimeApproximationApproach.class;
				}
	
				ResultTable resultTable = estimates.getEstimates(approach, 0);
				Vector x = resultTable.getLastEstimates();
				for (int i = 0; i < x.rows(); i++) {
					// String resourceName = resultTable.getResource(i).getName();
					String serviceName = resultTable.getService(i).getName();
					double rd = x.get(i);
					resourceDemandMap.put(serviceName, rd);
				}
			}catch(StackOverflowError e){
				log.error(e);
			}
		}

		// Network delay
		if (networkTimeSeries != null) {
			Vector networkDelayVector = networkTimeSeries.getData(0);
			double averageDelay = LinAlg.sum(networkDelayVector).get(0) / networkDelayVector.rows();
			double stdDev = 0;
			for (double d : networkDelayVector.toArray1D()) {
				stdDev += Math.abs(d - averageDelay);
			}
			stdDev = stdDev / networkDelayVector.rows();
			// log.info("\taverageDelay "+averageDelay+", stdDev "+stdDev);
			if (stdDev > 0.5 * averageDelay) {
				log.info("\tstandard deviation for network delays high (" + stdDev + ") compared to average delay ("
						+ averageDelay + "). Maybe extend model with network package size parameters.");
				// log.info("\t==> network model accuracy INsufficient");
			} else {
				log.info("\tstandard deviation for network delays (" + stdDev + ") is ok compared to average delay ("
						+ averageDelay + ")");
				// log.info("\t==> network model accuracy sufficient");
			}
			resourceDemandMap.put("Network", averageDelay);
		}
		super.deliver(ResourceDemandFilter.OUTPUT_PORT_NAME_DEMANDS, resourceDemandMap);
		super.terminate(errorBeforeTermination);
	}
	
	
}
