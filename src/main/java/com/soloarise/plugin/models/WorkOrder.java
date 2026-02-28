package com.soloarise.plugin.models;

public class WorkOrder {
    
    private final String soulName;
    private final String order;
    private final long assignedTime;
    private boolean completed = false;
    
    public WorkOrder(String soulName, String order) {
        this.soulName = soulName;
        this.order = order;
        this.assignedTime = System.currentTimeMillis();
    }
    
    public String getSoulName() { return soulName; }
    public String getOrder() { return order; }
    public long getAssignedTime() { return assignedTime; }
    public boolean isCompleted() { return completed; }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
