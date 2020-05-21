package com.mobilegenomics.genopo.core;

import java.util.Arrays;

public class MethylationPipelineStep extends PipelineStep {

    private static final PipelineStep f5cIndex = new PipelineStep(F5C_INDEX, "F5C_INDEX", "f5c index");

    private static final PipelineStep f5cCallMethylation = new PipelineStep(F5C_CALL_METHYLATION,
            "F5C_CALL_METHYLATION",
            "f5c call-methylation");

    private static final PipelineStep f5cEventAlign = new PipelineStep(F5C_EVENT_ALIGNMENT, "F5C_EVENT_ALIGNMENT",
            "f5c eventalign");

    private static final PipelineStep f5cMethylationFreq = new PipelineStep(F5C_METH_FREQ, "F5C_METH_FREQ",
            "f5c meth-freq");

    public MethylationPipelineStep() {

    }

    @Override
    public PipelineStep[] values() {
        PipelineStep[] common = super.values();
        PipelineStep[] f5cSteps = new PipelineStep[]{f5cIndex, f5cCallMethylation, f5cEventAlign, f5cMethylationFreq};
        PipelineStep[] merged = Arrays.copyOf(common, common.length + f5cSteps.length);
        System.arraycopy(f5cSteps, 0, merged, common.length, f5cSteps.length);
        return merged;
    }

}
