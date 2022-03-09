package org.palladiosimulator.pmxupgrade.model.systemmodel.trace;

/**
 * Represents the external calls for the individual traces..
 *
 * @author Patrick Treyer
 */
public class ExternalCall {

    private String className;
    private String methodName;
    private double numAverageCalls;

    public ExternalCall(String component,
                        String operation, double numCalls) {
        this.setClassName(component);
        this.setMethodName(operation);
        this.setNumCalls(numCalls);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public double getNumCalls() {
        return numAverageCalls;
    }

    public void setNumCalls(double numCalls) {
        this.numAverageCalls = numCalls;
    }

}
