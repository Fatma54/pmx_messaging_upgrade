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
package tools.descartes.pmx.filter.usage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.analysis.plugin.filter.forward.TeeFilter;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.ExecutionTrace;
import kieker.tools.traceAnalysis.systemModel.ExecutionTraceBasedSession;

/**
 * Writes a session.dat file including the {@link ExecutionTraceBasedSession}s
 * received via the input port. And then analyzes via BehaviorModelExtractor of
 * Markov4JMeter.
 * 
 * Based on Kieker's {@link TeeFilter} and on Session2UsageProfileWriterPlugin.
 * 
 * @author J�rgen Walter
 * 
 * 
 */
@Plugin(description = "A filter to print the object to a configured stream", configuration = {
		@Property(name = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_NAME_STREAM, defaultValue = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_VALUE_STREAM_STDOUT, description = "The name of the stream used to print the incoming data (special values are STDOUT, STDERR, STDlog, and NULL; "
				+ "other values are interpreted as filenames)."),
		@Property(name = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_NAME_ENCODING, defaultValue = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_VALUE_DEFAULT_ENCODING, description = "The used encoding for the selected stream."),
		@Property(name = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_NAME_APPEND, defaultValue = Session2UsageProfileWriterPlugin.CONFIG_PROPERTY_VALUE_STREAM_APPEND, description = "Decides whether the filter appends to the stream in the case of a file or not.") })
public final class Session2UsageProfileWriterPlugin extends
		AbstractFilterPlugin {

	/** The name of the input port for incoming events. */
	public static final String INPUT_PORT_NAME_SESSIONS = "receivedEvents";
	/** The name of the output port delivering the incoming events. */
	public static final String CONFIG_PROPERTY_NAME_STREAM = "stream";
	/** The name of the property determining the used encoding. */
	public static final String CONFIG_PROPERTY_NAME_ENCODING = "characterEncoding";
	/**
	 * The name of the property determining whether or not the stream appends or
	 * overwrites to files
	 */
	public static final String CONFIG_PROPERTY_NAME_APPEND = "append";

	/**
	 * The value of the stream property which determines that the filter uses
	 * the standard output.
	 */
	public static final String CONFIG_PROPERTY_VALUE_STREAM_STDOUT = "STDOUT";
	/**
	 * The value of the stream property which determines that the filter uses
	 * the standard error output.
	 */
	public static final String CONFIG_PROPERTY_VALUE_STREAM_STDERR = "STDERR";
	/**
	 * The value of the stream property which determines that the filter uses
	 * the standard log.
	 */
	public static final String CONFIG_PROPERTY_VALUE_STREAM_STDLOG = "STDlog";
	/**
	 * The value of the stream property which determines that the filter doesn't
	 * print anything.
	 */
	public static final String CONFIG_PROPERTY_VALUE_STREAM_NULL = "NULL";
	/**
	 * The default value of the encoding property which determines that the
	 * filter uses utf-8.
	 */
	public static final String CONFIG_PROPERTY_VALUE_DEFAULT_ENCODING = "UTF-8";
	/**
	 * The default value of the stream property which determines that the filter
	 * appends or overwrites a file.
	 */
	public static final String CONFIG_PROPERTY_VALUE_STREAM_APPEND = "true";

	private final PrintStream printStream;
	private final String printStreamName;
	private final boolean active;
	private final boolean append;
	private final String encoding;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public Session2UsageProfileWriterPlugin(final Configuration configuration,
			final IProjectContext projectContext) {
		super(configuration, projectContext);

		// Get the name of the stream.
		final String printStreamNameConfig = this.configuration
				.getStringProperty(CONFIG_PROPERTY_NAME_STREAM);
		// Get the encoding.
		this.encoding = this.configuration
				.getStringProperty(CONFIG_PROPERTY_NAME_ENCODING);

		// Decide which stream to be used - but remember the name!
		if (CONFIG_PROPERTY_VALUE_STREAM_STDLOG.equals(printStreamNameConfig)) {
			this.printStream = null; // NOPMD (null)
			this.printStreamName = null; // NOPMD (null)
			this.active = true;
			this.append = false;
		} else if (CONFIG_PROPERTY_VALUE_STREAM_STDOUT
				.equals(printStreamNameConfig)) {
			this.printStream = System.out;
			this.printStreamName = null; // NOPMD (null)
			this.active = true;
			this.append = false;
		} else if (CONFIG_PROPERTY_VALUE_STREAM_STDERR
				.equals(printStreamNameConfig)) {
			this.printStream = System.err;
			this.printStreamName = null; // NOPMD (null)
			this.active = true;
			this.append = false;
		} else if (CONFIG_PROPERTY_VALUE_STREAM_NULL
				.equals(printStreamNameConfig)) {
			this.printStream = null; // NOPMD (null)
			this.printStreamName = null; // NOPMD (null)
			this.active = false;
			this.append = false;
		} else {
			this.append = configuration
					.getBooleanProperty(CONFIG_PROPERTY_NAME_APPEND);
			PrintStream tmpPrintStream;
			try {
				tmpPrintStream = new PrintStream(new FileOutputStream(
						printStreamNameConfig, this.append), false,
						this.encoding);
			} catch (final UnsupportedEncodingException ex) {
				this.log.error("Failed to initialize " + printStreamNameConfig,
						ex);
				tmpPrintStream = null; // NOPMD (null)
			} catch (final FileNotFoundException ex) {
				this.log.error("Failed to initialize " + printStreamNameConfig,
						ex);
				tmpPrintStream = null; // NOPMD (null)
			}
			this.printStream = tmpPrintStream;
			this.printStreamName = printStreamNameConfig;
			this.active = true;
		}
	}

	@Override
	public final void terminate(final boolean error) {
		if ((this.printStream != null) && (this.printStream != System.out)
				&& (this.printStream != System.err)) {
			this.printStream.close();
		}

//		log.info("starting behavior model extraction -----------------------------------------------------");
//		String[] cbme_args = { "--input", printStreamName, "--output",
//				printStreamName.replace("session.dat", ""), "--numberClusters",
//				"1", "--clustering", RBMToRBMUnifier.CLUSTERING_TYPE_NONE };
//		BehaviorModelExtractor.main(cbme_args);
//		String resultPath = printStreamName.replace("session.dat", ""); // +"/usage/"
//		// TODO transform results from beahvior model extractor to m4j-dsl ==
//		// WESSBAS
//		// CommandLineArgumentsHandler.; TODO check arguments
//		String[] m4j_args = { "--flows", resultPath, "--output",
//				resultPath + "workload.xmi", "--workloadIntensity",
//				"testFiles/workloadintensity.properties", "--behavior", "testFiles/behaviorModels.properties" };
//		M4jdslModelGenerator.main(m4j_args);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_ENCODING, this.encoding);
		configuration.setProperty(CONFIG_PROPERTY_NAME_APPEND,
				Boolean.toString(this.append));
		// We reverse the if-decisions within the constructor.
		if (this.printStream == null) {
			if (this.active) {
				configuration.setProperty(CONFIG_PROPERTY_NAME_STREAM,
						CONFIG_PROPERTY_VALUE_STREAM_STDLOG);
			} else {
				configuration.setProperty(CONFIG_PROPERTY_NAME_STREAM,
						CONFIG_PROPERTY_VALUE_STREAM_NULL);
			}
		} else if (this.printStream == System.out) {
			configuration.setProperty(CONFIG_PROPERTY_NAME_STREAM,
					CONFIG_PROPERTY_VALUE_STREAM_STDOUT);
		} else if (this.printStream == System.err) {
			configuration.setProperty(CONFIG_PROPERTY_NAME_STREAM,
					CONFIG_PROPERTY_VALUE_STREAM_STDERR);
		} else {
			configuration.setProperty(CONFIG_PROPERTY_NAME_STREAM,
					this.printStreamName);
		}
		return configuration;
	}

	/**
	 * 
	 * 
	 * @param t
	 * @return
	 */
	private String getUseCaseForTrace(ExecutionTrace t) {
		for (Execution e : t.getTraceAsSortedExecutionSet()) {
			// We extract the method name of the method with eoi/ess 2/2, e.g.,
			// org.spec.jent.servlet.helper.SpecServletAction.doVehicleQuotes(javax.servlet.ServletContext,
			// javax.servlet.http.HttpServletRequest,
			// javax.servlet.http.HttpServletResponse, java.lang.Integer,
			// java.lang.String, int)
			if (e.getEoi() == 0 && e.getEss() == 0) {
				return e.getOperation().getSignature().getName();
			}
		}
		return Long.toString(t.getTraceId());
	}

	/**
	 * This method is the input port of the filter receiving incoming session
	 * objects. Every session will be printed into a stream (based on the
	 * configuration).
	 * 
	 * @param session
	 *            The new session object.
	 */
	@InputPort(name = INPUT_PORT_NAME_SESSIONS, description = "Receives incoming session objects to be logged", eventTypes = { ExecutionTraceBasedSession.class })
	public final void inputEvent(final ExecutionTraceBasedSession session) {
		if (this.active) {
			final StringBuilder sb = new StringBuilder(128);
			sb.append(session.getSessionId());
			for (ExecutionTrace t : session.getContainedTraces()) {
				sb.append(";\"").append(getUseCaseForTrace(t)).append("\":")
						.append(t.getStartTimestamp()).append(":")
						.append(t.getEndTimestamp());
			}
			final String record = sb.toString();
			if (this.printStream != null) {
				this.printStream.println(record);
			} else {
				this.log.info(record);
			}
		}
	}

}
