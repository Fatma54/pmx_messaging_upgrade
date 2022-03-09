package org.palladiosimulator.pmxupgrade.logic.dataprocessing.resourcedemands;

import org.palladiosimulator.pmxupgrade.logic.dataprocessing.resourcedemands.librede.LibReDEAdapter;
import org.palladiosimulator.pmxupgrade.logic.modelcreation.builder.ModelBuilder;
import org.palladiosimulator.pmxupgrade.model.common.Configuration;
import org.palladiosimulator.pmxupgrade.model.systemmodel.component.AllocationComponent;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.AbstractMessage;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.MessageTrace;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

import java.util.*;

/**
 * Service for estimating the resource demands using LibReDE.
 *
 * @author Patrick Treyer
 */
public class ResourceDemandEstimationService {

    private static Map<String, TimeSeries> serviceTimeSeriesMap;
    private static Map<String, TimeSeries> resourceTimeSeriesMap;
    private static TimeSeries networkTimeSeries;
    private static Set<String> hosts = new HashSet<>();
    private String outputPath;
    private HashMap<String, Integer> numCores = new HashMap<>();

    private static final Logger log = LogManager.getLogger(ResourceDemandEstimationService.class);

    public ResourceDemandEstimationService(final Configuration configuration) {
        this.outputPath = configuration.getOutputDirectory();
        serviceTimeSeriesMap = new HashMap<>();
        resourceTimeSeriesMap = new HashMap<>();
    }

    /**
     * Method for initiating the resource demand estimation with LibReDE.
     *
     * @param mt, the individual @{@link MessageTrace} of the tracing data.
     */
    public void inputMessageTraces(final MessageTrace mt) {
        Map<Execution, Double> externalCallTime = new HashMap<>();
        Map<Execution, List<Execution>> externalCallMethods = new HashMap<>();

        calculateExternalCallTime(mt, externalCallTime, externalCallMethods);

        for (Execution execution : externalCallTime.keySet()) {
            // ignore entry calls
            if (StringUtils.contains(execution.getAllocationComponent().getAssemblyComponent().getType().getTypeName(), "Entry")) {
                continue;
            }

            double time = (execution.getTout() - execution.getTin());
            double externalTime = externalCallTime.get(execution);
            if (time < 0) {
                log.error("time < 0: time = " + time);
            }

            if (externalTime - time > 0.001 * time) {        // measurement >10% uncertain
                boolean error = true;
                AllocationComponent ac = execution.getAllocationComponent();
                for (Execution sub : externalCallMethods.get(execution)) {
                    if (!ac.equals(sub.getAllocationComponent())) {
                        error = false;
                        break;
                    }
                }
                if (error) {
                    log.error("time < external time (trace id " + execution.getTraceId() + ") " + execution.getAllocationComponent().getAssemblyComponent().getType().getTypeName() + " " + execution.getOperation().getSignature().getName());
                    log.error("\t" + "time = " + time);
                    log.error("\t" + "exte = " + externalTime);
                    if (externalCallMethods.get(execution) != null) {
                        for (Execution sub : externalCallMethods.get(execution))
                            log.error("\t" + "       " + (sub.getTout() - sub.getTin()) + " << " + sub.getAllocationComponent().getAssemblyComponent().getType().getTypeName() + " " + sub.getOperation().getSignature().getName() + "(trace id " + sub.getTraceId() + ") ");
                    }
                } else {
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
    }

    private void calculateExternalCallTime(MessageTrace mt, Map<Execution, Double> externalCallTime, Map<Execution, List<Execution>> externalCallMethods) {
        List<AbstractMessage> messages = mt.getSequenceAsVector();
        for (AbstractMessage message : messages) {
            Execution sender = message.getSendingExecution();
            Execution receiver = message.getReceivingExecution();

            // check if receiver comes after sender
            if (sender.getEss() < receiver.getEss()) {

                // put sender and receiver in list
                if (!externalCallTime.containsKey(sender))
                    externalCallTime.put(sender, (double) 0);
                if (!externalCallTime.containsKey(receiver))
                    externalCallTime.put(receiver, (double) 0);

                // Time lost at linking resources -> add network delay
                if (!sender.getAllocationComponent()
                        .getExecutionContainer()
                        .equals(receiver.getAllocationComponent()
                                .getExecutionContainer())) {
                    if (!StringUtils.contains(sender.getAllocationComponent().getAssemblyComponent().getName(), "Entry")) {
                        double networkDelay = (receiver.getTin() - sender.getTout());
                        double timestamp = sender.getTout();
                        addNetworkLog(timestamp, networkDelay);
                    }
                }

                // add externalCallTime
                double externalTime = (receiver.getTout() - receiver.getTin());
                externalCallTime.put(sender, externalCallTime.get(sender) + externalTime);

                // add externalCallMethod
                List<Execution> list = externalCallMethods.get(sender);
                list = (list == null) ? new ArrayList<>() : list;
                list.add(receiver);
                externalCallMethods.put(sender, list);
            }
        }
    }

    public static void addNetworkLog(double timestamp, double delay) {
        if (networkTimeSeries == null) {
            double[] timeValue = new double[1];
            timeValue[0] = timestamp;
            tools.descartes.librede.linalg.Vector time = LinAlg.vector(timeValue);

            double[] values = new double[1];
            values[0] = delay;
            Matrix data = LinAlg.matrix(values);
            networkTimeSeries = new TimeSeries(time, data);
        } else {
            networkTimeSeries = networkTimeSeries.addSample(timestamp, delay);
        }
    }

    /**
     * Starting point for adding other metrics in future version of the approach.
     *
     * @param timestamp,   timestamp of the executions.
     * @param host,        host of the executed operation.
     * @param resource,    name of the resource.
     * @param utilization, the obtained system utilizaton
     */
    public static synchronized void addResourceLog(double timestamp, String host, String resource, double utilization) {
        TimeSeries timeSeries;
        String key = resource + "_" + host;
        if (!resourceTimeSeriesMap.containsKey(key)) {
            double[] timeValue = new double[1];
            timeValue[0] = timestamp;
            tools.descartes.librede.linalg.Vector time = LinAlg.vector(timeValue);

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
        TimeSeries timeSeries;
        String key = interfaceName + ModelBuilder.seperatorChar + host;
        if (exTime < 0) {
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
        } else {
            timeSeries = serviceTimeSeriesMap.get(key);
            timeSeries = timeSeries.addSample(timestamp, exTime);
        }
        serviceTimeSeriesMap.put(key, timeSeries);
    }


    public HashMap<String, Double> terminate() {
        if (serviceTimeSeriesMap.keySet().isEmpty()) {
            log.error("could not extract service times to estimate resource demands");    //No service execution logs could be found
            return null;
        }

        HashMap<String, Double> resourceDemandMap = new HashMap<>();

        for (String host : hosts) {
            StringBuilder sb = new StringBuilder();
            for (String service : serviceTimeSeriesMap.keySet()) {
                if (service.endsWith(host)) {
                    sb.append(service.replace(ModelBuilder.seperatorChar + host, "")).append(" | ");
                }
            }
            System.out.println("arrived");
            log.info("Estimate resource demands on [" + host + "]");
            log.info("\tservices: |" + sb.toString());

            try {
                Integer cores = null;
                if (numCores.containsKey(host)) {
                    cores = numCores.get(host);
                }
                if (cores == null) {
                    log.warn("no value passed for number of cores at " + host);
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
                tools.descartes.librede.linalg.Vector x = resultTable.getLastEstimates();
                for (int i = 0; i < x.rows(); i++) {
                    String serviceName = resultTable.getService(i).getName();
                    double rd = x.get(i);
                    resourceDemandMap.put(serviceName, rd);
                }
            } catch (StackOverflowError e) {
                log.error(e);
            }
        }

        // Network delay
        if (networkTimeSeries != null) {
            tools.descartes.librede.linalg.Vector networkDelayVector = networkTimeSeries.getData(0);
            double averageDelay = LinAlg.sum(networkDelayVector).get(0) / networkDelayVector.rows();
            double stdDev = 0;
            for (double d : networkDelayVector.toArray1D()) {
                stdDev += Math.abs(d - averageDelay);
            }
            stdDev = stdDev / networkDelayVector.rows();
            if (stdDev > 0.5 * averageDelay) {
                log.info("\tstandard deviation for network delays high (" + stdDev + ") compared to average delay ("
                        + averageDelay + "). Maybe extend model with network package size parameters.");
            } else {
                log.info("\tstandard deviation for network delays (" + stdDev + ") is ok compared to average delay ("
                        + averageDelay + ")");
            }
            resourceDemandMap.put("Network", averageDelay);
        }

        return resourceDemandMap;
    }

    public void addCPUCoreNumber(String host, Integer number) {
        if (numCores == null) {
            numCores = new HashMap<>();
        }
        numCores.put(host, number);
    }


}
