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
package tools.descartes.pmx.filter.resourcedemands.adapter;

import org.apache.log4j.Logger;

import tools.descartes.librede.algorithm.IKalmanFilterAlgorithm;
import tools.descartes.librede.approach.ResponseTimeApproximationApproach;
import tools.descartes.librede.approach.ServiceDemandLawApproach;
import tools.descartes.librede.approach.WangKalmanFilterApproach;
import tools.descartes.librede.configuration.ConfigurationFactory;
import tools.descartes.librede.configuration.EstimationAlgorithmConfiguration;
import tools.descartes.librede.configuration.EstimationApproachConfiguration;
import tools.descartes.librede.configuration.EstimationSpecification;
import tools.descartes.librede.configuration.LibredeConfiguration;
import tools.descartes.librede.configuration.Resource;
import tools.descartes.librede.configuration.TraceConfiguration;
import tools.descartes.librede.configuration.WorkloadDescription;
import tools.descartes.librede.datasource.memory.InMemoryDataSource;
import tools.descartes.librede.exceptions.NonOverlappingRangeException;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Time;
import tools.descartes.librede.units.UnitsFactory;
import tools.descartes.librede.util.RepositoryUtil;
import tools.descartes.librede.util.RepositoryUtil.Range;

public class EstimationSpecificationFactory {
	private static final Logger log = Logger
			.getLogger(EstimationSpecificationFactory.class);

	public static EstimationSpecification createEstimationSpecification(
			InMemoryDataSource dataSource, WorkloadDescription workloadDescription, String host,
			LibredeConfiguration configuration, int numCores) {
		
		EstimationSpecification estimationSpecification = ConfigurationFactory.eINSTANCE
				.createEstimationSpecification();
		estimationSpecification.setRecursive(false);
		configuration.setEstimation(estimationSpecification);

		Range range;
		try {
			range = RepositoryUtil.deduceMaximumOverlappingInterval(dataSource,
					configuration);
		} catch (NonOverlappingRangeException e) {
			range = RepositoryUtil.deduceMaximumInterval(dataSource, configuration);
			for (TraceConfiguration trace : configuration.getInput().getObservations()) {
				TimeSeries timeSeries = dataSource.getData(trace.getLocation());
				timeSeries.setStartTime(range.getStart());
				timeSeries.setEndTime(range.getEnd());
			}
		}
		
		estimationSpecification
				.setStepSize(UnitsFactory.eINSTANCE.createQuantity(range.getValue() / 10, Time.SECONDS));
		estimationSpecification.setWindow(60);
		
		configuration.getEstimation().setStartTimestamp(UnitsFactory.eINSTANCE.createQuantity(range.getStart(), Time.SECONDS));
		configuration.getEstimation().setEndTimestamp(UnitsFactory.eINSTANCE.createQuantity(range.getEnd(), Time.SECONDS));

//		log.info("\tuse measurements from [" +range.getStart() + ","+ range.getEnd()+"] "+Time.SECONDS+" (librede configuration)");
//		for(Service service: repository.listServices()){	
//			TimeSeries x = repository.select(StandardMetrics.RESPONSE_TIME, Time.SECONDS, service, Aggregation.NONE);
//			log.info("\t\t"+service.getName()+"____: "+x);
//			log.info("\t\t\t"+x.getStartTime() + " - "+x.getEndTime());
//		}
//		for(Resource resource: repository.listResources()){
//			if(repository.exists(StandardMetrics.UTILIZATION, resource, Aggregation.AVERAGE)){
//				TimeSeries x = repository.select(StandardMetrics.UTILIZATION, Ratio.NONE, resource, Aggregation.AVERAGE); 
//				log.info("\t\t"+resource.getName()+"____: "+x);
//				log.info("\t\t\t"+x.getStartTime() + " - "+x.getEndTime());
//			}
//		}

		addEstimationSpecification(workloadDescription, configuration, host, estimationSpecification, numCores);
		//addEstimationSpeci
		// addServiceDemandLawToEstimationSpecification(estimationSpecification);
		// EstimationAlgorithmConfiguration estimationAlgorithmConfiguration =
		// ConfigurationFactory.eINSTANCE.createEstimationAlgorithmConfiguration();
		// estimationAlgorithmConfiguration.setType(SimpleApproximation.class.getCanonicalName());
		// estimationSpecification.getAlgorithms().add(estimationAlgorithmConfiguration);
		//
		return estimationSpecification;
	}
	
	private static void addEstimationSpecification(
			WorkloadDescription workloadDescription,
			LibredeConfiguration libredeConfiguration, String host,
			EstimationSpecification estimationSpecification, int numCores) {
		if (workloadDescription.getResources().size() == 0) {
			log.info("\tno resource utilization logs for this host");
			//log.info("\tassumtion: one CPU with two cores");
			log.info("\tcan only apply response time estimation approach");
			log.info("\tcpu has " + numCores);
			Resource cpu = ConfigurationFactory.eINSTANCE.createResource();
			cpu.setName("CPU@" + host);
			cpu.setNumberOfServers(numCores);
			workloadDescription.getResources().add(cpu);
			LibReDEAdapter.mapServicesToResources(workloadDescription);

			//addFake2CoreCPU(repository, host, libredeConfiguration);
		} else {
			EstimationSpecificationFactory.addServiceDemandLawToEstimationSpecification(estimationSpecification);
//			EstimationSpecificationFactory
//			.addZhangKalmannApproachToEstimationSpecification(estimationSpecification);
		}
		EstimationSpecificationFactory
		.addResponseTimeApproximationApproachToEstimationSpecification(estimationSpecification);
	}

	private static void addResponseTimeApproximationApproachToEstimationSpecification(
			EstimationSpecification estimationSpecification) {
		EstimationApproachConfiguration estimationApproachConfiguration = ConfigurationFactory.eINSTANCE
				.createEstimationApproachConfiguration();
		estimationApproachConfiguration
				.setType(ResponseTimeApproximationApproach.class
						.getCanonicalName());
		estimationSpecification.getApproaches().add(
				estimationApproachConfiguration);
	}

	private static void addZhangKalmannApproachToEstimationSpecification(
			EstimationSpecification estimationSpecification) {
		EstimationApproachConfiguration estimationApproachConfiguration = ConfigurationFactory.eINSTANCE
				.createEstimationApproachConfiguration();
		estimationApproachConfiguration.setType(WangKalmanFilterApproach.class
				.getCanonicalName());
		estimationSpecification.getApproaches().add(
				estimationApproachConfiguration);
		EstimationAlgorithmConfiguration estimationAlgorithmConfiguration = ConfigurationFactory.eINSTANCE.createEstimationAlgorithmConfiguration();
		estimationSpecification.getAlgorithms().add(estimationAlgorithmConfiguration);
		String iKalmanFilter = IKalmanFilterAlgorithm.class.getCanonicalName();
		estimationAlgorithmConfiguration.setType(iKalmanFilter );
		
	}

	private static void addServiceDemandLawToEstimationSpecification(
			EstimationSpecification estimationSpecification) {
		EstimationApproachConfiguration estimationApproachConfiguration = ConfigurationFactory.eINSTANCE
				.createEstimationApproachConfiguration();
		estimationApproachConfiguration.setType(ServiceDemandLawApproach.class
				.getCanonicalName());
		estimationSpecification.getApproaches().add(
				estimationApproachConfiguration);
		// log.info(estimationApproachConfiguration.getType());
		// log.info("Parameters "+estimationApproachConfiguration.getParameters());
	}

//	public static void addFake2CoreCPU(IMonitoringRepository repository,
//			String host, LibredeConfiguration conf) {
//		Resource cpu = ConfigurationFactory.eINSTANCE.createResource();
//		cpu.setName("CPU@" + host);
//		cpu.setNumberOfServers(2);
//
//		repository.getWorkload().getResources().add(cpu);
//
////		TimeSeries timeSeries = createFakeTimeSeries(conf);
////		repository.insert(StandardMetrics.UTILIZATION, Ratio.NONE, cpu, timeSeries, Aggregation.AVERAGE);
//
//		TraceConfiguration traceConfiguration = ConfigurationFactory.eINSTANCE
//				.createTraceConfiguration();
//		traceConfiguration.setMetric(StandardMetrics.UTILIZATION);
//		traceConfiguration.setInterval(0);
//		TraceToEntityMapping traceToEntityMapping = ConfigurationFactory.eINSTANCE
//				.createTraceToEntityMapping();
//		traceToEntityMapping.setEntity(cpu);
//		traceToEntityMapping.setTraceColumn(0);
//		traceConfiguration.getMappings().add(traceToEntityMapping);
//		conf.getInput().getObservations().add(traceConfiguration);
//	}

//	private static TimeSeries createFakeTimeSeries(LibredeConfiguration conf) {
//		int numRecords = 10000;
//		// +1 is for rounding errors
//		double[] times = new double[numRecords + 1];
//		double[] data = new double[numRecords + 1];
//		for (int i = 0; i < numRecords + 1; i++) {
//			times[i] = conf.getEstimation().getStartTimestamp()
//					/ 1000.0
//					+ (((double) i) / numRecords)
//					* ((conf.getEstimation().getEndTimestamp() - conf
//							.getEstimation().getStartTimestamp()) / 1000.0);
//			data[i] = 0.3;
//		}
//		// DEBUG log.info("===== CPU@"+host+" ===>>>>> "+ times[0] +" "+
//		// times[numRecords]);
//		TimeSeries timeSeries = new TimeSeries(LinAlg.vector(times),
//				LinAlg.vector(data));
//
//		timeSeries
//				.setStartTime(conf.getEstimation().getStartTimestamp() / 1000.0);
//		timeSeries.setEndTime(conf.getEstimation().getEndTimestamp() / 1000.0);
//		return timeSeries;
//	}
}
