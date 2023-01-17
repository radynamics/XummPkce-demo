package com.radynamics.xummPkceDemo;

import org.json.JSONObject;

public interface OnchainVerifier {
    /**
     * True, if the transaction identified by txid was found on chain and matches the expectation.
     */
    boolean verify(String txid, JSONObject expected);
}
