package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import line.thrift.Group
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

/**
 * Test class for {@link GroupSelectChatPhase}.
 */
class GroupSelectChatPhaseTest extends Specification {
    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()
    GroupSelectChatPhase groupSelectChatPhase

    def setup() {
        groupSelectChatPhase = Spy(GroupSelectChatPhase, constructorArgs: [
                guard,
                account,
                userId,
                data
        ])
    }

    def "On enter, print instruction"() {
        when:
        groupSelectChatPhase.onEnter()

        then:
        1 * groupSelectChatPhase.sendTextMessage(_ as String) >> {}
    }

    def "On return, throw exception"() {
        given:
        def retStatus = ChatStatus.ACCOUNTS_SELECT
        def retData = objectMapper.createObjectNode()

        when:
        groupSelectChatPhase.onReturn(retStatus, retData)

        then:
        thrown(IllegalStateException)
    }

    def "On receive text message for cancel, leave the phase"() {
        given:
        def retData = objectMapper.valueToTree([
                (GroupSelectChatPhase.RET_CANCELED): true,
        ]) as ObjectNode

        when:
        groupSelectChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupSelectChatPhase.leavePhase(retData) >> {}

        where:
        msg     | _
        "?"     | _
        " ?\n " | _
        "\n?"   | _
    }

    def "On receive text message, get group ID from the URL"() {
        given:
        def retData = objectMapper.valueToTree([
                (GroupSelectChatPhase.RET_CANCELED): false,
                (GroupSelectChatPhase.RET_GROUP_ID): "test_group_id"
        ]) as ObjectNode
        def group = new Group(id: "test_group_id")

        when:
        groupSelectChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupSelectChatPhase.leavePhase(retData) >> {}
        1 * account.findGroupByTicket(ticketId) >> group

        where:
        msg                                           | ticketId
        "http://line.me/ti/g/abcdef"                  | "abcdef"
        "http://line.me/R/ti/g/abcdef"                | "abcdef"
        "http://line.me/abcdef"                       | "abcdef"
        "http://line.me:80/abcdef"                    | "abcdef"
        "https://line.me:443/abcdef#haha?hello=world" | "abcdef"
    }

    def "On receive text message, the URL is invalid"() {
        when:
        groupSelectChatPhase.onReceiveTextMessage(msg)

        then:
        0 * groupSelectChatPhase.leavePhase(_ as ObjectNode) >> {}
        0 * account.findGroupByTicket(_ as String)
        1 * groupSelectChatPhase.sendTextMessage(_ as String) >> {}

        where:
        msg                                 | _
        "http://line1.me/ti/g/abcdef"       | _
        "http://line.me"                    | _
        "http://line.me/"                   | _
        "hello"                             | _
    }

    def "On receive text message, cannot find group of a ticket ID"() {
        when:
        groupSelectChatPhase.onReceiveTextMessage("http://line.me/ti/g/abcdef")

        then:
        0 * groupSelectChatPhase.leavePhase(_ as ObjectNode) >> {}
        1 * account.findGroupByTicket("abcdef") >> {throw new IOException()}
        1 * groupSelectChatPhase.sendTextMessage(_ as String) >> {}
    }
}
