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

    public static void printList() {
        for (PipelineStep step : selectedPipelineSteps) {
            Log.d("STEPS = ", step.toString());
        }
    }

    public static void configureSteps() {
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

}
