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
package tools.descartes.pmx.filter.controlflow;

import java.util.concurrent.TimeUnit;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.AbstractNodeDecorator;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.systemModel.AbstractMessage;

public class CallNodeDecorator extends AbstractNodeDecorator {

	@Override
	public void processMessage(AbstractMessage message,
			DependencyGraphNode<?> sourceNode, DependencyGraphNode<?> targetNode,
			TimeUnit timeUnit) {
		if (sourceNode.equals(targetNode)) {
			return;
		}

		CallDecoration callDecoration = targetNode.getDecoration(CallDecoration.class);

		if (callDecoration == null) {
			callDecoration = new CallDecoration();
			targetNode.addDecoration(callDecoration);
		}
		
		callDecoration.registerExecution();	//message.getReceivingExecution()


	}

}
