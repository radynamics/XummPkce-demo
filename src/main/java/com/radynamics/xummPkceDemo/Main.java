package com.radynamics.xummPkceDemo;

import com.radynamics.xummPkceDemo.xummPkce.MemoryStorage;
import com.radynamics.xummPkceDemo.xummPkce.XummSigner;
import org.json.JSONObject;

public class Main {
    private static XummSigner xummSigner;

    // Enter your "API Key" from Xumm developer console
    private static final String apiKey = "<API-Key_from_XUMM_dashboard>";

    public static void main(String[] args) {
        xummSigner = new XummSigner(apiKey);
        var storage = new MemoryStorage();
        // Optionally load stored access token to prevent unnecessary auth requests.
        //storage.setAccessToken("eyJhbGci....");
        xummSigner.setStorage(storage);
        xummSigner.addStateListener(new TransactionStateListener() {
            @Override
            public void onProgressChanged(JSONObject transaction) {
                System.out.println(String.format("PROGRESS_CHANGED: %s", transaction));
            }

            @Override
            public void onSuccess(JSONObject transaction) {
                System.out.println(String.format("SUCCESS: %s", transaction));
            }

            @Override
            public void onFailure(JSONObject transaction) {
                System.out.println(String.format("FAILURE: %s", transaction));
            }
        });

        var json = new JSONObject();
        json.put("TransactionType", "Payment");
        json.put("Destination", "rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
        json.put("Amount", "322");
        xummSigner.submit(json);
    }
}
