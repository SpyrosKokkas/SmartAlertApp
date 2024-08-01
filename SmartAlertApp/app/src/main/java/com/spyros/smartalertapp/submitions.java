package com.spyros.smartalertapp;

import java.util.Date;

public class submitions {

    private String DangerType;
    private Date currentTime;
    private int Priority;


    public submitions(){
        //Empty Constructor
    }

    public submitions( String type , Date currentTime , int priority){
        this.DangerType = type;
        this.currentTime = currentTime;
        this.Priority = priority;
    }

    public void setDangerType(String dangerType) {
        DangerType = dangerType;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public String getDangerType() {
        return DangerType;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }
}
