package com.handoitadsf.line.group_guard;

import com.typesafe.config.Config;
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
        /*LineClient client = new LineClient();
        client.login("blueskywalk@outlook.com",
                "u4sax7zzfbq4b66",
                "9bbd9bc8a9c50873b8d70172c5d808095756dc8f25bb42a915cbcf79f079e18d", null);
        System.out.println("Auth token: " + client.getAuthToken());
        client.getApi().updateAuthToken();
        System.out.println("Auth token: " + client.getAuthToken());
        client.refreshGroups();
        client.refreshGroups();
        long revision = 0;
        while (true) {
            try {
                System.out.println("Polling with revision " + revision);
                //List<Operation> operations = client.getApi().fetchOps(revision, 50, revision, Integer.MAX_VALUE);
                List<Operation> operations = client.getApi().fetchOperations(revision, 50);
                for (Operation operation : operations) {
                    System.out.println(operation);
                    revision = Long.max(revision, operation.getRevision());
                }
                LineApiImpl api = (LineApiImpl) client.getApi();
                api.close();
                api.setClient(api.ready());
            } catch (Exception ex) {
                System.out.println("Get exception: " + ex);
            }
        }*/
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
            guardBuilder.addGroup(groupId);
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
            String id = accountConf.getString("id");
            String loginId = accountConf.getString("loginId");
            String password = accountConf.getString("password");
            String certificate = accountConf.getString("certificate");
            LOGGER.info("Login for {}", loginId);
            AccountCredential credential = new PasswordCredential(
                    loginId,
                    password,
                    certificate);
            Account account = credential.login(
                    pinCode -> System.out.println("Please enter pin code on your device: " + pinCode));
            String realId = account.getProfile().getMid();
            if (!realId.equals(id)) {
                throw new RuntimeException("MID of user " + realId + " doesn't match expected " + id);
            }
            guardBuilder.addAccount(account);
        }
    }
}
