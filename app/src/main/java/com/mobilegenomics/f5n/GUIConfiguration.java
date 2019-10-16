package com.mobilegenomics.f5n;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUIConfiguration {

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
            Log.d("STEPS = ", step.toString());
        }
    }

    public static void configureSteps(Context context) {
        // clear() vs new check what is better
        steps = new ArrayList<>();
        for (PipelineStep pipelineStep : selectedPipelineSteps) {
            Step step = new Step();
            step.setStep(context, pipelineStep);
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

}
