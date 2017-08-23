package org.guard_jiang.services.storage.sql.records;

import javax.annotation.Nullable;

/**
 * Credential.
 */
public class CredentialRecord {
    private String email;
    private String password;
    private String certificate;
    private String authToken;

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    @Nullable
    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(@Nullable String certificate) {
        this.certificate = certificate;
    }

    @Nullable
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }
}
