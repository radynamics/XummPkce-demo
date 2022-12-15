package com.radynamics.xummPkceDemo.xummPkce;

public interface StateListener<T> {
    void onOpened(T payload);

    void onExpired(T payload);

    void onAccepted(T payload, String txid);

    void onRejected(T payload);

    void onConnectionClosed(T payload, int code, String reason);

    void onException(T payload, Exception e);
}
