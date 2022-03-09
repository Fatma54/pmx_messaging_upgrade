package org.palladiosimulator.pmxupgrade.logic.dataprocessing.controlflow.graph;


import org.palladiosimulator.pmxupgrade.model.graph.AbstractVertexDecoration;
import org.palladiosimulator.pmxupgrade.model.systemmodel.trace.Execution;

import java.util.concurrent.TimeUnit;

/**
 * Response time decoration for graph vertices. This decoration extracts response times from executions and keeps track of the minimal, maximal and average response
 * time.
 * 
 * @author PMX, Universitaet Wuerzburg.
 * 
 * @since 1.5
 */
public class ResponseTimeDecoration extends AbstractVertexDecoration {

	private final TimeUnit executionTimeunit;
	private final TimeUnit displayTimeunit;
	private final String timeUnitShortname;

	private long responseTimeSum;
	private int executionCount;
	private long minimalResponseTime = Integer.MAX_VALUE;
	private long maximalResponseTime;

	/**
	 * Creates a new response time decoration.
	 * 
	 * @param executionTimeunit
	 *            The time unit which tells how to interpret the times of the executions.
	 */
	public ResponseTimeDecoration(final TimeUnit executionTimeunit, final TimeUnit displayTimeunit) {
		this.executionTimeunit = executionTimeunit;
		this.displayTimeunit = displayTimeunit;
		switch (displayTimeunit) {
		case NANOSECONDS:
			this.timeUnitShortname = "ns";
			break;
		case MICROSECONDS:
			this.timeUnitShortname = "us";
			break;
		case MILLISECONDS:
			this.timeUnitShortname = "ms";
			break;
		case SECONDS:
			this.timeUnitShortname = "s";
			break;
		default:
			this.timeUnitShortname = "??";
			break;
		}
	}

	/**
	 * Registers a given execution for the decorated vertex.
	 * 
	 * @param execution
	 *            The execution to register
	 */
	public void registerExecution(final Execution execution) {
		final long responseTime = this.displayTimeunit.convert(execution.getTout() - execution.getTin(), this.executionTimeunit);

		this.responseTimeSum = this.responseTimeSum + responseTime;
		this.executionCount++;

		if (responseTime < this.minimalResponseTime) {
			this.minimalResponseTime = responseTime;
		}
		if (responseTime > this.maximalResponseTime) {
			this.maximalResponseTime = responseTime;
		}
	}

	/**
	 * Returns the minimal response time (in ms) registered by this decoration.
	 * 
	 * @return See above
	 */
	public long getMinimalResponseTime() {
		return this.minimalResponseTime;
	}

	/**
	 * Returns the maximal response time (in ms) registered by this decoration.
	 * 
	 * @return See above
	 */
	public long getMaximalResponseTime() {
		return this.maximalResponseTime;
	}

	/**
	 * Returns the average response time (in ms) registered by this decoration.
	 * 
	 * @return See above
	 */
	public double getAverageResponseTime() {
		return (this.executionCount == 0) ? 0 : ((double) this.responseTimeSum / (double) this.executionCount); // NOCS (inline ?)
	}

	public long getTotalResponseTime() {
		return this.responseTimeSum;
	}

	@Override
	public String createFormattedOutput() {
		final StringBuilder sb = new StringBuilder(30);
		sb.append("min: ");
		sb.append(this.getMinimalResponseTime());
		sb.append(this.timeUnitShortname);
		sb.append(", avg: ");
		sb.append(Math.round(this.getAverageResponseTime()));
		sb.append(this.timeUnitShortname);
		sb.append(", max: ");
		sb.append(this.getMaximalResponseTime());
		sb.append(this.timeUnitShortname);
		sb.append(",\\ntotal: ");
		sb.append(this.getTotalResponseTime());
		sb.append(this.timeUnitShortname);
		return sb.toString();
	}

}
