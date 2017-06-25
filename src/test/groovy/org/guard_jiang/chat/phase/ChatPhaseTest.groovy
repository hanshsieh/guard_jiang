package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import line.thrift.ContentType
import line.thrift.MIDType
import line.thrift.Message
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

import javax.annotation.Nonnull

/**
 * Test class of {@link ChatPhase}.
 */
class ChatPhaseTest extends Specification {

    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = new ObjectMapper().createObjectNode().with {
        put("hello", "world")
        return it
    }
    ChatPhase chatPhase

    def setup() {
        chatPhase = Spy(ChatPhase, constructorArgs: [guard, account, userId, data])
    }


    def "On receive text message"() {
        given:
        def msg = new Message().with {
            it.toType = toType
            it.fromId = "test_from_id"
            it.toId = "test_to_id"
            it.text = "apple"
            it.contentType = ContentType.NONE
            return it
        }

        when:
        chatPhase.onReceiveMessage(msg)

        then:
        1 * chatPhase.onReceiveTextMessage("apple") >> {}

        where:
        toType        | _
        MIDType.USER  | _
        MIDType.GROUP | _
        MIDType.USER  | _
    }

    def "On receive text message, but the text is null"() {
        given:
        def msg = new Message().with {
            it.toType = toType
            it.fromId = "test_from_id"
            it.toId = "test_to_id"
            it.text = null
            it.contentType = ContentType.NONE
            return it
        }

        when:
        chatPhase.onReceiveMessage(msg)

        then:
        0 * chatPhase.onReceiveTextMessage(_ as String) >> {}

        where:
        toType        | _
        MIDType.USER  | _
        MIDType.GROUP | _
        MIDType.USER  | _
    }

    def "On receive non-text messages, should ignore"() {
        given:
        def msg = new Message().with {
            it.toType = toType
            it.fromId = "test_from_id"
            it.toId = "test_to_id"
            it.text = text
            it.contentType = contentType
            return it
        }

        when:
        chatPhase.onReceiveMessage(msg)

        then:
        0 * chatPhase.onReceiveTextMessage(_ as String) >> {}

        where:
        toType        | contentType       | text
        MIDType.USER  | ContentType.IMAGE | "apple"
        MIDType.GROUP | ContentType.IMAGE | "apple"
        MIDType.USER  | ContentType.CALL  | "apple"
    }

    def "Can send text message"() {
        when:
        chatPhase.sendTextMessage("hello world")

        then:
        1 * account.sendTextMessage("hello world", userId)
    }

    def "Get guard"() {
        when:
        def ret = chatPhase.getGuard()

        then:
        ret.is(guard)
    }

    def "Get account"() {
        when:
        def ret = chatPhase.getAccount()

        then:
        ret.is(account)
    }

    def "Start another phase"() {
        given:
        def newData = Mock(ObjectNode)

        when:
        chatPhase.startPhase(newChatStatus as ChatStatus, newData)

        then:
        chatPhase.getNewPhaseStatus() == newChatStatus
        chatPhase.getNewPhaseData().is(newData)

        where:
        newChatStatus << ChatStatus.values()
    }

    def "Start another phase without data"() {
        when:
        chatPhase.startPhase(newChatStatus as ChatStatus)

        then:
        chatPhase.getNewPhaseStatus() == newChatStatus
        chatPhase.getNewPhaseData().size() == 0
        chatPhase.isCalling()

        where:
        newChatStatus << ChatStatus.values()
    }

    def "Leave the phase"() {
        given:
        def returnData = Mock(ObjectNode)

        when:
        chatPhase.leavePhase(returnData)

        then:
        chatPhase.getReturnData().is(returnData)
        chatPhase.isLeaving()
    }

    def "Leave the phase without return data"() {
        when:
        chatPhase.leavePhase()

        then:
        chatPhase.getReturnData().size() == 0
        chatPhase.isLeaving()
    }
}
