package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 2/3/2017.
 */
public class InMemoryStorage implements Storage {

    @Nonnull
    private Collection<AccountCredential> credentials = Collections.emptyList();

    @Nonnull
    private Map<Relation, Role> roles = Collections.emptyMap();

    public void setCredentials(@Nonnull Collection<AccountCredential> credentials) {
        this.credentials = Collections.unmodifiableCollection(credentials);
    }

    public void setRoles(@Nonnull Map<Relation, Role> roles) {
        this.roles = Collections.unmodifiableMap(roles);
    }

    @Override @Nonnull
    public Collection<AccountCredential> getAccountCredentials() throws IOException {
        return credentials;
    }

    @Override @Nonnull
    public Map<Relation, Role> getRoles() throws IOException{
        return roles;
    }
}
