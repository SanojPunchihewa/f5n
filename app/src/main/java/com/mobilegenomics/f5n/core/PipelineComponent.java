package com.mobilegenomics.f5n.core;

import java.io.Serializable;

interface runNative {

    int run();
}

public class PipelineComponent implements runNative, Serializable {

    private static final long serialVersionUID = 0L;
    private PipelineStep pipelineStep;
    private String command;
    private String runtime;

    public PipelineComponent(PipelineStep pipelineStep, String command) {
        this.pipelineStep = pipelineStep;
        this.command = command;
    }

    public PipelineStep getPipelineStep() {
        return pipelineStep;
    }

    public String getCommand() {
        return command;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(final String runtime) {
        this.runtime = runtime;
    }

    @Override
    public int run() {
        int status;
        if (this.pipelineStep.getValue() < 1) {
            // minimap2
            status = NativeCommands.getNativeInstance().initminimap2(command);
        } else if (this.pipelineStep.getValue() < 3) {
            // samtools
            status = NativeCommands.getNativeInstance().initsamtool(command);
        } else {
            // f5c
            status = NativeCommands.getNativeInstance().init(command);
        }
        return status;
    }
}