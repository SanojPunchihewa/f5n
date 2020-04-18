package com.mobilegenomics.f5n.core;

import java.util.Arrays;

public class VariantPipelineStep extends PipelineStep {

    private static final PipelineStep nanopolishIndex = new PipelineStep(NANOPOLISH_INDEX, "NANOPOLISH_INDEX",
            "nanopolish index");

    private static final PipelineStep nanopolishVariant = new PipelineStep(NANOPOLISH_VARIANT, "NANOPOLISH_VARIANT",
            "nanopolish variants");

    private static final PipelineStep bcftoolsReheader = new PipelineStep(BCFTOOLS_REHEADER, "BCFTOOLS_REHEADER",
            "nanopolish reheader");

    private static final PipelineStep bcftoolsConcat = new PipelineStep(BCFTOOLS_CONCAT, "BCFTOOLS_CONCAT",
            "nanopolish concat");

    private static final PipelineStep bcftoolsView = new PipelineStep(BCFTOOLS_VIEW, "BCFTOOLS_VIEW",
            "nanopolish view");

    private static final PipelineStep bcftoolsIndex = new PipelineStep(BCFTOOLS_INDEX, "BCFTOOLS_INDEX",
            "nanopolish index");

    private static final PipelineStep bcftoolsConsensus = new PipelineStep(BCFTOOLS_CONSENSUS, "BCFTOOLS_CONSENSUS",
            "nanopolish consensus");

    public VariantPipelineStep() {

    }

    @Override
    public PipelineStep[] values() {
        PipelineStep[] common = super.values();
        PipelineStep[] nanopolishSteps = new PipelineStep[]{nanopolishIndex, nanopolishVariant, bcftoolsReheader,
                bcftoolsConcat, bcftoolsView, bcftoolsIndex, bcftoolsConsensus};
        PipelineStep[] merged = Arrays.copyOf(common, common.length + nanopolishSteps.length);
        System.arraycopy(nanopolishSteps, 0, merged, common.length, nanopolishSteps.length);
        return merged;
    }

}
