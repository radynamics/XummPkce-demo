package com.radynamics.xummPkceDemo.xummPkce;

public class XummException extends Exception {
    public XummException(String errorMessage) {
        super(errorMessage);
    }

    public XummException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
