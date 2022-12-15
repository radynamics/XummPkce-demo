package com.radynamics.xummPkceDemo.xummPkce;

public class OAuth2Exception extends Exception {
    public OAuth2Exception(String errorMessage) {
        super(errorMessage);
    }

    public OAuth2Exception(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
