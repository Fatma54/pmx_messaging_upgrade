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
package tools.descartes.pmx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;

public class PMXCocomeTest extends PMXPalladioTest{
	
	/**
	 * Cocome is a community case study wich has beed developed within DFG SPP.
	 * Version of the first funding periode.
	 * 
	 * This and other traces can be downloaded from 
	 * https://zenodo.org/record/15986#.Vp6NMlInKFs
	 */
	@Ignore
	/** Reason for ignoring
	WARNUNG: Caught exception when sending data from kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter: OutputPort validTraces to kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter's InputPort inputTraceEvents
	java.lang.ArrayIndexOutOfBoundsException: -1
		at kieker.common.util.signature.ClassOperationSignaturePair.splitOperationSignatureStr(ClassOperationSignaturePair.java:158)
		at kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter$TraceEventRecordHandler.finishExecution(TraceEventRecords2ExecutionAndMessageTraceFilter.java:289)
		at kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter$TraceEventRecordHandler.handleAfterEvent(TraceEventRecords2ExecutionAndMessageTraceFilter.java:436)
		at kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter$TraceEventRecordHandler.handleAfterOperationEvent(TraceEventRecords2ExecutionAndMessageTraceFilter.java:456)
		at kieker.tools.traceAnalysis.filter.flow.TraceEventRecords2ExecutionAndMessageTraceFilter.inputTraceEvents(TraceEventRecords2ExecutionAndMessageTraceFilter.java:168)
		at sun.reflect.GeneratedMethodAccessor23.invoke(Unknown Source)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)
		at java.lang.reflect.Method.invoke(Unknown Source)
		at kieker.analysis.plugin.AbstractPlugin.deliver(AbstractPlugin.java:212)
		at kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter.newEvent(EventRecordTraceReconstructionFilter.java:262)
		...
	*/
	@Test
	public void testWithCocomeTrace() throws Exception{
//		System.out.println(org.iobserve.common.record.ServletDeployedEvent.class);
//		System.out.println(org.iobserve.common.record.ServletDeployedEvent.class.getCanonicalName());
//		//this.stringRegistry.get("org.iobserve.common.record.ServletDeployedEvent");
//		final Class<? extends IMonitoringRecord> clazz = AbstractMonitoringRecord.classForName(org.iobserve.common.record.ServletDeployedEvent.class.toString());
//		final Class<?>[] typeArray = AbstractMonitoringRecord.typesForClass(clazz);
		
		String filename = "kieker-20141103-190203561-UTC-j2eeservice-KIEKER";
		extractModel(filename);
		String resultPath = getResultPath(filename);
				
		//components
		//System.out.println("QQQQQ "+ new File(resultPath + "extracted.repository").toURI().toString());
		Repository repository = PCMLoader.loadRepository(new File(resultPath +File.separator+ "extracted.repository").toURI().toString());
		assertEveryComponentHasServiceEffectspecification(repository);
		assertEquals(30, repository.getComponents__Repository().size());
		//28 Components + 2 Exceptions


		//hosts
		ResourceEnvironment resourceEnvironment = PCMLoader.loadResourceenvironment(new File(resultPath +"extracted.resourceenvironment").toURI().toString());
		assertEquals(2,resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
		List<String> hostNames = new ArrayList<>();
		hostNames.add("j2eeservice");
		for(ResourceContainer x: resourceEnvironment.getResourceContainer_ResourceEnvironment()){
			String name = x.getEntityName();
			assertTrue(hostNames.contains(name));
			hostNames.remove(name);
		}
		assertTrue(hostNames.isEmpty());

		//deployment
		Allocation allocation = PCMLoader.loadAllocation(new File(resultPath+"extracted.allocation").toURI().toString());
		assertEquals(30, allocation.getAllocationContexts_Allocation().size());
//		List<String> allocationNames = new ArrayList<>();
//		allocationNames.add("Allocation_Assembly_Bookstore#SRV0 <Bookstore> <Bookstore>");
//		allocationNames.add("Allocation_Assembly_CRM#SRV0 <CRM> <CRM>");
//		allocationNames.add("Allocation_Assembly_Catalog#SRV0 <Catalog> <Catalog>");
//		allocationNames.add("Allocation_Assembly_Catalog#SRV1 <Catalog> <Catalog>");
//		for(AllocationContext x: allocation.getAllocationContexts_Allocation()){
//			String name = x.getEntityName();
//			assertTrue(allocationNames.contains(name));
//			allocationNames.remove(name);
//		}
//		assertTrue(allocationNames.isEmpty());

	}
	
	/**
	 * Cocome is a community case study wich has beed developed within DFG SPP.
	 * Version of the first round.
	 * 
	 * This and other traces can be downloaded from 
	 * https://zenodo.org/record/15986#.Vp6NMlInKFs
	 */
	@Test
	public void testWithCocomeTrace_2016_03_09() throws Exception{
//		String filename = "cocome_2016_03_09"+File.separator+"logic"+File.separator+"kieker-20160309-101549259-UTC-logicnode-KIEKER"+File.separator;
//		extractModel(filename);
//		filename = "cocome_2016_03_09"+File.separator+"web"+File.separator+"kieker-20160309-101618094-UTC-webnode-KIEKER"+File.separator;
//		extractModel(filename);		
//		filename = "cocome_2016_03_09"+File.separator+"adapter"+File.separator+"kieker-20160309-101050595-UTC-srvadapter-KIEKER"+File.separator;
//		extractModel(filename);
		
//		System.out.println(org.iobserve.common.record.EJBDeployedEvent.class);
//		System.out.println(org.iobserve.common.record.EJBDeployedEvent.class.getCanonicalName());

		
		String basepath = System.getProperty("user.dir")+File.separator+"testFiles"+File.separator;
		ArrayList<String> filenames = new ArrayList<>();
		filenames.add(basepath+"cocome_2016_03_09"+File.separator+"logic"+File.separator+"kieker-20160309-101549259-UTC-logicnode-KIEKER"+File.separator);
		filenames.add(basepath+"cocome_2016_03_09"+File.separator+"web"+File.separator+"kieker-20160309-101618094-UTC-webnode-KIEKER"+File.separator);
		filenames.add(basepath+"cocome_2016_03_09"+File.separator+"adapter"+File.separator+"kieker-20160309-101050595-UTC-srvadapter-KIEKER"+File.separator);
		String outputdir = System.getProperty("user.dir")+File.separator+"extracted"+File.separator+"cocome_2016_03_09"+File.separator+"pcm"+File.separator;
		extractModelFromMultipleSources(filenames, outputdir);

		//testBasicAssertions(resultPath);

	}
	
	/**
	 * Cocome introduces many new kieker record types. This test runs on a file where all records except for the OperationExecutionRecords have been deleted.
	 * @throws Exception
	 */
	@Test
	public void testWithCocomeTrace_2016_03_09_reduced() throws Exception{
		String filename = "cocome_2016_03_09_modified"+File.separator+"logic"+File.separator+"kieker-20160309-101549259-UTC-logicnode-KIEKER"+File.separator;
		extractModel(filename);
		String resultPath = getResultPath(filename);

		//testBasicAssertions(resultPath);

	}

}
