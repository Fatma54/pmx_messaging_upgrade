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

import java.io.File;

import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;

import tools.descartes.pmx.pcm.builder.persistance.PCMLoader;

public class PMXNopCommerceTest extends PMXPalladioTest{
	
	/**
	 * The NopCommerce application is a web shop very popular in the .Net world.
	 * http://www.nopcommerce.com/
	 */
	@Test
	public void testWithNopCommerceTrace() {
		String filename = "nopCommerce"+File.separator+"kieker-20151008-145457358-UTC-WIN-JQHNDE89VN4-KIEKER"+File.separator;
		extractModel(filename);

		String resultPath = getResultPath(filename);

		Repository repository = PCMLoader.loadRepository(new File(resultPath + "extracted.repository").toURI().toString());
		assertEquals(38, repository.getComponents__Repository().size());
		assertEveryComponentHasServiceEffectspecification(repository);
	}}
