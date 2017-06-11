package org.guard_jiang.chat

import line.thrift.ContentType
import line.thrift.MIDType
import line.thrift.Message
import org.guard_jiang.Account
import org.guard_jiang.Guard
import spock.lang.Specification

/**
 * Test class for {@link MessageManager}.
 */
class MessageManagerTest extends Specification {

    def chatManagerFactory = Mock(ChatManagerFactory)
    MessageManager messageManager

    def setup() {
        messageManager = new MessageManager(chatManagerFactory)
    }

    def "Handle user message"() {
        given:
        def msg = new Message().with {
            toType = MIDType.USER
            fromId = "test_from_id"
            toId = "test_to_id"
            text = "test_text"
            contentType = ContentType.NONE
            return it
        }
        def chatManager = Mock(ChatManager)

        when:
        messageManager.onReceiveMessage(msg)

        then:
        1 * chatManagerFactory.createChatManager(
                {ChatEnv chatEnv -> chatEnv.id == "test_to_id" && chatEnv.type == ChatEnvType.USER} as ChatEnv,
                "test_from_id"
        ) >> chatManager
        1 * chatManager.onReceiveTextMessage("test_text")
        0 * _._
    }

    def "Non user messages and non-text messages should be ignored"() {
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
        messageManager.onReceiveMessage(msg)

        then:
        0 * _._

        where:
        toType          | contentType         | text
        MIDType.USER    | ContentType.NONE    | null
        MIDType.USER    | ContentType.IMAGE   | "apple"
        MIDType.GROUP   | ContentType.NONE    | "apple"
        MIDType.ROOM    | ContentType.NONE    | "apple"
    }
}
