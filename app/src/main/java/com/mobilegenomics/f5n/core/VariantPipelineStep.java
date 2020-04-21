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
        PipelineStep[] nanopolishSteps = new PipelineStep[]{nanopolishIndex, nanopolishVariant};
        PipelineStep[] merged = Arrays.copyOf(common, common.length + nanopolishSteps.length);
        System.arraycopy(nanopolishSteps, 0, merged, common.length, nanopolishSteps.length);
        return merged;
    }

}
