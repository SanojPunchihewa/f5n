package com.mobilegenomics.genopo.support;

import java.util.ArrayList;

public interface PermissionResultCallback {

    void NeverAskAgain(int request_code);

    void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions);

    void PermissionDenied(int request_code);

    void PermissionGranted(int request_code);
}
