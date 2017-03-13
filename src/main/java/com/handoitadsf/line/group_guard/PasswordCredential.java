package com.handoitadsf.line.group_guard;

import io.cslinmiso.line.model.LoginCallback;
import org.apache.thrift.TException;

import java.io.IOException;

import io.cslinmiso.line.model.LineClient;
import line.thrift.TalkException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cahsieh on 1/26/17.
 */
public class PasswordCredential implements AccountCredential {
    private final String accountName;
    private final String password;
    private final String certificate;

    public PasswordCredential(@Nonnull String accountName,
                              @Nonnull String password) {
        this.accountName = accountName;
        this.password = password;
        this.certificate = null;
    }

    public PasswordCredential(@Nonnull String accountName,
                              @Nonnull String password,
                              @Nullable String certificate) {
        this.accountName = accountName;
        this.password = password;
        this.certificate = certificate;
    }

    @Override
    public Account login(@Nonnull LoginCallback loginCallback) throws IOException, LoginFailureException {
        LineClient client;
        try {
            client = new LineClient();
            client.login(accountName, password, certificate, loginCallback);
        } catch (TException ex) {
            throw new IOException(ex);
        } catch (Exception ex) {
            throw new LoginFailureException("Fail to login", ex);
        }
        return new Account(client);
    }

    @Override
    public Account login() throws IOException, LoginFailureException {
        LineClient client;
        try {
            client = new LineClient();
            client.login(accountName, password, certificate, null);
        } catch (TException ex) {
            throw new IOException(ex);
        } catch (Exception ex) {
            throw new LoginFailureException("Fail to login", ex);
        }
        return new Account(client);
    }
}
