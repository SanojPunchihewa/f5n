package com.mobilegenomics.genopo.core;

public class SingleToolPipelineStep extends PipelineStep {

    private static final PipelineStep artic = new PipelineStep(ARTIC, "ARTIC",
            "artic");

    private static final PipelineStep bcftools = new PipelineStep(BCFTOOLS, "BCFTOOLS",
            "bcftools");

    private static final PipelineStep bioawk = new PipelineStep(BIOAWK, "BIOAWK",
            "bioawk");

    public SingleToolPipelineStep() {

    }

    @Override
    public PipelineStep[] values() {
        return new PipelineStep[]{artic, bcftools, bioawk};
    }

}
