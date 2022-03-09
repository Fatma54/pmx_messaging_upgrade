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
/**
 * Copied from iobserve project. Above header is only because of license format checks in PMX.
 */
package org.iobserve.common.record;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.util.registry.IRegistry;
import kieker.common.util.Version;

import kieker.common.record.flow.AbstractEvent;

/**
 * @author Generic Kieker
 * 
 * @since 1.12
 */
public abstract class ServletDeploymentEvent extends AbstractEvent  {
		private static final long serialVersionUID = -3181427847418420309L;
	
	
	/* user-defined constants */
	/* default constants */
	public static final String SERIVCE = "";
	public static final String CONTEXT = "";
	public static final String DEPLOYMENT_ID = "";
	/* property declarations */
	private final String serivce;
	private final String context;
	private final String deploymentId;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param serivce
	 *            serivce
	 * @param context
	 *            context
	 * @param deploymentId
	 *            deploymentId
	 */
	public ServletDeploymentEvent(final long timestamp, final String serivce, final String context, final String deploymentId) {
		super(timestamp);
		this.serivce = serivce == null?"":serivce;
		this.context = context == null?"":context;
		this.deploymentId = deploymentId == null?"":deploymentId;
	}

	
	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected ServletDeploymentEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this.serivce = (String) values[1];
		this.context = (String) values[2];
		this.deploymentId = (String) values[3];
	}

	/**
	 * This constructor converts the given array into a record.
	 * 
	 * @param buffer
	 *            The bytes for the record.
	 * 
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public ServletDeploymentEvent(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
		this.serivce = stringRegistry.get(buffer.getInt());
		this.context = stringRegistry.get(buffer.getInt());
		this.deploymentId = stringRegistry.get(buffer.getInt());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.BinaryFactory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		throw new UnsupportedOperationException();
	}

	public final String getSerivce() {
		return this.serivce;
	}
	
	public final String getContext() {
		return this.context;
	}
	
	public final String getDeploymentId() {
		return this.deploymentId;
	}
	
}
