package org.guard_jiang.services.storage.sql.records;

import org.guard_jiang.AccountCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Guard account.
 */
public class AccountRecord {
    private String id;
    private int partition = -1;
    private String email;
    private String password;
    private String certificate;
    private String authToken;

    public AccountRecord() {}

    public AccountRecord(@Nonnull AccountRecord oldRecord) {
        id = oldRecord.id;
        partition = oldRecord.partition;
        email = oldRecord.email;
        password = oldRecord.password;
        certificate = oldRecord.certificate;
        authToken = oldRecord.authToken;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public int getPartition() {
        return partition;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

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
}
