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
package tools.descartes.pmx.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class CSVBuilder {
	private static final Logger log = Logger.getLogger(CSVBuilder.class);

	public static void setOutputDirectory(String outputDirectory) {
		CSVBuilder.outputDirectory = outputDirectory;
		new File(outputDirectory).mkdirs();
	}

	static void filterEverySecondOne(List<Double> list, int offset) {

	};

	private static String outputDirectory;

	static void display(HashMap<String, List<Double>> workload) {
		for (String s : workload.keySet()) {
			List<Double> list = workload.get(s);
			for (int i = 0; i < 4; i++) {
				log.info("\tXXX" + list.get(i));
			}

			// // for(min2)
			// if (s.equals("showVetListGET#VetControllerHTTP#petclinic")) {
			// for (int i = 0; i < 3; i++) {
			// list.remove(0);
			// }
			// }
			//
			// if (s.equals("initFindFormGET#OwnerControllerHTTP#petclinic")) {
			// for (int i = 0; i < 2; i++) {
			// list.remove(0);
			// }
			// }
			//
			// if (s.equals("showOwnerGET#OwnerControllerHTTP#petclinic")) {
			// for (int i = 0; i < 4; i++) {
			// list.remove(0);
			// }
			// }

			log.info(list.size() + " " + s);
		}
	}

	public static void createPetClinicCSVs(HashMap<String, List<Double>> workload) {
		display(workload);

		List<Double> list = workload.get("showOwnerGET#OwnerControllerHTTP#petclinic");
		List<Double> first = new ArrayList<Double>();
		List<Double> second = new ArrayList<Double>();
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

		// nimm immer das erste aus zwei

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
		return "" + String.format("%.19f", ((Double) ((d) / 1000.0 / 1000.0 / 1000.0))).replace(",", ".");
	}

	public static void createInterarrivalCSVs(HashMap<String, List<Double>> workload) {
		for (String s : workload.keySet()) {
			createInterarrivalCSV(workload, s);
		}
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
			sb.append(format((Double) ((secondElem - firstElem))));
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
			sb.append(format((Double) (element - previousElement)));
					
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
		// s = s.replace("#", "_");
		return s;
	}

	public static void workloadToCSV(HashMap<String, List<Double>> workload) {
		for (String s : workload.keySet()) {
			StringBuilder sb = new StringBuilder();
			for (Double element : workload.get(s)) {
				sb.append("" + element);
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
		list.sort(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				if ((o1 - o2) > 0) {
					return 1;
				} else if ((o1 - o2) < 0) {
					return -1;
				}
				return 0;
			}
		});
	}
}
