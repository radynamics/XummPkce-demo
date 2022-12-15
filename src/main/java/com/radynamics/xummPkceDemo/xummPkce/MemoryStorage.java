package com.radynamics.xummPkceDemo.xummPkce;

public class MemoryStorage implements Storage {
    private String accessToken;

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String value) {
        accessToken = value;
    }
}
