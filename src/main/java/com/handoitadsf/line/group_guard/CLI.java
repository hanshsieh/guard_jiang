package com.handoitadsf.line.group_guard;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import io.cslinmiso.line.api.impl.LineApiImpl;
import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;
import io.cslinmiso.line.model.LoginCallback;
import line.thrift.OpType;
import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cahsieh on 1/26/17.
 */
public class CLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);
    private final Config config;
    public static void main(String[] args) throws Exception {
        new CLI(args);
    }
    public CLI(String[] args) throws Exception {
        config = ConfigFactory.load("config.conf");
        GuardBuilder guardBuilder = new GuardBuilder();
        addAccounts(guardBuilder);
        addGroups(guardBuilder);
        Guard guard = guardBuilder.build();
        guard.start();
    }

    private void addGroups(@Nonnull GuardBuilder guardBuilder) {
        List<? extends Config> groupsConfig = config.getConfigList("groups");
        for (Config groupConfig : groupsConfig) {
            String groupId = groupConfig.getString("id");
            guardBuilder.addGroup(new GroupProfile(groupId));
            List<String> defenders = groupConfig.getStringList("defenders");
            for (String defender : defenders) {
                guardBuilder.addRole(defender, groupId, Role.DEFENDER);
            }
            List<String> supporters = groupConfig.getStringList("supporters");
            for (String supporter : supporters) {
                guardBuilder.addRole(supporter, groupId, Role.SUPPORTER);
            }
        }
    }

    private void addAccounts(@Nonnull GuardBuilder guardBuilder) throws IOException, LoginFailureException {
        List<? extends Config> accountsConfig = config.getConfigList("accounts");
        for (Config accountConf : accountsConfig) {
            AccountCredential credential = new AccountCredential();
            String mid = accountConf.getString("mid");
            LOGGER.info("Login for {}", mid);
            try {
                String authToken = accountConf.getString("authToken");
                credential.setAuthToken(authToken);
            } catch(ConfigException.Missing ex) {
                credential.setAuthToken(null);
            }
            String email = accountConf.getString("email");
            String password = accountConf.getString("password");
            String certificate = accountConf.getString("certificate");
            credential.setEmail(email);
            credential.setPassword(password);
            credential.setCertificate(certificate);
            credential.setLoginCallback(pinCode -> System.out.printf("Enter pin code %s", pinCode));

            Account account = new Account(credential);
            account.login();

            LOGGER.debug("Certificate: {}", account.getCertificate());
            LOGGER.debug("Auth token: {}", account.getAuthToken());

            String realId = account.getMid();
            if (!realId.equals(mid)) {
                throw new RuntimeException("MID of user " + realId + " doesn't match expected " + mid);
            }
            guardBuilder.addAccount(account);
        }
    }
}
