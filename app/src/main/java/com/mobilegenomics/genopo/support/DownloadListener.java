package com.mobilegenomics.genopo.support;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.liulishuo.okdownload.core.cause.EndCause;

public interface DownloadListener {

    void onComplete(@NonNull final EndCause cause, @Nullable final Exception realCause);
}
