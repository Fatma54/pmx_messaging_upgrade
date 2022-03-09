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
package tools.descartes.pmx.pcm.builder.util.cmbg;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/** Customer Model Behavior Graph */
public class CMBG {
	private static final Logger log = Logger
			.getLogger(CMBG.class);
	int population = 50; 		//TODO
//	CInteger.parseInt(creatorTools.getThisWorkloadModel()
//	.getWorkloadIntensity().getFormula())
	
	public int getPopulation(){
		return population;
	}
	
	public Service getInitialState(){
		return new Service();
	}

	public List<Double> getrelativeFrequencies() {
		//BehaviorMix.getRelativeFrequencies();
		List<Double> relativeFrequencies = new ArrayList<Double>();
		relativeFrequencies.add(0.2);
		relativeFrequencies.add(0.4);
		relativeFrequencies.add(0.4);
		checkValidity(relativeFrequencies);
		return relativeFrequencies;
	}
	
	public List<BehaviorModel> getBehaviorModels(){
		List<BehaviorModel> behaviorModels = new ArrayList<BehaviorModel>();
		behaviorModels.add(new BehaviorModel());
		return behaviorModels;
//		BehaviorModel
	}
	
	private static void checkValidity(List<Double> relativeFrequencies) {
		// TODO consider rounding errors
		double result = 0;
		for(double frequency: relativeFrequencies){
			result += frequency;
		}
		if(result > 1.0){
			log.warn("RelativeFrequencies "+ relativeFrequencies+ ": Sum > 1!");
		}
	}
}
