package com.radynamics.xummPkceDemo.xummPkce;

public interface StateListener<T> {
    void onExpired(T payload);

    void onAccepted(T payload, String txid);

    void onRejected(T payload);

    void onException(T payload, Exception e);
}
