package com.mobilegenomics.f5n;

import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;

public class Step {

    private int argumentsCount;

    private ArrayList<Argument> arguments;

    private PipelineStep stepName;

    private StringBuilder command;

    public PipelineStep getStep() {
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
        Argument argument = new Argument(ArgumentID.MINIMAP2_REF_FILE, true, "reference index", null, null,
                "Path to the reference index file",
                false, null, false,
                true, new Pair<>(PipelineStep.F5C_CALL_METHYLATION, ArgumentID.F5C_METH_REF_FILE));
        arguments.add(argument);
        argument = new Argument(ArgumentID.MINIMAP2_QUERY_FILE, true, "query sequence", null, null,
                "Path to the query sequence file",
                false, null, false,
                true, new Pair<>(PipelineStep.F5C_INDEX, ArgumentID.F5C_INDEX_FASTA_FILE));
        arguments.add(argument);
        argument = new Argument(ArgumentID.MINIMAP2_OUTPUT_FILE, true, "output file", null, "minimap2.out.sam",
                "Path to the output sam file",
                true, "-o", false,
                true, new Pair<>(PipelineStep.SAMTOOL_SORT, ArgumentID.SAMTOOL_SORT_INPUT_FILE));
        arguments.add(argument);
        argument = new Argument(false, "Output format SAM", null,
                "Generate CIGAR and output alignments in SAM ", true, "-a", true);
        arguments.add(argument);
        argument = new Argument(false, "Output format PAF", null,
                "Generate CIGAR and output alignments in PAF format", true, "-c", true);
        arguments.add(argument);
//        argument = new Argument(true, "Preset", "map-ont",
//                "Preset type (map-ont, map-pb, splice, ...)", true, "-x", false);
//        arguments.add(argument);
        argument = new Argument(false, "Threads", "3",
                "Number of threads [3]", true, "-t", false);
        arguments.add(argument);
        argument = new Argument(false, "Bases loaded into memory", "500M",
                "Number of bases loaded into memory[500M]", true, "-K", false);
        arguments.add(argument);
    }

    private void samtoolSortArguments() {
        Argument argument = new Argument(ArgumentID.SAMTOOL_SORT_INPUT_FILE, true, "input sam", null, null,
                "Path to the input sam file",
                false, null, false,
                true, null);
        arguments.add(argument);
        argument = new Argument(ArgumentID.SAMTOOL_SORT_OUTPUT_FILE, true, "output file", null, "samtool.sorted.bam",
                "Path to the output file",
                true, "-o", false,
                true,
                new Pair<>(PipelineStep.SAMTOOL_INDEX, ArgumentID.SAMTOOL_INDEX_INPUT_FILE));
        arguments.add(argument);
        argument = new Argument(false, "max memory", "768M",
                "max memory per thread; suffix K/M/G recognized",
                true, "-m", false);
        arguments.add(argument);
        argument = new Argument(false, "threads", null, "Number of threads[1]", true, "-@", false);
        arguments.add(argument);
    }

    private void samtoolIndexArguments() {
        Argument argument = new Argument(ArgumentID.SAMTOOL_INDEX_INPUT_FILE, true, "input bam", null, null,
                "Path to the input bam file",
                false, null, false,
                true, new Pair<>(PipelineStep.F5C_CALL_METHYLATION, ArgumentID.F5C_METH_SORTED_FILE));
        arguments.add(argument);
        argument = new Argument(false, "threads", null, "Number of threads[1]", true, "-@", false);
        arguments.add(argument);
    }

    private void f5cIndexArguments() {
        //Build an index mapping from basecalled reads to the signals measured by the sequencer (same as nanopolish index)
        Argument argument = new Argument(ArgumentID.F5C_INDEX_FAST5_FILE, true, "fast5 files", null, null,
                "path to the directory containing the raw ONT signal files. This option can be given multiple times.",
                true, "--directory", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_INDEX_FASTA_FILE, true, "fast(a or q) reads", null, "fast5_files",
                "Path to the reads.fasta or fastq file",
                false, null, false,
                true, new Pair<>(PipelineStep.F5C_CALL_METHYLATION, ArgumentID.F5C_METH_FASTA_FILE));
        arguments.add(argument);
        argument = new Argument(false, "summary file", null,
                "the sequencing summary file from albacore, providing this option will make indexing much faster",
                true, "--sequencing-summary", false);
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_INDEX_SUMMARY_FILE, false, "paths to summary files", null, null,
                "file containing the paths to the sequencing summary files (one per line)",
                true, "--summary-fofn", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(false, "verbose", null,
                "display verbose output",
                true, "--verbose", true);
        arguments.add(argument);

    }

    private void f5cCallMethylationArguments() {
        //Classify nucleotides as methylated or not (optimised nanopolish call-methylation)
        Argument argument = new Argument(ArgumentID.F5C_METH_FASTA_FILE, true, "fastq/fasta read file", null, null,
                "fastq/fasta read file",
                true, "-r", false,
                true, new Pair<>(PipelineStep.F5C_EVENT_ALIGNMENT, ArgumentID.F5C_ALIGN_FASTA_FILE));
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_METH_SORTED_FILE, true, "sorted bam file", null, null,
                "sorted bam file",
                true, "-b", false,
                true, new Pair<>(PipelineStep.F5C_EVENT_ALIGNMENT, ArgumentID.F5C_ALIGN_SORTED_FILE));
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_METH_REF_FILE, true, "reference genome", null, null,
                "reference genome",
                true, "-g", false,
                true, new Pair<>(PipelineStep.F5C_EVENT_ALIGNMENT, ArgumentID.F5C_ALIGN_REF_FILE));
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_METH_OUTPUT_FILE, true, "output tsv file", null,
                "f5c-call-methylation.tsv",
                "tsv file from call-methylation",
                true, "-o", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(false, "number of threads", null, "number of threads [8]", true, "-t", false);
        arguments.add(argument);
        argument = new Argument(false, "batch size", null,
                "batch size (max number of reads loaded at once) [512]", true, "-K", false);
        arguments.add(argument);
        argument = new Argument(false, "max Bases", null,
                "max number of bases loaded at once [2.0M]", true, "-B", false);
        arguments.add(argument);
        argument = new Argument(false, "help", null,
                "help", true, "-h", true);
        arguments.add(argument);
        argument = new Argument(false, "min map quality", null,
                "minimum mapping quality [30]", true, "--min-mapq", false);
        arguments.add(argument);
        argument = new Argument(false, "consider secondary map", null,
                "consider secondary mappings or not [no]", true, "--secondary=", false);
        arguments.add(argument);
        argument = new Argument(false, "skip unreadable or terminate", null,
                "skip any unreadable fast5 or terminate program [yes]", true, "--skip-unreadable=", false);
        arguments.add(argument);
        argument = new Argument(false, "verbosity", null,
                "verbosity level [0]", true, "--verbose", false);
        arguments.add(argument);
        argument = new Argument(false, "version", null,
                "print version", true, "--version", true);
        arguments.add(argument);
    }

    private void f5cEventAlignArguments() {

        //Align nanopore events to reference k-mers (optimised nanopolish eventalign)
        Argument argument = new Argument(ArgumentID.F5C_ALIGN_FASTA_FILE, true, "fastq/fasta read file", null, null,
                "fastq/fasta read file",
                true, "-r", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_ALIGN_SORTED_FILE, true, "sorted bam file", null, null,
                "sorted bam file",
                true, "-b", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_ALIGN_REF_FILE, true, "reference genome", null, null,
                "reference genome",
                true, "-g", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(ArgumentID.F5C_ALIGN_OUTPUT_FILE, true, "output file", null, "f5c-event-align.txt",
                "path to output summary",
                true, "-o", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(false, "threads", "8",
                "Number of threads[8]", true, "-t", false);
        arguments.add(argument);
        argument = new Argument(false, "batch size", "512",
                "max number of reads loaded at once[512]", true, "-K", false);
        arguments.add(argument);
        argument = new Argument(false, "bases loaded at once[K / M / G]", "2.0M",
                "max number of bases loaded at once [2.0M]", true, "-B", false);
        arguments.add(argument);
    }

    public void buildCommandString() {
        command = new StringBuilder(stepName.getCommand());
        for (Argument argument : arguments) {
            command.append(" ");
            command.append(argument.toString());
        }
    }

    public String getCommandString() {
        return command.toString();
    }

    public Argument findArgumentById(int argId) {
        for (Argument argument : arguments) {
            if (argument.getArgID() == argId) {
                return argument;
            }
        }
        return null;
    }
}
