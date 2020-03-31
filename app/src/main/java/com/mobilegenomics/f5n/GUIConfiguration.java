package com.mobilegenomics.f5n;

import android.content.Context;
import android.util.Log;
import androidx.annotation.RawRes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mobilegenomics.f5n.core.AppMode;
import com.mobilegenomics.f5n.core.Argument;
import com.mobilegenomics.f5n.core.PipelineComponent;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.support.JSONFileHelper;
import com.mobilegenomics.f5n.support.PipelineState;
import com.mobilegenomics.f5n.support.TimeFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIConfiguration {

    private static final String TAG = GUIConfiguration.class.getSimpleName();

    private static AppMode appMode;

    private static PipelineState pipelineState;

    private static ArrayList<PipelineStep> selectedPipelineSteps = new ArrayList<>();

    private static ArrayList<Step> steps = new ArrayList<>();

    private static int current = 0;

    private static ArrayList<PipelineComponent> pipelineComponents;

    private static HashMap<String, String> linkedFileArguments = new HashMap<>();

    public static void setPipelineState(PipelineState state) {
        pipelineState = state;
    }

    public static PipelineState getPipelineState() {
        return pipelineState;
    }

    public static void addPipelineStep(PipelineStep step) {
        selectedPipelineSteps.add(step);
    }

    public static void eraseSelectedPipeline() {
        selectedPipelineSteps.clear();
    }

    public static AppMode getAppMode() {
        return appMode;
    }

    public static void setAppMode(final AppMode appMode) {
        GUIConfiguration.appMode = appMode;
    }

    public static void printList() {
        for (PipelineStep step : selectedPipelineSteps) {
            Log.d(TAG, step.toString());
        }
    }

    public static void configureSteps(Context context, String folderPath) {
        // clear() vs new check what is better
        steps = new ArrayList<>();
        for (PipelineStep pipelineStep : selectedPipelineSteps) {
            ArrayList<Argument> arguments = configureArguments(context, pipelineStep, folderPath);
            Step step = new Step(pipelineStep, arguments);
            steps.add(step);
        }
    }

    public static void configureSteps(ArrayList<Step> stepList) {
        steps = stepList;
    }

    public static Step getNextStep() {
        // TODO Fix boundary conditions
        return steps.get(current++);
    }

    public static Step getPreviousStep() {
        // TODO Fix boundary conditions
        return steps.get(--current);
    }

    public static void reduceStepCount() {
        // TODO Fix boundary conditions
        // Since UI is navigated using a stack, we just need to reduce the count
        current--;
    }

    public static void resetSteps() {
        current = 0;
    }

    public static int getCurrentStepCount() {
        return current;
    }

    public static boolean isFinalStep() {
        return current == selectedPipelineSteps.size();
    }

    public static String[] getSelectedCommandStrings() {
        String[] commandArray = new String[steps.size()];
        int stepId = 0;
        for (Step step : steps) {
            commandArray[stepId++] = step.getCommandString();
        }
        return commandArray;
    }

    public static void createPipeline() {
        pipelineComponents = new ArrayList<>();
        for (Step step : steps) {
            PipelineComponent pipelineComponent = new PipelineComponent(step.getStep(), step.getCommandString());
            pipelineComponents.add(pipelineComponent);
        }
    }

    public static void runPipeline() {
        for (PipelineComponent pipelineComponent : pipelineComponents) {
            long start = System.currentTimeMillis();
            int returnCode = pipelineComponent.run();
            long time = System.currentTimeMillis() - start;
            String status = returnCode == 0 ? "Success" : "Error";
            pipelineComponent.setRuntime(TimeFormat.millisToShortDHMS(time) + " status = " + status);
            if (returnCode != 0) {
                break;
            }
        }
    }

    public static List<PipelineComponent> getPipeline() {
        return pipelineComponents;
    }

    public static ArrayList<Step> getSteps() {
        return steps;
    }

    public static void setSteps(ArrayList<Step> newSteps) {
        steps = newSteps;
    }

    public static void configureLikedFileArgument(String fileName, String value) {
        linkedFileArguments.put(fileName, value);
    }

    public static String getLinkedFileArgument(String fileName) {
        return linkedFileArguments.containsKey(fileName) ? linkedFileArguments.get(fileName) : "";
    }

    private static ArrayList<Argument> configureArguments(Context context, PipelineStep pipelineStep,
            String folderPath) {
        int rawFile = 0;
        switch (pipelineStep.getValue()) {
            case PipelineStep.MINIMAP2_SEQUENCE_ALIGNMENT:
                rawFile = R.raw.minimap2;
                break;
            case PipelineStep.SAMTOOLS_SORT:
                rawFile = R.raw.samtool_sort_arguments;
                break;
            case PipelineStep.SAMTOOLS_INDEX:
                rawFile = R.raw.samtool_index_arguments;
                break;
            case PipelineStep.F5C_INDEX:
                rawFile = R.raw.f5c_index_arguments;
                break;
            case PipelineStep.F5C_CALL_METHYLATION:
                rawFile = R.raw.f5c_call_methylation_arguments;
                break;
            case PipelineStep.F5C_EVENT_ALIGNMENT:
                rawFile = R.raw.f5c_event_align_arguments;
                break;
            case PipelineStep.F5C_METH_FREQ:
                rawFile = R.raw.f5c_meth_freq_arguments;
                break;
            case PipelineStep.NANOPOLISH_INDEX:
                rawFile = R.raw.nanopolish_index_arguments;
                break;
            default:
                Log.e(TAG, "Invalid Pipeline Step");
                break;
        }
        return buildArgumentsFromJson(context, rawFile, folderPath);
    }

    private static ArrayList<Argument> buildArgumentsFromJson(Context context, @RawRes int file, String folderPath) {
        ArrayList<Argument> arguments = new ArrayList<>();
        JsonObject argsJson = JSONFileHelper.rawtoJsonObject(context, file);
        JsonArray argsJsonArray = argsJson.getAsJsonArray("args");
        for (JsonElement element : argsJsonArray) {
            Argument argument = new Gson().fromJson(element, Argument.class);
            if (folderPath != null) {
                configureDemoArgsFilePath(argument, folderPath);
            }
            arguments.add(argument);
        }
        return arguments;
    }

    private static void configureDemoArgsFilePath(Argument argument, String folder) {

        if (argument.isFile()) {
            argument.setSetByUser(true);
        }

        // minimap2 input/output files
        if (argument.getArgID().equals("MINIMAP2_REF_FILE")) {
            argument.setArgValue(folder + "/draft.fa");
        }
        if (argument.getArgID().equals("MINIMAP2_QUERY_FILE")) {
            argument.setArgValue(folder + "/reads.fasta");
        }
        if (argument.getArgID().equals("MINIMAP2_OUTPUT_FILE")) {
            argument.setArgValue(folder + "/minimap2-out.sam");
        }
        // For the Demo we need to use SAM output format in minimap2
        if (argument.getArgID().equals("MINIMAP2_OUTPUT_SAM")) {
            argument.setSetByUser(true);
        }

        // samtools input/output files
        if (argument.getArgID().equals("SAMTOOL_SORT_INPUT_FILE")) {
            argument.setArgValue(folder + "/minimap2-out.sam");
        }
        if (argument.getArgID().equals("SAMTOOL_SORT_OUTPUT_FILE")) {
            argument.setArgValue(folder + "/reads.sorted.bam");
        }
        // For the Demo we need to use 100M max memory for samtools sort
        if (argument.getArgID().equals("SAMTOOL_MAX_MEMORY")) {
            argument.setSetByUser(true);
            argument.setArgValue("100M");
        }

        if (argument.getArgID().equals("SAMTOOL_INDEX_INPUT_FILE")) {
            argument.setArgValue(folder + "/reads.sorted.bam");
        }

        // f5c index input/output files
        if (argument.getArgID().equals("F5C_INDEX_FAST5_FILE")) {
            argument.setArgValue(folder + "/fast5_files");
        }
        if (argument.getArgID().equals("F5C_INDEX_FASTA_FILE")) {
            argument.setArgValue(folder + "/reads.fasta");
        }

        // f5c methylation input/output files
        if (argument.getArgID().equals("F5C_METH_FASTA_FILE")) {
            argument.setArgValue(folder + "/reads.fasta");
        }
        if (argument.getArgID().equals("F5C_METH_SORTED_FILE")) {
            argument.setArgValue(folder + "/reads.sorted.bam");
        }
        if (argument.getArgID().equals("F5C_METH_REF_FILE")) {
            argument.setArgValue(folder + "/draft.fa");
        }
        if (argument.getArgID().equals("F5C_METH_OUTPUT_FILE")) {
            argument.setArgValue(folder + "/f5c-methylation.tsv");
        }

        // f5c event alignment input/output files
        if (argument.getArgID().equals("F5C_ALIGN_FASTA_FILE")) {
            argument.setArgValue(folder + "/reads.fasta");
        }
        if (argument.getArgID().equals("F5C_ALIGN_SORTED_FILE")) {
            argument.setArgValue(folder + "/reads.sorted.bam");
        }
        if (argument.getArgID().equals("F5C_ALIGN_REF_FILE")) {
            argument.setArgValue(folder + "/draft.fa");
        }
        if (argument.getArgID().equals("F5C_ALIGN_OUTPUT_FILE")) {
            argument.setArgValue(folder + "/f5c-event-alignment.txt");
        }
        if (argument.getArgID().equals("F5C_ALIGN_SUMMARY_OUTPUT_FILE")) {
            argument.setArgValue(folder + "/event.summary.txt");
        }

        // f5c methylation frequency input/output files
        if (argument.getArgID().equals("F5C_METH_FREQ_INPUT_FILE")) {
            argument.setArgValue(folder + "/f5c-methylation.tsv");
        }
        if (argument.getArgID().equals("F5C_METH_FREQ_FILE")) {
            argument.setArgValue(folder + "/f5c-methylation-freq.tsv");
        }

    }

}
