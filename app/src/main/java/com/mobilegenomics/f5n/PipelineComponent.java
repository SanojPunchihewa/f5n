package com.mobilegenomics.f5n;

public class PipelineComponent implements runNative {

    public PipelineStep getPipelineStep() {
        return pipelineStep;
    }

    private PipelineStep pipelineStep;

    private String command;

    public String getCommand() {
        return command;
    }

    private String runtime;

    public PipelineComponent(PipelineStep pipelineStep, String command) {
        this.pipelineStep = pipelineStep;
        this.command = command;
    }

    public String getRuntime() {
        return runtime;
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

    public void setRuntime(final String runtime) {
        this.runtime = runtime;
    }
}

interface runNative {

    int run();
}