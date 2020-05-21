package com.mobilegenomics.genopo.core;

import java.util.Arrays;

public class ArticPipelineStep extends PipelineStep {

    private static final PipelineStep articTrim = new PipelineStep(ARTIC_TRIM, "ARTIC_TRIM",
            "artic trim");

    private static final PipelineStep samtoolsSort = new PipelineStep(SAMTOOLS_SORT, "SAMTOOLS_SORT",
            "samtools sort");

    private static final PipelineStep samotoolsIndex = new PipelineStep(SAMTOOLS_INDEX, "SAMTOOLS_INDEX",
            "samtools index");

    private static final PipelineStep nanopolishIndex = new PipelineStep(NANOPOLISH_INDEX, "NANOPOLISH_INDEX",
            "nanopolish index");

    private static final PipelineStep nanopolishVariant = new PipelineStep(NANOPOLISH_VARIANT, "NANOPOLISH_VARIANT",
            "nanopolish variants");

    private static final PipelineStep bcftoolsReheader = new PipelineStep(BCFTOOLS_REHEADER, "BCFTOOLS_REHEADER",
            "bcftools reheader");

    private static final PipelineStep bcftoolsConcat = new PipelineStep(BCFTOOLS_CONCAT, "BCFTOOLS_CONCAT",
            "bcftools concat");

    private static final PipelineStep bcftoolsView = new PipelineStep(BCFTOOLS_VIEW, "BCFTOOLS_VIEW",
            "bcftools view");

    private static final PipelineStep bcftoolsIndex = new PipelineStep(BCFTOOLS_INDEX, "BCFTOOLS_INDEX",
            "bcftools index");

    private static final PipelineStep bcftoolsConsensus = new PipelineStep(BCFTOOLS_CONSENSUS, "BCFTOOLS_CONSENSUS",
            "bcftools consensus");

    public ArticPipelineStep() {

    }

    @Override
    public PipelineStep[] values() {
        PipelineStep[] common = super.values();
        PipelineStep[] articSteps = new PipelineStep[]{articTrim, samtoolsSort, samotoolsIndex, nanopolishIndex,
                nanopolishVariant, bcftoolsReheader,
                bcftoolsConcat, bcftoolsView, bcftoolsIndex, bcftoolsConsensus};
        PipelineStep[] merged = Arrays.copyOf(common, common.length + articSteps.length);
        System.arraycopy(articSteps, 0, merged, common.length, articSteps.length);
        return merged;
    }

}
