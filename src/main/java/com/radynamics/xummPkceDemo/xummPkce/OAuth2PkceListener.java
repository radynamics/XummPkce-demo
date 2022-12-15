package com.radynamics.xummPkceDemo.xummPkce;

public interface OAuth2PkceListener {
    void onAuthorizationCodeReceived(String code);
}
