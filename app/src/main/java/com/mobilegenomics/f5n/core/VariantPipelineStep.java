package com.mobilegenomics.f5n.core;

import java.util.Arrays;

public class VariantPipelineStep extends PipelineStep {

    private static final PipelineStep nanopolishIndex = new PipelineStep(NANOPOLISH_INDEX, "NANOPOLISH_INDEX",
            "nanopolish index");

    private static final PipelineStep nanopolishVariant = new PipelineStep(NANOPOLISH_VARIANT, "NANOPOLISH_VARIANT",
            "nanopolish variants");

    public VariantPipelineStep() {

    }

    @Override
    public PipelineStep[] values() {
        PipelineStep[] common = super.values();
        PipelineStep[] f5cSteps = new PipelineStep[]{nanopolishIndex, nanopolishVariant};
        PipelineStep[] merged = Arrays.copyOf(common, common.length + f5cSteps.length);
        System.arraycopy(f5cSteps, 0, merged, common.length, f5cSteps.length);
        return merged;
    }

}
