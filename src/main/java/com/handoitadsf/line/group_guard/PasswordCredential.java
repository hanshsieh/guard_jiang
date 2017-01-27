package com.handoitadsf.line.group_guard;

import org.apache.thrift.TException;

import java.io.IOException;

import io.cslinmiso.line.model.LineClient;
import line.thrift.TalkException;

/**
 * Created by cahsieh on 1/26/17.
 */
public class PasswordCredential implements AccountCredential {
    private String accountName;
    private String password;

    public PasswordCredential(String accountName, String password) {
        this.accountName = accountName;
        this.password = password;
    }

    public Account login() throws IOException, LoginFailureException {
        LineClient client;
        try {
            client = new LineClient(accountName, password);
        } catch (TException ex) {
            throw new IOException(ex);
        } catch (Exception ex) {
            throw new LoginFailureException("Fail to login", ex);
        }
        return new Account(client);
    }
}
