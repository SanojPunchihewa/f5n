package com.mobilegenomics.f5n;

public enum PipelineStep {
    MINIMAP2_SEQUENCE_ALIGNMENT(0),
    SAMTOOL_SORT(1),
    SAMTOOL_INDEX(2),
    F5C_INDEX(3),
    F5C_CALL_METHYLATION(4),
    F5C_EVENT_ALIGNMENT(5);

    private final int value;

    private PipelineStep(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
