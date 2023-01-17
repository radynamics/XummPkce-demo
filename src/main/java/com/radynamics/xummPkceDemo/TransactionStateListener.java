package com.radynamics.xummPkceDemo;

import org.json.JSONObject;

public interface TransactionStateListener {
    void onProgressChanged(JSONObject transaction);

    void onSuccess(JSONObject transaction);

    void onFailure(JSONObject transaction);
}
