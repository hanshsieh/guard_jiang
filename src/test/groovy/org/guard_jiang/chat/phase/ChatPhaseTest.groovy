package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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
        chatPhase = new ChatPhase(guard, account, userId, data) {
            @Override
            void onEnter() throws IOException {

            }

            @Override
            void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {

            }

            @Override
            void onReceiveTextMessage(@Nonnull String text) throws IOException {

            }
        }
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
