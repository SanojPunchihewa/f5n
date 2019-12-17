package com.mobilegenomics.f5n.core;

public class NativeCommands {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
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

    public native int init(String command);

    public native int initminimap2(String command);

    public native int initsamtool(String command);

}
