package com.handoitadsf.line.group_guard

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import groovy.util.logging.Slf4j
import io.cslinmiso.line.model.LineClient

/**
 * Created by someone on 3/25/2017.
 */
@Slf4j
class AuthGetter {
    private final InMemoryStorage storage
    private final Config config
    public static void main(String[] args) throws Exception {
        new AuthGetter();
    }

    public AuthGetter() {
        storage = new InMemoryStorage();
        config = ConfigFactory.load("config.conf");
        def accountsConf = config.getConfigList("accounts");
        accountsConf.each { def accountConf ->
            String mid = accountConf.getString("mid")
            String email = accountConf.getString("email")
            String password = accountConf.getString("password")
            def client = new LineClient()
            println "mid: $mid"
            println "email: $email"
            client.login(email, password, null, {println "Pin code: $it"})
            println "certificate: ${client.getCertificate()}"
            println "Auth token: ${client.authToken}"
            println "======================"
        }
    }
}
