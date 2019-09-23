package com.mobilegenomics.f5n;

public class PipelineComponent implements runNative {

    PipelineStep pipelineStep;

    private String command;

    public PipelineComponent(PipelineStep pipelineStep, String command) {
        this.pipelineStep = pipelineStep;
        this.command = command;
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

interface runNative {

    int run();
}