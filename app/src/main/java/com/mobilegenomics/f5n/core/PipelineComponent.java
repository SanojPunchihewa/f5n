package com.mobilegenomics.f5n.core;

interface runNative {

    int run();
}

public class PipelineComponent implements runNative {

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
        status = NativeCommands.getNativeInstance().init(command,this.pipelineStep.getValue());
        return status;
    }
}