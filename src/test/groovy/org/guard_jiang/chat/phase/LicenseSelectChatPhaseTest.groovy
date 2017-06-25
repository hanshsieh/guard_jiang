package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.License
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

import java.time.Instant

/**
 * Test class for {@link LicenseSelectChatPhase}.
 */
class LicenseSelectChatPhaseTest extends Specification {

    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()
    LicenseSelectChatPhase licenseSelectChatPhase

    def setup() {
        licenseSelectChatPhase = Spy(LicenseSelectChatPhase, constructorArgs: [
                guard,
                account,
                userId,
                data
        ])
    }

    def "On enter, print the list of licenses"() {
        given:
        def guardId = "test_guard_id"
        def licenses = [
            new License("license_id_1", "AAAAABBBBBCCCCCDDDDDEEEEEFFFFFGGGGGHHHHH", userId, Instant.now()).with {
                numDefenders = 1
                maxDefenders = 10
                numSupporters = 2
                maxSupporters = 9
                return it
            },
            new License("license_id_2", "IIIIIJJJJJKKKKKLLLLLMMMMMNNNNNOOOOOPPPPP", userId, Instant.now()).with {
                numDefenders = 3
                maxDefenders = 8
                numSupporters = 4
                maxSupporters = 7
                return it
            }
        ]
        def data = licenseSelectChatPhase.data

        when:
        licenseSelectChatPhase.onEnter()

        then:
        _ * account.getMid() >> guardId
        1 * guard.getLicensesOfUser(userId) >> licenses
        1 * licenseSelectChatPhase.sendTextMessage({ String msg ->
            msg ==~ /(?s)1:\n.*AAAAA\.\.\..*defender.*9.*supporter.*7.*/
        } as String) >> {}
        1 * licenseSelectChatPhase.sendTextMessage({ String msg ->
            msg ==~ /(?s)2:\n.*IIIII\.\.\..*defender.*5.*supporter.*3.*/
        } as String) >> {}
        1 * licenseSelectChatPhase.sendTextMessage(_ as String) >> {}
        data == objectMapper.valueToTree([
            (LicenseSelectChatPhase.KEY_LICENSE_IDS): [
                "license_id_1",
                "license_id_2"
            ]
        ])
    }

    def "On enter, there's no licenses for the user"() {
        given:
        def guardId = "test_guard_id"
        def retData = objectMapper.valueToTree([
            (LicenseSelectChatPhase.RET_CANCELED): true
        ]) as ObjectNode

        when:
        licenseSelectChatPhase.onEnter()

        then:
        _ * account.getMid() >> guardId
        1 * guard.getLicensesOfUser(userId) >> []
        1 * licenseSelectChatPhase.sendTextMessage(_ as String) >> {}
        1 * licenseSelectChatPhase.leavePhase(retData) >> {}
    }

    def "On return, throw exception"() {
        given:
        def retStatus = ChatStatus.ACCOUNTS_SELECT
        def retData = objectMapper.createObjectNode()

        when:
        licenseSelectChatPhase.onReturn(retStatus, retData)

        then:
        thrown(IllegalStateException)
    }

    def "On receive text message, return the corresponding licence"() {
        given:
        def data = licenseSelectChatPhase.data
        data.set(LicenseSelectChatPhase.KEY_LICENSE_IDS, objectMapper.valueToTree([
            "license_id_1",
            "license_id_2"
        ]))
        def retData = objectMapper.valueToTree([
                (LicenseSelectChatPhase.RET_CANCELED): false,
                (LicenseSelectChatPhase.RET_LICENSE_ID): retLicenseId
        ]) as ObjectNode

        when:
        licenseSelectChatPhase.onReceiveTextMessage(msg)

        then:
        1 * licenseSelectChatPhase.leavePhase(retData) >> {}

        where:
        msg     | retLicenseId
        "1"     | "license_id_1"
        " 1\n " | "license_id_1"
        "\n1"   | "license_id_1"
        "2"     | "license_id_2"
    }

    def "On receive invalid text message, print message"() {
        given:
        def data = licenseSelectChatPhase.data
        data.set(LicenseSelectChatPhase.KEY_LICENSE_IDS, objectMapper.valueToTree([
                "license_id_1",
                "license_id_2"
        ]))

        when:
        licenseSelectChatPhase.onReceiveTextMessage(msg)

        then:
        1 * licenseSelectChatPhase.sendTextMessage(_ as String) >> {}

        where:
        msg     | _
        "hello" | _
        " 1abc" | _
        ""      | _
        " "     | _
        "0"     | _
        "3"     | _
    }

    def "On receive text message for cancel, leave the phase"() {
        given:
        def retData = objectMapper.valueToTree([
                (LicenseSelectChatPhase.RET_CANCELED): true,
        ]) as ObjectNode

        when:
        licenseSelectChatPhase.onReceiveTextMessage(msg)

        then:
        1 * licenseSelectChatPhase.leavePhase(retData) >> {}

        where:
        msg     | _
        "?"     | _
        " ?\n " | _
        "\n?"   | _
    }
}
