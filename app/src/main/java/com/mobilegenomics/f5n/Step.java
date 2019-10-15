package com.mobilegenomics.f5n;

import android.util.Log;
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
        Argument argument = new Argument("minimap2-ref", true, "reference index", null,
                "Path to the reference index file",
                false, null, false,
                true, "f5c-meth-ref");
        arguments.add(argument);
        argument = new Argument("minimap2-query", true, "query sequence", null, "Path to the query sequence file",
                false, null, false,
                true, "f5c-index-fasta");
        arguments.add(argument);
        argument = new Argument("minimap2-output", true, "output file", null, "Path to the output sam file",
                true, "-o", false,
                true, "samtool-sort-in");
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
        Argument argument = new Argument("samtool-sort-in", true, "input sam", null, "Path to the input sam file",
                false, null, false,
                true, null);
        arguments.add(argument);
        argument = new Argument("samtool-sort-out", true, "output file", null, "Path to the output file",
                true, "-o", false,
                true, "samtool-index");
        arguments.add(argument);
        argument = new Argument(false, "max memory", "768M", "max memory per thread; suffix K/M/G recognized",
                true,
                "-m", false);
        arguments.add(argument);
        argument = new Argument(false, "threads", null, "Number of threads[1]",
                true, "-@", false);
        arguments.add(argument);
    }

    private void samtoolIndexArguments() {
        Argument argument = new Argument("samtool-index", true, "input bam", null, "Path to the input bam file",
                false, null, false,
                true, "f5c-meth-sorted");
        arguments.add(argument);
        argument = new Argument(false, "threads", null, "Number of threads[1]",
                true, "-@", false);
        arguments.add(argument);
    }

    private void f5cIndexArguments() {
        //Build an index mapping from basecalled reads to the signals measured by the sequencer (same as nanopolish index)
        Argument argument = new Argument("f5c-index-fast5", true, "fast5 files", null,
                "path to the directory containing the raw ONT signal files. This option can be given multiple times.",
                true, "--directory", false,
                true, null);
        arguments.add(argument);
        argument = new Argument("f5c-index-fasta", true, "fast(a or q) reads", null,
                "Path to the reads.fasta or fastq file",
                false, null, false,
                true, "f5c-meth-fasta");
        arguments.add(argument);
        argument = new Argument(false, "summary file", null,
                "the sequencing summary file from albacore, providing this option will make indexing much faster",
                true, "--sequencing-summary", false);
        arguments.add(argument);
        argument = new Argument("f5c-index-summary", false, "paths to summary files", null,
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
        Argument argument = new Argument("f5c-meth-fasta", true, "fastq/fasta read file", null,
                "fastq/fasta read file",
                true, "-r", false,
                true, "f5c-align-fasta");
        arguments.add(argument);
        argument = new Argument("f5c-meth-sorted", true, "sorted bam file", null, "sorted bam file",
                true, "-b", false,
                true, "f5c-align-sorted");
        arguments.add(argument);
        argument = new Argument("f5c-meth-ref", true, "reference genome", null, "reference genome",
                true, "-g", false,
                true, "f5c-align-ref");
        arguments.add(argument);
        argument = new Argument("f5c-meth-summary", true, "output tsv file", null, "tsv file from call-methylation",
                true, "-o", false,
                true, null);
        arguments.add(argument);
        argument = new Argument(false, "number of threads", null, "number of threads [8]",
                true, "-t", false);
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
        Argument argument = new Argument("f5c-align-fasta", true, "fastq/fasta read file", null,
                "fastq/fasta read file",
                true, "-r", false,
                true, null);
        arguments.add(argument);
        argument = new Argument("f5c-align-sorted", true, "sorted bam", null, "path to sorted bam file",
                true, "-b", false,
                true, null);
        arguments.add(argument);
        argument = new Argument("f5c-align-ref", true, "reference genome", null, "path to reference genome",
                true, "-g", false,
                true, null);
        arguments.add(argument);
        argument = new Argument("f5c-align-summary", true, "output file", null, "path to output summary",
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
}
