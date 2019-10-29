package com.mobilegenomics.f5n.support;

import com.mobilegenomics.f5n.dto.WrapperObject;

public interface ServerCallback {

    void onSuccess(WrapperObject job);

    void onError(WrapperObject job);
}
