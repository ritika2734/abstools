/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.java.lib.types;

public class ABSProcess extends ABSBuiltInDataType {

    // set: pid, method, arrival, cost, deadline, start, finish, critical, value

    private long pid;
    private String methodName;
    private int arrivalTime;
    private int cost;
    private int deadline;
    private int startTime;
    private int finishTime;
    private boolean critical;
    private int value;
    
    public ABSProcess() {
        super("ABSProcess");
    }

    public ABSProcess(long pid, String method, int arrival, int cost, int deadline, int start, int finish, boolean crit, int value) {
        super("ABSProcess");
        this.pid = pid;
        this.methodName = method;
        this.arrivalTime = arrival;
        this.cost = cost;
        this.deadline = deadline;
        this.startTime = start;
        this.finishTime = finish;
        this.critical = crit;
        this.value = value;
    }
    
    public long getPid() {
        return pid;
    }

    public String getMethodName() {
        return methodName;
    }
    
    public int getArrivalTime() {
        return arrivalTime;
    }
    
    public String toString() {
        return String.format("%1$s: %2$d,%3$s,%4$d,%5$d,%6$d,%7$d,%8$d,%9$b,%10$d", 
                getConstructorName(), pid, methodName, arrivalTime, cost, deadline, startTime, finishTime, critical, value);
    }
    

}