package com.mobilegenomics.f5n.support;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ZipListener {

    void onStarted(@NonNull final long totalBytes);

    void onProgress(@NonNull final long bytesDone, @NonNull final long totalBytes);

    void onComplete(@NonNull final boolean success, @NonNull final long timeTook, @Nullable final Exception exception);

}
