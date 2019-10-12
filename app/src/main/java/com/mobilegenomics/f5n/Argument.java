package com.mobilegenomics.f5n;

import android.util.Pair;
import androidx.annotation.NonNull;

public class Argument {

    private int argID;

    private String argName;

    private String argValue;

    private String folderPath;

    private String fileName;

    private String argDescription;

    private boolean hasFlag;

    private boolean setByUser; // not needed

    private String flag;

    private boolean required;

    private Pair<PipelineStep, Integer> linkedArgument;

    public String getFileName() {
        return fileName;
    }

    public int getArgID() {
        return argID;
    }

    public void setArgID(final int argID) {
        this.argID = argID;
    }

    public Pair<PipelineStep, Integer> getLinkedArgument() {
        return linkedArgument;
    }

    public void setLinkedArgument(final Pair<PipelineStep, Integer> linkedArgument) {
        this.linkedArgument = linkedArgument;
    }

    public boolean isFile() {
        return file;
    }

    public boolean isFlagOnly() {
        return flagOnly;
    }

    private boolean flagOnly;

    private boolean file;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public Argument(final boolean required, final String argName, final String argValue, final String argDescription,
            final boolean hasFlag,
            final String flag, final boolean flagOnly) {
        this.argName = argName;
        this.argValue = argValue;
        this.argDescription = argDescription;
        this.hasFlag = hasFlag;
        this.flag = flag;
        this.setByUser = false;
        this.required = required;
        this.flagOnly = flagOnly;
        this.file = false;
    }

    /*
     *  Constructor for File type arguments
     *
     * */
    public Argument(final int id, final boolean required, final String argName, final String folderPath,
            final String fileName,
            final String argDescription,
            final boolean hasFlag,
            final String flag, final boolean flagOnly, final boolean isFile,
            final Pair<PipelineStep, Integer> linkedArgument) {
        this.argID = id;
        this.argName = argName;
        this.folderPath = folderPath;
        this.fileName = fileName;
        this.argDescription = argDescription;
        this.hasFlag = hasFlag;
        this.flag = flag;
        this.setByUser = false;
        this.required = required;
        this.flagOnly = flagOnly;
        this.file = isFile;
        this.linkedArgument = linkedArgument;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgName(final String argName) {
        this.argName = argName;
    }

    public String getArgValue() {
        return argValue;
    }

    public void setFolderPathAndFileName(final String folderPath, final String fileName) {
        this.folderPath = folderPath;
        this.fileName = fileName;
        String path = folderPath.endsWith("/") ? folderPath + fileName : folderPath + "/" + fileName;
        setArgValue(path);
        updateLinkedFileArguments(folderPath, fileName);
    }

    public void setArgValue(final String argValue) {
        this.argValue = argValue;
    }

    private void updateLinkedFileArguments(final String folderPath, final String fileName) {
        if (getLinkedArgument() != null && GUIConfiguration.hasPipelineStep(getLinkedArgument().first)) {
            Pair<PipelineStep, Integer> linkedArgument = getLinkedArgument();
            Step step = GUIConfiguration.getStepByPipelineStep(linkedArgument.first);
            if (step != null) {
                step.findArgumentById(linkedArgument.second).setFolderPathAndFileName(folderPath, fileName);
            }
        }
    }

    public String getArgDescription() {
        return argDescription;
    }

    public void setArgDescription(final String argDescription) {
        this.argDescription = argDescription;
    }

    public boolean isHasFlag() {
        return hasFlag;
    }

    public void setHasFlag(final boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(final String flag) {
        this.flag = flag;
    }

    @NonNull
    @Override
    public String toString() {
        // TODO Check for NULL
        if (this.setByUser) {
            if (this.argValue != null) {
                return this.hasFlag ? this.flag + " " + this.argValue : this.argValue;
            } else {
                return this.flag;
            }
        }
        return "";
    }

    public boolean isSetByUser() {
        return setByUser;
    }

    public void setSetByUser(final boolean setByUser) {
        this.setByUser = setByUser;
    }
}
