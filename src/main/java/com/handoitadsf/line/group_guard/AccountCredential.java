package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;
import io.cslinmiso.line.model.LoginCallback;

/**
 * Created by cahsieh on 1/26/17.
 */
public class AccountCredential {
    private String email;
    private String password;
    private String certificate;
    private String authToken;
    private LoginCallback loginCallback;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public LoginCallback getLoginCallback() {
        return loginCallback;
    }

    public void setLoginCallback(LoginCallback loginCallback) {
        this.loginCallback = loginCallback;
    }
}
