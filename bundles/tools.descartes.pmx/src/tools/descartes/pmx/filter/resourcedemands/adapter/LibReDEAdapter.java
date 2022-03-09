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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tools.descartes.librede.Librede;
import tools.descartes.librede.LibredeResults;
import tools.descartes.librede.configuration.ConfigurationFactory;
import tools.descartes.librede.configuration.DataSourceConfiguration;
import tools.descartes.librede.configuration.ExporterConfiguration;
import tools.descartes.librede.configuration.ExternalCall;
import tools.descartes.librede.configuration.InputSpecification;
import tools.descartes.librede.configuration.LibredeConfiguration;
import tools.descartes.librede.configuration.OutputSpecification;
import tools.descartes.librede.configuration.Parameter;
import tools.descartes.librede.configuration.Resource;
import tools.descartes.librede.configuration.ResourceDemand;
import tools.descartes.librede.configuration.Service;
import tools.descartes.librede.configuration.TraceConfiguration;
import tools.descartes.librede.configuration.TraceToEntityMapping;
import tools.descartes.librede.configuration.ValidationSpecification;
import tools.descartes.librede.configuration.WorkloadDescription;
import tools.descartes.librede.datasource.IDataSource;
import tools.descartes.librede.datasource.memory.InMemoryDataSource;
import tools.descartes.librede.export.csv.CsvExporter;
import tools.descartes.librede.metrics.Aggregation;
import tools.descartes.librede.metrics.StandardMetrics;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Ratio;
import tools.descartes.librede.units.Time;
import tools.descartes.librede.units.UnitsFactory;
import tools.descartes.pmx.builder.ModelBuilder;

public class LibReDEAdapter {
	private static final Logger log = Logger.getLogger(LibReDEAdapter.class);

	public static LibredeResults initAndRunLibrede(String host,
			Map<String, TimeSeries> serviceToTimeSeriesMap,
			Map<String, TimeSeries> resourceTimeSeriesMap, String outputPath, int numCores) {
		Librede.init();

		WorkloadDescription workloadDescription = ConfigurationFactory.eINSTANCE
				.createWorkloadDescription(); 
		LibredeConfiguration configuration = addConfigurationToRepository(host, outputPath);
		configuration.setWorkloadDescription(workloadDescription);
		InMemoryDataSource dataSource = new InMemoryDataSource();
		
		DataSourceConfiguration dataSourceConfiguration = ConfigurationFactory.eINSTANCE.createDataSourceConfiguration();
		dataSourceConfiguration.setName("key");
		dataSourceConfiguration.setType(InMemoryDataSource.class.getName());
		configuration.getInput().getDataSources().add(dataSourceConfiguration);
		
		List<TraceConfiguration> serviceObservations = addServices(host, dataSource, workloadDescription,
				serviceToTimeSeriesMap);
		for(TraceConfiguration service: serviceObservations){
			service.setDataSource(dataSourceConfiguration);
		}
		
		List<TraceConfiguration> resourceObservations = addResourcesToLibReDE(host, resourceTimeSeriesMap, dataSource, workloadDescription);// configuration);
		configuration.getInput().getObservations().addAll(resourceObservations);
		for(TraceConfiguration resource: resourceObservations){
			resource.setDataSource(dataSourceConfiguration);
		}

		
		mapServicesToResources(workloadDescription);

		configuration.getInput().getObservations().addAll(serviceObservations);
		configuration.getInput().getDataSources().add(dataSourceConfiguration);
		
		EstimationSpecificationFactory.createEstimationSpecification(
				dataSource, workloadDescription, host, configuration, numCores);

		Map<String, IDataSource> x = new HashMap<String, IDataSource>();
		x.put("key", dataSource);
		LibredeResults libredeResults = Librede.execute(configuration, x);
		/** Receive results from LibReDE */
		return libredeResults;
	}

	public static void mapServicesToResources(WorkloadDescription workloadDescription) {
		for(Service service: workloadDescription.getServices()){
			for(Resource resource: workloadDescription.getResources()){
				ResourceDemand rd = ConfigurationFactory.eINSTANCE.createResourceDemand();
				rd.setResource(resource);
				service.getTasks().add(rd);
			}
			
			//TODO
			ExternalCall ext = ConfigurationFactory.eINSTANCE.createExternalCall();
			ext.setCalledService(service);
			service.getTasks().add(ext);

		}
	}

	private static List<TraceConfiguration> addResourcesToLibReDE(String host,
			Map<String, TimeSeries> resourceTimeSeriesMap,
			InMemoryDataSource dataSource, WorkloadDescription workloadDescription) {
		List<TraceConfiguration> traceConfigurations = new ArrayList<TraceConfiguration>();		
		for (String resourceName: resourceTimeSeriesMap.keySet()) {
			// log.info("Enter number of CPU cores for "+host);
			// cpu.setNumberOfServers(getInputInt());
			if(resourceName.replace("CPU", "").endsWith(ModelBuilder.seperatorChar+host)){
				log.info("\tnumber of servers for "+resourceName+ " = 1");
				TraceConfiguration traceConfiguration = addResourceToLibReDE(dataSource, workloadDescription, host,
						resourceName.split("_" + host)[0], 1, resourceTimeSeriesMap.get(resourceName));
				//conf.getInput().getObservations().add(traceConfiguration);		
				traceConfigurations.add(traceConfiguration);
			}
		}
		return traceConfigurations;

	}

	private static List<TraceConfiguration> addServices(String host, InMemoryDataSource dataSource, WorkloadDescription workloadDescription,
			Map<String, TimeSeries> serviceTimeSeriesMap) {
		List<TraceConfiguration> traceConfigurations = new ArrayList<TraceConfiguration>();
		for (String serviceKey : serviceTimeSeriesMap.keySet()) {
			if (!serviceKey.endsWith(host)) {
				continue;
			}
			TimeSeries timeSeries = serviceTimeSeriesMap.get(serviceKey);
			Service service = ConfigurationFactory.eINSTANCE.createService();
			service.setName(serviceKey);
			dataSource.append(serviceKey, timeSeries);
			workloadDescription.getServices().add(service);
			
			TraceConfiguration traceConfiguration = ConfigurationFactory.eINSTANCE
					.createTraceConfiguration();
			traceConfiguration.setLocation(serviceKey);
			traceConfiguration.setMetric(StandardMetrics.RESIDENCE_TIME);
			traceConfiguration.setInterval(UnitsFactory.eINSTANCE.createQuantity(0,Time.NANOSECONDS));
			TraceToEntityMapping traceToEntityMapping = ConfigurationFactory.eINSTANCE
					.createTraceToEntityMapping();
			traceToEntityMapping.setEntity(service);
			traceConfiguration.setAggregation(Aggregation.NONE);
			traceConfiguration.setUnit(Time.NANOSECONDS);
			traceToEntityMapping.setTraceColumn(1);
			traceConfiguration.getMappings().add(traceToEntityMapping);
			traceConfigurations.add(traceConfiguration);
		}
		return traceConfigurations;
	}


	private static TraceConfiguration addResourceToLibReDE(InMemoryDataSource dataSource, WorkloadDescription workloadDescription,
			String host, String resourceName, int numServers, TimeSeries timeSeries) {
		Resource resource = ConfigurationFactory.eINSTANCE.createResource();
		String key = resourceName+ModelBuilder.seperatorChar+ host;
		resource.setName(key);
		// log.info("Enter number of CPU cores for "+host);
		// cpu.setNumberOfServers(getInputInt());
		resource.setNumberOfServers(numServers);
		workloadDescription.getResources().add(resource);
		double averageTimeIncrement = timeSeries.getAverageTimeIncrement();
		dataSource.append(key, timeSeries);

		TraceConfiguration traceConfiguration = ConfigurationFactory.eINSTANCE
				.createTraceConfiguration();
		traceConfiguration.setLocation(key);
		traceConfiguration.setMetric(StandardMetrics.UTILIZATION);
		traceConfiguration.setInterval(UnitsFactory.eINSTANCE.createQuantity(averageTimeIncrement,Time.SECONDS));
		TraceToEntityMapping traceToEntityMapping = ConfigurationFactory.eINSTANCE
				.createTraceToEntityMapping();
		traceConfiguration.setAggregation(Aggregation.AVERAGE);
		traceConfiguration.setUnit(Ratio.NONE);
		traceToEntityMapping.setEntity(resource);
		traceToEntityMapping.setTraceColumn(1);
		traceConfiguration.getMappings().add(traceToEntityMapping);
		return traceConfiguration;
	}

//	private static LibredeResults runLibReDe(IMonitoringRepository repo,
//			LibredeConfiguration conf) {
//		if (!conf.getValidation().isValidateEstimates()) {
//
//			try {
//				LibredeResults results = Librede.runEstimation(conf, repo);
//				Librede.printSummary(results);
//				Librede.exportResults(conf, results);
//				return results;
//
//			} catch (Exception e) {
//				log.error("Error running estimation.", e);
//			}
//		} else {
//			try {
//				LibredeResults results = Librede
//						.runEstimationWithCrossValidation(conf, repo);
//				Librede.printSummary(results);
//				Librede.exportResults(conf, results);
//				return results;
//			} catch (Exception e) {
//				log.error("Error running estimation.", e);
//			}
//		}
//		return null;
//	}
	
	private static LibredeConfiguration addConfigurationToRepository(String host, String resultPath) {
		LibredeConfiguration conf = ConfigurationFactory.eINSTANCE
				.createLibredeConfiguration();

		ValidationSpecification validationSpecification = ConfigurationFactory.eINSTANCE
				.createValidationSpecification();
		validationSpecification.setValidateEstimates(false);
		conf.setValidation(validationSpecification);

		InputSpecification inputSpecification = ConfigurationFactory.eINSTANCE
				.createInputSpecification();
		conf.setInput(inputSpecification);

	
		//set output
		OutputSpecification outputSpecification = ConfigurationFactory.eINSTANCE
				.createOutputSpecification();
		ExporterConfiguration csvExporterConfiguration = ConfigurationFactory.eINSTANCE
				.createExporterConfiguration();
		csvExporterConfiguration.setType(CsvExporter.class.getCanonicalName());
		csvExporterConfiguration.setName(CsvExporter.class.getName());
		Parameter parameter = ConfigurationFactory.eINSTANCE.createParameter();
		parameter.setName("OutputDirectory");
		parameter.setValue(resultPath);
		csvExporterConfiguration.getParameters().add(parameter);
		outputSpecification.getExporters().add(csvExporterConfiguration);

		conf.setOutput(outputSpecification);
		
		return conf;
	}




}
