package com.radynamics.xummPkceDemo.xummPkce;

import com.radynamics.xummPkceDemo.OnchainVerifier;
import com.radynamics.xummPkceDemo.TransactionStateListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class XummSigner implements StateListener<JSONObject> {
    private final static Logger log = LogManager.getLogger(XummSigner.class);

    private final XummApi api = new XummApi();
    private final PollingObserver<JSONObject> observer = new PollingObserver<>(api);
    private final ArrayList<TransactionStateListener> stateListener = new ArrayList<>();
    private Storage storage = new MemoryStorage();
    private final String apiKey;
    private OnchainVerifier verifier;
    private CompletableFuture<Void> authentication;

    private static final String scope = "xummPkceDemo";

    public XummSigner(String apiKey) {
        if (apiKey == null) throw new IllegalArgumentException("Parameter 'apiKey' cannot be null");
        this.apiKey = apiKey;
        this.observer.addStateListener(this);
    }

    public void submit(JSONObject transaction) {
        submit(new JSONObject[]{transaction});
    }

    public void submit(JSONObject[] transactions) {
        for (var trx : transactions) {
            submit(trx, trx);
        }
    }

    @Override
    public void onExpired(JSONObject t) {
        raiseFailure(t);
    }

    @Override
    public void onAccepted(JSONObject t, String txid) {
        if (verifier != null && !verifier.verify(txid, t)) {
            raiseFailure(t);
            return;
        }

        raiseSuccess(t);
    }

    @Override
    public void onRejected(JSONObject t) {
        raiseFailure(t);
    }

    @Override
    public void onException(JSONObject t, Exception e) {
        raiseFailure(t);
    }

    private void submit(JSONObject transaction, JSONObject xummJson) {
        if (xummJson == null) throw new IllegalArgumentException("Parameter 'json' cannot be null");

        var auth = new CompletableFuture<Void>();
        if (storage.getAccessToken() == null) {
            auth = authenticate(transaction);
        } else {
            var payload = JwtPayload.create(storage.getAccessToken());
            if (payload != null && payload.expired()) {
                auth = authenticate(transaction);
            } else {
                auth.complete(null);
            }
        }

        auth
                .thenRunAsync(() -> submitAndObserve(transaction, xummJson))
                .thenRun(() -> {
                    try {
                        // Wait before stop observing
                        while (observer.countListening() > 0) {
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                })
                .whenComplete((result, throwable) -> observer.shutdown())
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    raiseFailure(transaction);
                    return null;
                });
    }

    private synchronized CompletableFuture<Void> authenticate(JSONObject t) {
        if (authentication != null) {
            return authentication;
        }

        authentication = XummPkce.authenticateAsync(apiKey, scope)
                .thenAccept(storage::setAccessToken)
                .exceptionally((e) -> {
                    log.error(e.getMessage(), e);
                    raiseFailure(t);
                    return null;
                })
                .whenComplete((unused, throwable) -> authentication = null);
        return authentication;
    }

    private void submitAndObserve(JSONObject t, JSONObject json) {
        try {
            api.setAccessToken(storage.getAccessToken());
            api.addListener(() -> {
                log.info("Xumm accessToken expired.");
                // Re-authenticate if used accessToken expired.
                authenticate(t)
                        .thenRunAsync(() -> submitAndObserve(t, json));
            });

            var sendResponse = api.submit(json);
            if (sendResponse == null) {
                return;
            }

            raiseProgressChanged(t);
            observer.observe(t, UUID.fromString(sendResponse.getString("uuid")));
        } catch (IOException | InterruptedException | XummException e) {
            throw new RuntimeException(e);
        }
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public OnchainVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(OnchainVerifier verifier) {
        this.verifier = verifier;
    }

    public void addStateListener(TransactionStateListener l) {
        stateListener.add(l);
    }

    private void raiseProgressChanged(JSONObject transaction) {
        for (var l : stateListener) {
            l.onProgressChanged(transaction);
        }
    }

    private void raiseSuccess(JSONObject transaction) {
        for (var l : stateListener) {
            l.onSuccess(transaction);
        }
    }

    private void raiseFailure(JSONObject transaction) {
        for (var l : stateListener) {
            l.onFailure(transaction);
        }
    }
}
