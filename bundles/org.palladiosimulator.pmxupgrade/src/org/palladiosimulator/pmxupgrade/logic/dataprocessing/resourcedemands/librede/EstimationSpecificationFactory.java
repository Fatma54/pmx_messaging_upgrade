package org.palladiosimulator.pmxupgrade.logic.dataprocessing.resourcedemands.librede;

import tools.descartes.librede.approach.ResponseTimeApproximationApproach;
import tools.descartes.librede.approach.ServiceDemandLawApproach;
import tools.descartes.librede.configuration.*;
import tools.descartes.librede.datasource.memory.InMemoryDataSource;
import tools.descartes.librede.exceptions.NonOverlappingRangeException;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Time;
import tools.descartes.librede.units.UnitsFactory;
import tools.descartes.librede.util.RepositoryUtil;
import tools.descartes.librede.util.RepositoryUtil.Range;

/**
 * Factory which prepares the resource demand estimation with LibREdE by specifying concrete approaches
 * for the estimation.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class EstimationSpecificationFactory {

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

        addEstimationSpecification(workloadDescription, configuration, host, estimationSpecification, numCores);
        return estimationSpecification;
    }

    private static void addEstimationSpecification(
            WorkloadDescription workloadDescription,
            LibredeConfiguration libredeConfiguration, String host,
            EstimationSpecification estimationSpecification, int numCores) {
        if (workloadDescription.getResources().size() == 0) {
            Resource cpu = ConfigurationFactory.eINSTANCE.createResource();
            cpu.setName("CPU@" + host);
            cpu.setNumberOfServers(numCores);
            workloadDescription.getResources().add(cpu);
            LibReDEAdapter.mapServicesToResources(workloadDescription);
        } else {
            EstimationSpecificationFactory.addServiceDemandLawToEstimationSpecification(estimationSpecification);
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

    private static void addServiceDemandLawToEstimationSpecification(
            EstimationSpecification estimationSpecification) {
        EstimationApproachConfiguration estimationApproachConfiguration = ConfigurationFactory.eINSTANCE
                .createEstimationApproachConfiguration();
        estimationApproachConfiguration.setType(ServiceDemandLawApproach.class
                .getCanonicalName());
        estimationSpecification.getApproaches().add(
                estimationApproachConfiguration);
    }

}
