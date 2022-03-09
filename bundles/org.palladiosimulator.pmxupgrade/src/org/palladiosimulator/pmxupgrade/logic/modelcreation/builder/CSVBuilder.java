package org.palladiosimulator.pmxupgrade.logic.modelcreation.builder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Builder for an CSV-Export of the extracted workload information.
 *
 * @author PMX, Universitaet Wuerzburg.
 */
public class CSVBuilder {

    private static final Logger log = LogManager.getLogger(CSVBuilder.class);

    public static void setOutputDirectory(String outputDirectory) {
        CSVBuilder.outputDirectory = outputDirectory;
        new File(outputDirectory).mkdirs();
    }

    private static String outputDirectory;

    static void display(HashMap<String, List<Double>> workload) {
        for (String s : workload.keySet()) {
            List<Double> list = workload.get(s);
            for (int i = 0; i < 4; i++) {
                log.info("\tXXX" + list.get(i));
            }
            log.info(list.size() + " " + s);
        }
    }

    public static void createPetClinicCSVs(HashMap<String, List<Double>> workload) {
        display(workload);

        List<Double> list = workload.get("showOwnerGET#OwnerControllerHTTP#petclinic");
        List<Double> first = new ArrayList<>();
        List<Double> second = new ArrayList<>();
        sort(list);
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                first.add(list.get(i));
            } else {
                second.add(list.get(i));
            }
        }
        log.info(first.size() + " first");
        log.info(second.size() + " second");
        createInterarrivalCSV(first, second, "showOwnerGET", "showOwnerGET");

        createInterarrivalCSV(workload.get("processFindFormGET#OwnerControllerHTTP#petclinic"), first,
                "processFindFormGET", "showOwnerGET");

        createInterarrivalCSV(workload, "initFindFormGET#OwnerControllerHTTP#petclinic",
                "processFindFormGET#OwnerControllerHTTP#petclinic");
        createInterarrivalCSV(workload, "showVetListGET#VetControllerHTTP#petclinic",
                "initFindFormGET#OwnerControllerHTTP#petclinic");
        createInterarrivalCSV(workload, "welcomeGET#WelcomeControllerHTTP#petclinic",
                "showVetListGET#VetControllerHTTP#petclinic");
        createInterarrivalCSV(workload, "welcomeGET#WelcomeControllerHTTP#petclinic");

        display(workload);
    }

    public static void createCombinedCSV(HashMap<String, List<Double>> workload) {
        StringBuilder sb = new StringBuilder();
        for (String s : workload.keySet()) {
            List<Double> xy = workload.get(s);
            sort(xy);
            sb.append(shortenName(s));

            for (int i = 1; i < xy.size(); i++) {
                Double element = xy.get(i);
                sb.append(format(element));
                sb.append(";");
            }
            sb.append("\n");
        }
        String fileName = outputDirectory + "workloads" + File.separator;
        saveToFile(sb.toString(), fileName);
    }

    private static String format(Double d) {
        if (d < 0) {
            log.error("d<0");
            d = 0.0;
        }
        return "" + String.format("%.19f", (((d) / 1000.0 / 1000.0 / 1000.0))).replace(",", ".");
    }

    private static void createInterarrivalCSV(List<Double> arrivalsFirst, List<Double> arrivalsSecond, String first,
                                              String second) {
        log.info("LOGGGING " + first + "_ " + second);
        StringBuilder sb = new StringBuilder();
        sort(arrivalsFirst);
        sort(arrivalsSecond);
        while (arrivalsFirst.get(0) > arrivalsSecond.get(0)) {
            arrivalsSecond.remove(0);
            log.error("removed from " + second);
        }
        arrivalsSecond.remove(0);
        log.error("removed from " + second);

        for (int i = 0; i < Math.min(arrivalsFirst.size(), arrivalsSecond.size()); i++) {
            Double firstElem = arrivalsFirst.get(i);
            Double secondElem = arrivalsSecond.get(i);
            sb.append(format(((secondElem - firstElem))));
            sb.append(";");
        }
        saveToFile(sb.toString(),
                outputDirectory + "interarrival_" + shortenName(first) + "_" + shortenName(second) + ".csv");
    }

    private static void createInterarrivalCSV(HashMap<String, List<Double>> workload, String first, String second) {
        List<Double> arrivalsFirst = workload.get(first);
        List<Double> arrivalsSecond = workload.get(second);
        createInterarrivalCSV(arrivalsFirst, arrivalsSecond, first, second);

    }

    private static void createInterarrivalCSV(HashMap<String, List<Double>> workload, String s) {
        StringBuilder sb = new StringBuilder();
        List<Double> arrivals = workload.get(s);
        sort(arrivals);
        Double previousElement = arrivals.get(0);
        for (int i = 1; i < arrivals.size(); i++) {
            Double element = arrivals.get(i);
            sb.append(format((element - previousElement)));

            sb.append(";");
            previousElement = element;
        }

        s = shortenName(s);
        String fileName = outputDirectory + "interarrival_" + s + ".csv";
        saveToFile(sb.toString(), fileName);

    }

    private static String shortenName(String s) {
        s = s.split("#")[0];
        s = s.replace("<", "");
        s = s.replace(">", "");
        return s;
    }

    public static void workloadToCSV(HashMap<String, List<Double>> workload) {
        for (String s : workload.keySet()) {
            StringBuilder sb = new StringBuilder();
            for (Double element : workload.get(s)) {
                sb.append(element);
                sb.append(";");
            }
            String fileName = outputDirectory + shortenName(s) + "_empiricaldist.csv";
            saveToFile(sb.toString(), fileName);
        }
    }

    private static void saveToFile(String content, String fileName) {
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < 100; i++) {
                br.write(content);
            }
            br.close();
        } catch (IOException e) {
            log.error(e);
        }
        log.info("Stored workload (" + fileName + ")");
    }

    private static void sort(List<Double> list) {
        list.sort((o1, o2) -> {
            if ((o1 - o2) > 0) {
                return 1;
            } else if ((o1 - o2) < 0) {
                return -1;
            }
            return 0;
        });
    }
}
