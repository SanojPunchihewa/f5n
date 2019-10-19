package com.mobilegenomics.f5n;

import android.content.Context;
import android.util.Log;
import androidx.annotation.RawRes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mobilegenomics.f5n.core.Argument;
import com.mobilegenomics.f5n.core.PipelineComponent;
import com.mobilegenomics.f5n.core.PipelineStep;
import com.mobilegenomics.f5n.core.Step;
import com.mobilegenomics.f5n.support.JSONFileHelper;
import com.mobilegenomics.f5n.support.TimeFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIConfiguration {

    private static final String TAG = GUIConfiguration.class.getSimpleName();

    private static ArrayList<PipelineStep> selectedPipelineSteps = new ArrayList<>();

    private static ArrayList<Step> steps = new ArrayList<>();

    private static int current = 0;

    private static ArrayList<PipelineComponent> pipelineComponents;

    private static HashMap<String, String> linkedFileArguments = new HashMap<>();

    public static void addPipelineStep(PipelineStep step) {
        selectedPipelineSteps.add(step);
    }

    public static void eraseSelectedPipeline() {
        selectedPipelineSteps.clear();
    }

    public static void printList() {
        for (PipelineStep step : selectedPipelineSteps) {
            Log.d(TAG, step.toString());
        }
    }

    public static void configureSteps(Context context) {
        // clear() vs new check what is better
        steps = new ArrayList<>();
        for (PipelineStep pipelineStep : selectedPipelineSteps) {
            ArrayList<Argument> arguments = configureArguments(context, pipelineStep);
            Step step = new Step(pipelineStep, arguments);
            steps.add(step);
        }
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
            pipelineComponent.run();
            long time = System.currentTimeMillis() - start;
            pipelineComponent.setRuntime(TimeFormat.millisToShortDHMS(time));
        }
    }

    public static List<PipelineComponent> getPipeline() {
        return pipelineComponents;
    }

    public static ArrayList<Step> getSteps() {
        return steps;
    }

    public static void configureLikedFileArgument(String fileName, String value) {
        linkedFileArguments.put(fileName, value);
    }

    public static String getLinkedFileArgument(String fileName) {
        return linkedFileArguments.containsKey(fileName) ? linkedFileArguments.get(fileName) : "";
    }

    private static ArrayList<Argument> configureArguments(Context context, PipelineStep pipelineStep) {
        int rawFile = 0;
        switch (pipelineStep) {
            case MINIMAP2_SEQUENCE_ALIGNMENT:
                rawFile = R.raw.minimap2;
                break;
            case SAMTOOL_SORT:
                rawFile = R.raw.samtool_sort_arguments;
                break;
            case SAMTOOL_INDEX:
                rawFile = R.raw.samtool_index_arguments;
                break;
            case F5C_INDEX:
                rawFile = R.raw.f5c_index_arguments;
                break;
            case F5C_CALL_METHYLATION:
                rawFile = R.raw.f5c_call_methylation_arguments;
                break;
            case F5C_EVENT_ALIGNMENT:
                rawFile = R.raw.f5c_event_align_arguments;
                break;
            default:
                Log.e(TAG, "Invalid Pipeline Step");
                break;
        }
        return buildArgumentsFromJson(context, rawFile);
    }

    private static ArrayList<Argument> buildArgumentsFromJson(Context context, @RawRes int file) {
        ArrayList<Argument> arguments = new ArrayList<>();
        JsonObject argsJson = JSONFileHelper.rawtoJsonObject(context, file);
        JsonArray argsJsonArray = argsJson.getAsJsonArray("args");
        for (JsonElement element : argsJsonArray) {
            Argument argument = new Gson().fromJson(element, Argument.class);
            arguments.add(argument);
        }
        return arguments;
    }

}
