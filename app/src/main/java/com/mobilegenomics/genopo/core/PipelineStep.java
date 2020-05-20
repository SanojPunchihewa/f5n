package com.mobilegenomics.genopo.core;

interface EnumHelper {

    String getName();

    String getCommand();

    int getValue();

    PipelineStep[] values();

}

public class PipelineStep implements EnumHelper {

    // Use 0-9 for minimap2 subcommands
    public static final int MINIMAP2_SEQUENCE_ALIGNMENT = 0;

    // Use 10-19 for samtools subcommands
    public static final int SAMTOOLS_SORT = 10;

    public static final int SAMTOOLS_INDEX = 11;

    public static final int SAMTOOLS_DEPTH = 12;

    // Use 20-29 for f5c subcommands
    public static final int F5C_INDEX = 20;

    public static final int F5C_CALL_METHYLATION = 21;

    public static final int F5C_EVENT_ALIGNMENT = 22;

    public static final int F5C_METH_FREQ = 23;

    // Use 30-39 for nanopolish subcommands
    public static final int NANOPOLISH_INDEX = 30;

    public static final int NANOPOLISH_VARIANT = 31;

    // Use 40-49 for artic subcommands
    public static final int ARTIC_TRIM = 40;

    public static final int ARTIC_MASK = 41;

    public static final int ARTIC_MULTIINTER = 42;

    public static final int ARTIC = 49;

    // Use 50-59 for bcftools subcommands
    public static final int BCFTOOLS_CONSENSUS = 50;

    public static final int BCFTOOLS_CONCAT = 51;

    public static final int BCFTOOLS_REHEADER = 52;

    public static final int BCFTOOLS_VIEW = 53;

    public static final int BCFTOOLS_INDEX = 54;

    public static final int BCFTOOLS_QUERY = 55;

    public static final int BCFTOOLS = 59;

    // Use 60-69 for bioawk subcommands
    public static final int BIOAWK = 69;

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
