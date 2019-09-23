package com.mobilegenomics.f5n;

import android.util.Log;
import java.util.ArrayList;

public class GUIConfiguration {

    private static ArrayList<PipelineStep> selectedPipelineSteps = new ArrayList<>();

    private static ArrayList<Step> steps = new ArrayList<>();

    private static int current = 0;

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

    public static void configureSteps() {
        // clear() vs new check what is better
        steps = new ArrayList<>();
        for (PipelineStep pipelineStep : selectedPipelineSteps) {
            Step step = new Step();
            step.setStep(pipelineStep);
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

}
