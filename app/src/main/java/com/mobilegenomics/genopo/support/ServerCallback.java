package com.mobilegenomics.genopo.support;

import com.mobilegenomics.genopo.dto.WrapperObject;

public interface ServerCallback {

    void onSuccess(WrapperObject job);

    void onError(WrapperObject job);
}
