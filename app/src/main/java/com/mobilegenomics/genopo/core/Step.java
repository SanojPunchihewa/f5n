package com.mobilegenomics.genopo.core;

import java.io.Serializable;
import java.util.ArrayList;

public class Step implements Serializable {

    private ArrayList<Argument> arguments;

    private PipelineStep stepName;

    private StringBuilder commandBuilder;

    private String command;

    private boolean isSelected;

    public Step(final PipelineStep stepName, final String command) {
        this.stepName = stepName;
        this.command = command;
        this.isSelected = true;
    }

    public Step(final PipelineStep stepName, final ArrayList<Argument> arguments) {
        this.stepName = stepName;
        this.arguments = arguments;
        this.isSelected = true;
        buildCommandString();
    }

    public PipelineStep getStep() {
        return stepName;
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public void buildCommandString() {
        commandBuilder = new StringBuilder(stepName.getCommand());
        for (Argument argument : arguments) {
            commandBuilder.append(" ");
            commandBuilder.append(argument.toString());
        }
        command = commandBuilder.toString();
    }

    public String getCommandString() {
        return command;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setCommandString(final String command) {
        this.command = command;
    }

    public void setSelected(final boolean selected) {
        isSelected = selected;
    }
}
