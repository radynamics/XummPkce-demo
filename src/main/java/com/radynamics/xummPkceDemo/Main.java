package com.radynamics.xummPkceDemo;

import com.radynamics.xummPkceDemo.xummPkce.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Main {
    private static XummSigningObserver<JSONObject> observer;

    // Enter your "API Key" from Xumm developer console
    private static final String apiKey = "<API-Key_from_XUMM_dashboard>";
    private static final String scope = "xummPkceDemo";

    public static void main(String[] args) {
        observer = new XummSigningObserver<>();
        observer.addStateListener(new StateListener<>() {
            @Override
            public void onOpened(JSONObject payload) {
                // do nothing
            }

            @Override
            public void onExpired(JSONObject payload) {
                System.out.println(String.format("EXPIRED: %s", payload));
            }

            @Override
            public void onAccepted(JSONObject payload, String txid) {
                System.out.println(String.format("ACCEPTED: %s", payload));
            }

            @Override
            public void onRejected(JSONObject payload) {
                System.out.println(String.format("REJECTED: %s", payload));
            }

            @Override
            public void onConnectionClosed(JSONObject payload, int code, String reason) {
                // do nothing
            }

            @Override
            public void onException(JSONObject payload, Exception e) {
                System.out.println(String.format("%s: %s", payload, e));
            }
        });

        var storage = new MemoryStorage();
        // Optionally load stored access token to prevent unnecessary auth requests.
        //storage.setAccessToken("eyJhbGci....");

        var auth = new CompletableFuture<Void>();
        if (storage.getAccessToken() == null) {
            auth = authenticate(storage);
        } else {
            auth.complete(null);
        }

        auth
                .thenRun(() -> submitTransactionAndObserve(storage))
                .thenRun(() -> {
                    try {
                        // Wait before stop observing
                        while (observer.countListening() > 0) {
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .whenComplete((result, throwable) -> observer.shutdown())
                .exceptionally((e) -> {
                    System.out.println(e);
                    return null;
                });
    }

    private static CompletableFuture<Void> authenticate(Storage storage) {
        return XummPkce.authenticateAsync(apiKey, scope)
                .thenAccept(storage::setAccessToken)
                .exceptionally((e) -> {
                    System.out.println(e);
                    return null;
                });
    }

    private static void submitTransactionAndObserve(Storage storage) {
        try {
            var api = new XummApi(storage.getAccessToken());
            api.addListener(() -> {
                System.out.println("Xumm accessToken expired");
                // Re-authenticate if used accessToken expired.
                authenticate(storage)
                        .thenRun(() -> submitTransactionAndObserve(storage));
            });
            // Get status of a submitted payload
            //System.out.println(api.status(UUID.fromString("f7bcca35-1175-4635-a035-d8bc5e837c62")));

            var json = new JSONObject();
            json.put("TransactionType", "Payment");
            json.put("Destination", "rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B");
            json.put("Amount", "322");
            var sendResponse = api.submit(json);

            System.out.println(sendResponse);
            observer.observe(json, URI.create(sendResponse.getJSONObject("refs").getString("websocket_status")));
        } catch (IOException | InterruptedException | XummException e) {
            throw new RuntimeException(e);
        }
    }
}
