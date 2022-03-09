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

import org.iobserve.common.record.GeoLocation;

import kieker.common.util.registry.IRegistry;
import kieker.common.util.Version;

import kieker.common.record.flow.AbstractEvent;

/**
 * @author Generic Kieker
 * 
 * @since 1.12
 */
public class ServerGeoLocation extends AbstractEvent implements GeoLocation {
	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // AbstractEvent.timestamp
			 + TYPE_SIZE_SHORT // GeoLocation.countryCode
			 + TYPE_SIZE_STRING // ServerGeoLocation.hostname
			 + TYPE_SIZE_STRING // ServerGeoLocation.address
	;
	private static final long serialVersionUID = 2607620721245961693L;
	
	public static final Class<?>[] TYPES = {
		long.class, // AbstractEvent.timestamp
		short.class, // GeoLocation.countryCode
		String.class, // ServerGeoLocation.hostname
		String.class, // ServerGeoLocation.address
	};
	
	/* user-defined constants */
	/* default constants */
	public static final short COUNTRY_CODE = 49;
	public static final String HOSTNAME = "";
	public static final String ADDRESS = "";
	/* property declarations */
	private final short countryCode;
	private final String hostname;
	private final String address;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param countryCode
	 *            countryCode
	 * @param hostname
	 *            hostname
	 * @param address
	 *            address
	 */
	public ServerGeoLocation(final long timestamp, final short countryCode, final String hostname, final String address) {
		super(timestamp);
		this.countryCode = countryCode;
		this.hostname = hostname == null?"":hostname;
		this.address = address == null?"":address;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public ServerGeoLocation(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
		this.countryCode = (Short) values[1];
		this.hostname = (String) values[2];
		this.address = (String) values[3];
	}
	
	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected ServerGeoLocation(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this.countryCode = (Short) values[1];
		this.hostname = (String) values[2];
		this.address = (String) values[3];
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
	public ServerGeoLocation(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
		this.countryCode = buffer.getShort();
		this.hostname = stringRegistry.get(buffer.getInt());
		this.address = stringRegistry.get(buffer.getInt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getCountryCode(),
			this.getHostname(),
			this.getAddress()
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getHostname());
		stringRegistry.get(this.getAddress());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferOverflowException {
		buffer.putLong(this.getTimestamp());
		buffer.putShort(this.getCountryCode());
		buffer.putInt(stringRegistry.get(this.getHostname()));
		buffer.putInt(stringRegistry.get(this.getAddress()));
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

	public final short getCountryCode() {
		return this.countryCode;
	}
	
	public final String getHostname() {
		return this.hostname;
	}
	
	public final String getAddress() {
		return this.address;
	}
	
}
