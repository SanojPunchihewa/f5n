package com.mobilegenomics.f5n.core;

import android.util.Log;

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
        try{
            status = NativeCommands.getNativeInstance().init(command,this.pipelineStep.getValue());
        }catch (Exception e){
            status =1;
        }
        return status;
    }
}