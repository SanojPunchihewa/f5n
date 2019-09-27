package com.mobilegenomics.f5n;

import androidx.annotation.NonNull;

public class Argument {

    private String argName;

    private String argValue;

    private String argDescription;

    private boolean hasFlag;

    private boolean setByUser; // not needed

    private String flag;

    private boolean required;

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

    public Argument(final boolean required, final String argName, final String argValue, final String argDescription,
            final boolean hasFlag,
            final String flag, final boolean flagOnly, final boolean isFile) {
        this.argName = argName;
        this.argValue = argValue;
        this.argDescription = argDescription;
        this.hasFlag = hasFlag;
        this.flag = flag;
        this.setByUser = false;
        this.required = required;
        this.flagOnly = flagOnly;
        this.file = isFile;
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

    public void setArgValue(final String argValue) {
        this.argValue = argValue;
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
