package com.mobilegenomics.f5n;

import androidx.annotation.NonNull;

public class Argument {

    private String argName;

    private String argValue;

    private String argDescription;

    private boolean hasFlag;

    private boolean setByUser;

    private String flag;

    public Argument(final String argName, final String argValue, final String argDescription, final boolean hasFlag,
            final String flag) {
        this.argName = argName;
        this.argValue = argValue;
        this.argDescription = argDescription;
        this.hasFlag = hasFlag;
        this.flag = flag;
        this.setByUser = false;
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
        return this.hasFlag ? this.flag + " " + this.argValue : this.argValue;
    }

    public boolean isSetByUser() {
        return setByUser;
    }

    public void setSetByUser(final boolean setByUser) {
        this.setByUser = setByUser;
    }
}
