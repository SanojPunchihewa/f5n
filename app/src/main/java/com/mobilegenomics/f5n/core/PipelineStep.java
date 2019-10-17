package com.mobilegenomics.f5n.core;

public enum PipelineStep {
    //    MINIMAP2_SEQUENCE_ALIGNMENT(0,
//        "minimap2 -x map-ont -a /mnt/sdcard/f5c/test/ecoli_2kb_region/draft.fa /mnt/sdcard/f5c/test/ecoli_2kb_region/reads.fasta"),
    MINIMAP2_SEQUENCE_ALIGNMENT(0, "minimap2 -ax map-ont"),
    SAMTOOL_SORT(1, "samtool sort"),
    SAMTOOL_INDEX(2, "samtool index"),
    F5C_INDEX(3, "f5c index"),
    F5C_CALL_METHYLATION(4, "f5c call-methylation --secondary=yes --min-mapq=0 -B 2M"),
    F5C_EVENT_ALIGNMENT(5, "f5c eventalign");

    private final int value;

    private final String command;

    PipelineStep(int value, String command) {
        this.value = value;
        this.command = command;
    }

    public int getValue() {
        return value;
    }

    public String getCommand() {
        return command;
    }
}
