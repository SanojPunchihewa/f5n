package com.mobilegenomics.genopo.core;

import androidx.annotation.NonNull;
import java.io.Serializable;

public class Argument implements Serializable {

    private String argName;

    private String argValue;

    private String argDescription;

    private boolean hasFlag;

    private boolean setByUser; // not needed

    private String flag;

    private boolean required;

    private String argID;

    private String isDependentOn;

    public String getArgID() {
        return argID;
    }

    public String getIsDependentOn() {
        return isDependentOn;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isFlagOnly() {
        return flagOnly;
    }

    private boolean flagOnly;

    private boolean isFile;

    public boolean isRequired() {
        return required;
    }

    public void setIsDependentOn(final String isDependentOn) {
        this.isDependentOn = isDependentOn;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public Argument(final String argName, final String argValue, final String argDescription,
            final String flag, final String argID, final String isDependentOn, final boolean hasFlag,
            final boolean flagOnly,
            final boolean required, final boolean isFile) {
        this.argID = argID;
        this.argName = argName;
        this.argValue = argValue;
        this.argDescription = argDescription;
        this.hasFlag = hasFlag;
        this.flag = flag;
        this.setByUser = false;
        this.required = required;
        this.flagOnly = flagOnly;
        this.isFile = isFile;
        this.isDependentOn = isDependentOn;
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
                if (this.hasFlag) {
                    return this.flag.endsWith("=") ? this.flag + this.argValue : this.flag + " " + this.argValue;
                } else {
                    return this.argValue;
                }
            } else {
                return this.flagOnly ? this.flag : "";
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
