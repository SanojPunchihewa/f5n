package com.mobilegenomics.genopo.core;

public class ArticPipelineArgument {

    private String argID;

    private String argDescription;

    private boolean flagOnly;

    private boolean isFile;

    private String value;

    public ArticPipelineArgument(final String argID, final String argDescription, final boolean flagOnly,
            final boolean isFile,
            final String value) {
        this.argID = argID;
        this.argDescription = argDescription;
        this.flagOnly = flagOnly;
        this.isFile = isFile;
        this.value = value;
    }

    public String getArgID() {
        return argID;
    }

    public void setArgID(final String argID) {
        this.argID = argID;
    }

    public String getArgDescription() {
        return argDescription;
    }

    public void setArgDescription(final String argDescription) {
        this.argDescription = argDescription;
    }

    public boolean isFlagOnly() {
        return flagOnly;
    }

    public void setFlagOnly(final boolean flagOnly) {
        this.flagOnly = flagOnly;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(final boolean file) {
        isFile = file;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
