package com.mobilegenomics.f5n.dto;

import com.mobilegenomics.f5n.core.Step;
import java.io.Serializable;
import java.util.ArrayList;

public class WrapperObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String prefix;

    private State state;

    private String clientIP;

    private String pathToDataDir;

    private String resultSummery;

    private Long releaseTime;

    private Long collectTime;

    private ArrayList<Step> steps = new ArrayList<>();

    public WrapperObject() {
    }

    public WrapperObject(String prefix, State state, String pathToDataDir, ArrayList<Step> steps) {
        this.prefix = prefix;
        this.state = state;
        this.pathToDataDir = pathToDataDir;
        this.steps = steps;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getPathToDataDir() {
        return pathToDataDir;
    }

    public void setPathToDataDir(String pathToDataDir) {
        this.pathToDataDir = pathToDataDir;
    }

    public String getResultSummery() {
        return resultSummery;
    }

    public void setResultSummery(String resultSummery) {
        this.resultSummery = resultSummery;
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }

    public Long getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Long releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Long collectTime) {
        this.collectTime = collectTime;
    }

    private String listToString(ArrayList<Step> steps) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Step step : steps) {
            stringBuilder.append(step.getCommandString());
            stringBuilder.append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{State=" + state);
        if (steps != null && !steps.isEmpty()) {
            stringBuilder.append(", Pipeline Steps=" + listToString(steps));
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private String listToStringPretty(ArrayList<Step> steps) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Step step : steps) {
            stringBuilder.append(step.getCommandString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringPretty() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append("#######################################\n");
        stringBuilder.append("State : " + state);
        stringBuilder.append("\nPipeline Steps : " + listToStringPretty(steps));
        stringBuilder.append("#######################################\n");
        return stringBuilder.toString();
    }
}
