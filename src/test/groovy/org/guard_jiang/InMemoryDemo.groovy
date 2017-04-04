package org.guard_jiang

import org.guard_jiang.storage.InMemoryStorage
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import groovy.util.logging.Slf4j

/**
 * Created by someone on 3/25/2017.
 */
@Slf4j
class InMemoryDemo {
    private final InMemoryStorage storage
    private final Config config
    private final List malicious = []
    public static void main(String[] args) throws Exception {
        new InMemoryDemo();
    }

    public InMemoryDemo() {
        storage = new InMemoryStorage();
        config = ConfigFactory.load("config.conf");
        loadAccounts();
        loadGroups()
        def guard = new Guard(storage)
        guard.start()
        if (malicious.isEmpty()) {
            return
        }
        log.info("Sleep a while...")
        sleep(1000 * 30)
        log.info("Starting malicious accounts...")
        malicious.each { List entry ->
            def account = entry[0] as Account
            account.login()
            def overthrows = entry[1] as List<String>
            overthrows.each {String overthrow ->
                def group = account.getGroup(overthrow)
                if (group == null) {
                    log.error("Account ${account.mid} isn't a member of group $overthrow")
                    return
                }
                def otherMemberIds = group.getMembers()
                        .collect({it.getMid()})
                        .findAll {it != account.mid}
                try {
                    otherMemberIds.each {def otherMemberId ->
                        account.kickOutFromGroup(overthrow, otherMemberId)
                    }
                } catch (Exception ex) {
                    log.error("Fail to overthrow group $overthrow", ex)
                }

            }
        }
    }

    private void loadAccounts() {
        def accountsConf = config.getConfigList("accounts");
        accountsConf.each { def accountConf ->
            String mid = accountConf.getString("mid")
            String email = accountConf.getString("email")
            String password = accountConf.getString("password")
            String certificate = accountConf.getString("certificate")
            String authToken = accountConf.getString("authToken")
            List<String> overthrow = accountConf.getStringList("overthrow")
            Credential credential = new Credential()
            credential.setEmail(email)
            credential.setPassword(password)
            credential.setCertificate(certificate)
            credential.setAuthToken(authToken)
            if (overthrow.isEmpty()) {
                storage.setCredential(mid, credential)
            } else {
                def account = new Account(credential)
                malicious << [account, overthrow]
            }
        }
    }

    private void loadGroups() {
        def groupsConf = config.getConfigList("groups")
        groupsConf.each { def groupConf ->
            String groupId = groupConf.getString("id")
            List<String> defenders = groupConf.getStringList("defenders")
            List<String> supporters = groupConf.getStringList("supporters")
            List<String> admins = groupConf.getStringList("admins")
            defenders.each { String defender ->
                storage.setGroupRole(groupId, defender, Role.DEFENDER)
            }
            supporters.each { def supporter ->
                storage.setGroupRole(groupId, supporter, Role.SUPPORTER)
            }
            admins.each { def admin ->
                storage.setGroupRole(groupId, admin, Role.ADMIN)
            }

        }
    }
}
