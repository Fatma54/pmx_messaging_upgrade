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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.iobserve.common.record.IUndeploymentRecord;
import org.iobserve.common.record.ServletDeploymentEvent;

import kieker.common.util.registry.IRegistry;
import kieker.common.util.Version;

/**
 * @author Generic Kieker
 * 
 * @since 1.12
 */
public class ServletUndeployedEvent extends ServletDeploymentEvent implements IUndeploymentRecord {
	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // AbstractEvent.timestamp
			 + TYPE_SIZE_STRING // ServletDeploymentEvent.serivce
			 + TYPE_SIZE_STRING // ServletDeploymentEvent.context
			 + TYPE_SIZE_STRING // ServletDeploymentEvent.deploymentId
	;
	private static final long serialVersionUID = -8547454785236238279L;
	
	public static final Class<?>[] TYPES = {
		long.class, // AbstractEvent.timestamp
		String.class, // ServletDeploymentEvent.serivce
		String.class, // ServletDeploymentEvent.context
		String.class, // ServletDeploymentEvent.deploymentId
	};
	
	/* user-defined constants */
	/* default constants */
	/* property declarations */

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
	public ServletUndeployedEvent(final long timestamp, final String serivce, final String context, final String deploymentId) {
		super(timestamp, serivce, context, deploymentId);
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public ServletUndeployedEvent(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
	}
	
	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected ServletUndeployedEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
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
	public ServletUndeployedEvent(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getSerivce(),
			this.getContext(),
			this.getDeploymentId()
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getSerivce());
		stringRegistry.get(this.getContext());
		stringRegistry.get(this.getDeploymentId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferOverflowException {
		buffer.putLong(this.getTimestamp());
		buffer.putInt(stringRegistry.get(this.getSerivce()));
		buffer.putInt(stringRegistry.get(this.getContext()));
		buffer.putInt(stringRegistry.get(this.getDeploymentId()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
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

}
