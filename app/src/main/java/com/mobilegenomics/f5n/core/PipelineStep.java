package com.mobilegenomics.f5n.core;

interface EnumHelper {

    String getName();

    String getCommand();

    int getValue();

    PipelineStep[] values();

}

public class PipelineStep implements EnumHelper {

    public static final int MINIMAP2_SEQUENCE_ALIGNMENT = 0;

    public static final int SAMTOOLS_SORT = 1;

    public static final int SAMTOOLS_INDEX = 2;

    public static final int F5C_INDEX = 3;

    public static final int F5C_CALL_METHYLATION = 4;

    public static final int F5C_EVENT_ALIGNMENT = 5;

    public static final int F5C_METH_FREQ = 6;

    public static final int NANOPOLISH_INDEX = 7;

    public static final int NANOPOLISH_VARIANT = 8;

    private final int value;

    private final String name;

    private final String command;

    private static final PipelineStep minimap2Align = new PipelineStep(MINIMAP2_SEQUENCE_ALIGNMENT,
            "MINIMAP2_SEQUENCE_ALIGNMENT",
            "minimap2 -x map-ont");

    private static final PipelineStep samtoolsSort = new PipelineStep(SAMTOOLS_SORT, "SAMTOOLS_SORT",
            "samtools sort");

    private static final PipelineStep samotoolsIndex = new PipelineStep(SAMTOOLS_INDEX, "SAMTOOLS_INDEX",
            "samtools index");

    PipelineStep() {
        value = -1;
        name = "";
        command = "";
    }

    PipelineStep(int value, String name, String command) {
        this.value = value;
        this.name = name;
        this.command = command;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public PipelineStep[] values() {
        return new PipelineStep[]{minimap2Align, samtoolsSort, samotoolsIndex};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((command == null) ? 0 : command.hashCode());
        result = prime * result + value;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PipelineStep other = (PipelineStep) obj;
        if (command == null) {
            if (other.command != null) {
                return false;
            }
        } else if (!command.equals(other.command)) {
            return false;
        }
        if (value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PipelineStep [value=" + value + ", name=" + name + ", command="
                + command + "]";
    }

}
