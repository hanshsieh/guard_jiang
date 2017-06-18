package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.node.ObjectNode
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.License
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

/**
 * Test class for {@link LicenseCreationChatPhase}.
 */
class LicenseCreationChatPhaseTest extends Specification {

    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = Mock(ObjectNode)
    LicenseCreationChatPhase licenseCreationChatPhase

    def setup() {
        licenseCreationChatPhase = Spy(LicenseCreationChatPhase, constructorArgs: [
                guard,
                account,
                userId,
                data]
        )
    }

    def "On enter, create a trial license for the user"() {
        given:
        def license = Mock(License)

        when:
        licenseCreationChatPhase.onEnter()

        then:
        _ * account.getMid() >> "test_guard_id"
        1 * guard.getLicensesOfUser("test_guard_id") >> []
        1 * guard.createTrialLicense("test_user_id") >> license
        1 * license.getReadableKey() >> "license_key_in_readable_form"
        1 * licenseCreationChatPhase.sendTextMessage({it.contains("license_key_in_readable_form")} as String)
        1 * licenseCreationChatPhase.sendTextMessage(_ as String)
        1 * licenseCreationChatPhase.leavePhase() >> {}
    }

    def "On return, leave the phase"() {
        given:
        def returnStatus = ChatStatus.GROUP_MANAGE
        def returnData = Mock(ObjectNode)

        when:
        licenseCreationChatPhase.onReturn(returnStatus, returnData)

        then:
        1 * licenseCreationChatPhase.leavePhase() >> {}
    }

    def "On receive text message, leave the phase"() {
        when:
        licenseCreationChatPhase.onReceiveTextMessage("test_message")

        then:
        1 * licenseCreationChatPhase.leavePhase() >> {}
    }
}
