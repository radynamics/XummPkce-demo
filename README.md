# XUMM PKCE demo

This Java project demos following operations using [XUMM-App](https://github.com/XRPL-Labs/XUMM-App):
- [Sign in with XUMM](https://xumm.readme.io/docs/user-sign-in-identity-provider) using OAuth2 PKCE on a desktop software
- Send payment signing request to XUMM
- Observe/Monitor state of sent signing request

### Steps to use this code
1. Create an application in [Xumm Developer Console](https://apps.xumm.dev/)
   1. Under "Settings / Origin/Redirect URIs" enter "http://127.0.0.1:58890/auth"
2. Enter "API-Key" in Main.java (apiKey)

### Hints ###
- Use a storage (like MemoryStorage) to store accessToken for further usage. accessToken may expire at any time.
- OAuth2PkceAuthentication.java runs temporary a HttpServer on 127.0.0.1 listening on port 58890
- Set scope variable in Main.java to your own software name

### Useful documents ###
- [Build on Xumm](https://xumm.readme.io/)
- [Authorization Code Flow with PKCE](https://auth0.com/docs/get-started/authentication-and-authorization-flow/call-your-api-using-the-authorization-code-flow-with-pkce)
