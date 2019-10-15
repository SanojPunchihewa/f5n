package com.mobilegenomics.f5n.dto;

import com.mobilegenomics.f5n.core.PipelineComponent;

import java.io.Serializable;
import java.util.ArrayList;

public class WrapperObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String prefix;
    private State state;
    private String clientIP;
    private String pathToDataDir;
    private ArrayList<PipelineComponent> pipelineComponents = new ArrayList<>();

    public WrapperObject() {
    }

    public WrapperObject(String prefix, State state, String pathToDataDir, ArrayList<PipelineComponent> pipelineComponents) {
        this.prefix = prefix;
        this.state = state;
        this.pathToDataDir = pathToDataDir;
        this.pipelineComponents = pipelineComponents;
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

    public ArrayList<PipelineComponent> getPipelineComponents() {
        return pipelineComponents;
    }

    public void setPipelineComponents(ArrayList<PipelineComponent> pipelineComponents) {
        this.pipelineComponents = pipelineComponents;
    }

    public String listToString(ArrayList<PipelineComponent> pipelineComponentsList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (PipelineComponent pipelineComponents : pipelineComponentsList) {
            stringBuilder.append(pipelineComponents.toString());
            stringBuilder.append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{State=" + state);
        if (pipelineComponents != null && !pipelineComponents.isEmpty()) {
            stringBuilder.append(", Pipeline Components=" + listToString(pipelineComponents));
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public String listToStringPretty(ArrayList<PipelineComponent> pipelineComponentsList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (PipelineComponent pipelineComponent : pipelineComponentsList) {
            stringBuilder.append(pipelineComponent.getPipelineStep().name() + " " + pipelineComponent.getCommand());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringPretty() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append("#######################################\n");
        stringBuilder.append("State : " + state);
        stringBuilder.append("\nPipeline Components : " + listToStringPretty(pipelineComponents));
        stringBuilder.append("#######################################\n");
        return stringBuilder.toString();
    }
}
