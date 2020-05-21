package com.mobilegenomics.genopo.core;

import com.mobilegenomics.genopo.R;
import com.mobilegenomics.genopo.support.PreferenceUtil;

public class NativeCommands {

    // Used to load the 'native-lib' library on application startup.
    static {

        int pipelineType = PreferenceUtil.getSharedPreferenceInt(R.string.key_pipeline_type_preference);

        if (pipelineType == PipelineType.PIPELINE_METHYLATION.ordinal()) {
            System.loadLibrary("methylation-native-lib");
        } else if (pipelineType == PipelineType.PIPELINE_VARIANT.ordinal()) {
            System.loadLibrary("variant-native-lib");
        } else if (pipelineType == PipelineType.PIPELINE_ARTIC.ordinal()) {
            System.loadLibrary("artic-native-lib");
        } else if (pipelineType == PipelineType.PIPELINE_CONSENSUS.ordinal()) {
            System.loadLibrary("consensus-native-lib");
        } else if (pipelineType == PipelineType.SINGLE_TOOL.ordinal()) {
            System.loadLibrary("single-tool-native-lib");
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
