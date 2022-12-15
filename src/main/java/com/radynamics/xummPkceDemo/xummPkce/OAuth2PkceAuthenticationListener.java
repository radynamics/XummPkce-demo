package com.radynamics.xummPkceDemo.xummPkce;

import java.net.URI;

public interface OAuth2PkceAuthenticationListener {
    void onAuthorized(String accessToken);

    void onOpenInBrowser(URI uri);
}
