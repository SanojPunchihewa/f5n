package com.mobilegenomics.f5n.core;

import com.mobilegenomics.f5n.GUIConfiguration;

public class NativeCommands {

    // Used to load the 'native-lib' library on application startup.
    static {
        if (GUIConfiguration.getAppMode() == AppMode.STANDALONE_METHYLATION) {
            System.loadLibrary("methylation-native-lib");
        } else if (GUIConfiguration.getAppMode() == AppMode.STANDALONE_VARIANT) {
            System.loadLibrary("variant-native-lib");
        }
    }

    private static NativeCommands nativeCommands;

    private NativeCommands() {

    }

    public static NativeCommands getNativeInstance() {
        if (nativeCommands == null) {
            nativeCommands = new NativeCommands();
        }
        return nativeCommands;
    }

    public native int startPipeline(String pipePath);

    public native int finishPipeline(String pipePath);

    public native int init(String command, int command_id);
}
