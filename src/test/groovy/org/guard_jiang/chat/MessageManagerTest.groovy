package org.guard_jiang.chat

import line.thrift.ContentType
import line.thrift.MIDType
import line.thrift.Message
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
                {ChatEnv chatEnv -> chatEnv.id == "test_from_id" && chatEnv.type == ChatEnvType.USER} as ChatEnv,
                "test_from_id"
        ) >> chatManager
        1 * chatManager.onReceiveMessage(msg)
        0 * _._
    }
}
