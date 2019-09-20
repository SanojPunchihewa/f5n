package com.mobilegenomics.f5n;

import android.util.Log;
import java.util.ArrayList;

public class Step {

    private int argumentsCount;

    private ArrayList<Argument> arguments;

    private PipelineStep stepName;

    public PipelineStep getStepName() {
        return stepName;
    }

    public void setStep(final PipelineStep stepName) {
        this.stepName = stepName;
        configureArguments();
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    private void configureArguments() {
        this.arguments = new ArrayList<>();
        switch (this.stepName) {
            case MINIMAP2_SEQUENCE_ALIGNMENT:
                this.argumentsCount = 3;
                this.minimap2Arguments();
                break;
            case SAMTOOL_SORT:
                this.argumentsCount = 2;
                this.samtoolSortArguments();
                break;
            case SAMTOOL_INDEX:
                this.argumentsCount = 2;
                this.samtoolIndexArguments();
                break;
            case F5C_INDEX:
                this.argumentsCount = 2;
                this.f5cIndexArguments();
                break;
            case F5C_CALL_METHYLATION:
                this.argumentsCount = 2;
                this.f5cCallMethylationArguments();
                break;
            case F5C_EVENT_ALIGNMENT:
                this.argumentsCount = 2;
                this.f5cEventAlignArguments();
                break;
            default:
                Log.e("STEP", "cannot come here");
                break;
        }
    }

    private void minimap2Arguments() {
        Argument argument = new Argument("reference index", null, "Path to the reference index file", false, null);
        arguments.add(argument);
        argument = new Argument("query sequence", null, "Path to the query sequence file", false, null);
        arguments.add(argument);
        argument = new Argument("output file", null, "Path to the output sam file", true, "-o");
        arguments.add(argument);
    }

    private void samtoolSortArguments() {
        Argument argument = new Argument("input sam", null, "Path to the input index sam file", false, null);
        arguments.add(argument);
        argument = new Argument("output bam", null, "Path to the output bam file", true, "-o");
        arguments.add(argument);
    }

    // TODO
    private void samtoolIndexArguments() {
        Argument argument = new Argument("input sam", null, "Path to the input index sam file", false, null);
        arguments.add(argument);
        argument = new Argument("output bam", null, "Path to the output bam file", true, "-o");
        arguments.add(argument);
    }

    // TODO
    private void f5cIndexArguments() {
        Argument argument = new Argument("input sam", null, "Path to the input index sam file", false, null);
        arguments.add(argument);
        argument = new Argument("output bam", null, "Path to the output bam file", true, "-o");
        arguments.add(argument);
    }

    // TODO
    private void f5cCallMethylationArguments() {
        Argument argument = new Argument("input sam", null, "Path to the input index sam file", false, null);
        arguments.add(argument);
        argument = new Argument("output bam", null, "Path to the output bam file", true, "-o");
        arguments.add(argument);
    }

    // TODO
    private void f5cEventAlignArguments() {
        Argument argument = new Argument("input sam", null, "Path to the input index sam file", false, null);
        arguments.add(argument);
        argument = new Argument("output bam", null, "Path to the output bam file", true, "-o");
        arguments.add(argument);
    }
}
