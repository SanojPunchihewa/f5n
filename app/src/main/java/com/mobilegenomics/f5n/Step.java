package com.mobilegenomics.f5n;

import android.content.Context;
import android.util.Log;
import androidx.annotation.RawRes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;

public class Step {

    private int argumentsCount;

    private ArrayList<Argument> arguments;

    private PipelineStep stepName;

    private StringBuilder commandBuilder;

    private String command;

    public PipelineStep getStep() {
        return stepName;
    }

    public void setStep(Context context, final PipelineStep stepName) {
        this.stepName = stepName;
        configureArguments(context);
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    private void configureArguments(Context context) {
        this.arguments = new ArrayList<>();
        switch (this.stepName) {
            case MINIMAP2_SEQUENCE_ALIGNMENT:
                this.argumentsCount = 3;
                this.minimap2Arguments(context);
                break;
            case SAMTOOL_SORT:
                this.argumentsCount = 2;
                this.samtoolSortArguments(context);
                break;
            case SAMTOOL_INDEX:
                this.argumentsCount = 2;
                this.samtoolIndexArguments(context);
                break;
            case F5C_INDEX:
                this.argumentsCount = 2;
                this.f5cIndexArguments(context);
                break;
            case F5C_CALL_METHYLATION:
                this.argumentsCount = 2;
                this.f5cCallMethylationArguments(context);
                break;
            case F5C_EVENT_ALIGNMENT:
                this.argumentsCount = 2;
                this.f5cEventAlignArguments(context);
                break;
            default:
                Log.e("STEP", "cannot come here");
                break;
        }
        buildCommandString();
    }

    private void minimap2Arguments(Context context) {
        buildArgumentsFromJson(context, R.raw.minimap2);
    }

    private void samtoolSortArguments(Context context) {
        buildArgumentsFromJson(context, R.raw.samtool_sort_arguments);
    }

    private void samtoolIndexArguments(Context context) {
        buildArgumentsFromJson(context, R.raw.samtool_index_arguments);
    }

    private void f5cIndexArguments(Context context) {
        buildArgumentsFromJson(context, R.raw.f5c_index_arguments);
    }

    private void f5cCallMethylationArguments(Context context) {
        buildArgumentsFromJson(context, R.raw.f5c_call_methylation_arguments);
    }

    private void f5cEventAlignArguments(Context context) {
        buildArgumentsFromJson(context, R.raw.f5c_event_align_arguments);
    }

    private void buildArgumentsFromJson(Context context, @RawRes int file) {
        JsonObject samtoolIndexArgsJson = Helper.rawtoJsonObject(context, file);
        JsonArray samtoolIndexArgsJsonArray = samtoolIndexArgsJson.getAsJsonArray("args");
        for (JsonElement element : samtoolIndexArgsJsonArray) {
            Argument argument = new Gson().fromJson(element, Argument.class);
            arguments.add(argument);
        }
    }

    public void buildCommandString() {
        commandBuilder = new StringBuilder(stepName.getCommand());
        for (Argument argument : arguments) {
            commandBuilder.append(" ");
            commandBuilder.append(argument.toString());
        }
        command = commandBuilder.toString();
    }

    public String getCommandString() {
        return command;
    }

    public void setgetCommandString(final String command) {
        this.command = command;
    }

}
