package org.guard_jiang.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.chat.phase.ChatPhase
import org.guard_jiang.chat.phase.ChatPhaseFactory
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test class for {@link ChatManager}.
 */
class ChatManagerTest extends Specification {

    def guard = Mock(Guard)
    def account = Mock(Account)
    def objectMapper = Mock(ObjectMapper)
    def chatPhaseFactory = Mock(ChatPhaseFactory)
    ChatManager chatManager

    def setup() {
    }

    @Unroll
    def "Given empty stack, no default chat frame for #envType env"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(envType, userId)
        def chat = Mock(Chat)
        def stack = new ArrayDeque<ChatFrame>()
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack
        0 * _._

        where:
        envType << [ChatEnvType.GROUP, ChatEnvType.ROOM]
    }

    def "Given empty stack, receive a text message, and no calling and leaving"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(ChatEnvType.USER, userId)
        def chat = Mock(Chat)
        def stack = new ArrayDeque<ChatFrame>()
        def emptyData = Mock(ObjectNode)
        def newData = Mock(ObjectNode)
        def chatPhase = Mock(ChatPhase)
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack
        _ * objectMapper.createObjectNode() >> emptyData
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == ChatStatus.USER_MAIN_MENU &&
                chatFrame.data.is(emptyData)
        } as ChatFrame) >> chatPhase
        1 * chatPhase.onEnter()
        1 * chatPhase.onReceiveTextMessage("test_text")
        _ * chatPhase.isLeaving() >> false
        _ * chatPhase.isCalling() >> false
        _ * chatPhase.getData() >> newData
        1 * guard.setChat({ Chat arg ->
            arg.stack.size() == 1 &&
                arg.stack[0].with {
                    chatStatus == ChatStatus.USER_MAIN_MENU &&
                    data.is(newData)
                }
        } as Chat)
        0 * _._
    }

    def "Given empty stack, receive a text message, and there's calling"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(ChatEnvType.USER, userId)
        def chat = Mock(Chat)
        def stack = new ArrayDeque<ChatFrame>()
        def emptyData = Mock(ObjectNode)
        def newData = Mock(ObjectNode)
        def chatPhase = Mock(ChatPhase)
        def newChatStatus = ChatStatus.ACCOUNTS_SELECT
        def newChatPhaseData = Mock(ObjectNode)
        def newChatPhase = Mock(ChatPhase)
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack
        _ * objectMapper.createObjectNode() >> emptyData
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == ChatStatus.USER_MAIN_MENU &&
                    chatFrame.data.is(emptyData)
        } as ChatFrame) >> chatPhase
        1 * chatPhase.onReceiveTextMessage("test_text")
        _ * chatPhase.isLeaving() >> false
        _ * chatPhase.isCalling() >> true
        _ * chatPhase.getData() >> newData
        _ * chatPhase.getNewPhaseStatus() >> newChatStatus
        _ * chatPhase.getNewPhaseData() >> newChatPhaseData
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == newChatStatus &&
                    chatFrame.data.is(newChatPhaseData)
        } as ChatFrame) >> newChatPhase
        1 * chatPhase.onEnter()
        1 * newChatPhase.onEnter()
        _ * newChatPhase.getData() >> newChatPhaseData
        _ * newChatPhase.isLeaving() >> false
        _ * newChatPhase.isCalling() >> false
        1 * guard.setChat({ Chat arg ->
            arg.stack.size() == 2 &&
                arg.stack[0].with {
                    chatStatus == ChatStatus.USER_MAIN_MENU &&
                        data.is(newData)
                } &&
                arg.stack[1].with {
                    chatStatus == newChatStatus &&
                        data.is(newChatPhaseData)
                }
        } as Chat)
        0 * _._
    }

    def "Given empty stack, receive a text message, and there is leaving"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(ChatEnvType.USER, userId)
        def chat = Mock(Chat)
        def stack = new ArrayDeque<ChatFrame>()
        def emptyData = Mock(ObjectNode)
        def newData = Mock(ObjectNode)
        def chatPhase = Mock(ChatPhase)
        def returnData = Mock(ObjectNode)
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack
        _ * objectMapper.createObjectNode() >> emptyData
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == ChatStatus.USER_MAIN_MENU &&
                    chatFrame.data.is(emptyData)
        } as ChatFrame) >> chatPhase
        1 * chatPhase.onEnter()
        1 * chatPhase.onReceiveTextMessage("test_text")
        _ * chatPhase.isLeaving() >> true
        _ * chatPhase.isCalling() >> false
        _ * chatPhase.getData() >> newData
        _ * chatPhase.getReturnData() >> returnData
        1 * guard.setChat({ Chat arg ->
            arg.stack.isEmpty()
        } as Chat)
        0 * _._
    }

    def "Given non-empty stack, receive a text messages, and we are entering, and then leaving"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(ChatEnvType.USER, userId)
        def chat = Mock(Chat)
        def firstChatData = Mock(ObjectNode)
        def stack = new ArrayDeque<ChatFrame>()
        stack.addLast(new ChatFrame(firstChatStatus, firstChatData))
        def newFirstPhaseData = Mock(ObjectNode)
        def firstChatPhase = Mock(ChatPhase)
        def newChatPhaseData = Mock(ObjectNode)
        def newChatPhase = Mock(ChatPhase)
        def newPhaseReturnData = Mock(ObjectNode)
        def firstChatPhaseAfterReturn = Mock(ChatPhase)
        def firstPhaseAfterReturnNewData = Mock(ObjectNode)
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack

        // Create first chat phase from the stack frame
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == firstChatStatus &&
                    chatFrame.data.is(firstChatData)
        } as ChatFrame) >> firstChatPhase

        // Handle the message
        1 * firstChatPhase.onReceiveTextMessage("test_text")
        _ * firstChatPhase.isLeaving() >> false

        // The phase wants to enter another phase
        // The 2nd stack frame is pushed
        _ * firstChatPhase.isCalling() >> true
        _ * firstChatPhase.getData() >> newFirstPhaseData
        _ * firstChatPhase.getNewPhaseStatus() >> newChatStatus
        _ * firstChatPhase.getNewPhaseData() >> newChatPhaseData

        // Create chat phase for the new chat frame
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == newChatStatus &&
                    chatFrame.data.is(newChatPhaseData)
        } as ChatFrame) >> newChatPhase

        // Enter the new chat phase
        1 * newChatPhase.onEnter()
        _ * newChatPhase.getData() >> newChatPhaseData

        // The new phase wants to return
        _ * newChatPhase.isLeaving() >> true
        _ * newChatPhase.isCalling() >> false
        _ * newChatPhase.getReturnData() >> newPhaseReturnData

        // The 2nd stack frame is popped. Create chat phase from the 1st stack frame
        1 * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == firstChatStatus &&
                    chatFrame.data.is(newFirstPhaseData)
        } as ChatFrame) >> firstChatPhaseAfterReturn

        // Return to the 1st chat phase
        1 * firstChatPhaseAfterReturn.onReturn(newChatStatus, newPhaseReturnData)
        _ * firstChatPhaseAfterReturn.getData() >> firstPhaseAfterReturnNewData

        // The 1st chat phae doesn't want to enter or leave
        _ * firstChatPhaseAfterReturn.isCalling() >> false
        _ * firstChatPhaseAfterReturn.isLeaving() >> false

        // Save the chat
        1 * guard.setChat({ Chat arg ->
            arg.stack.size() == 1 &&
                arg.stack[0].with {
                    chatStatus == firstChatStatus &&
                        data == firstPhaseAfterReturnNewData
                }
        } as Chat)
        0 * _._

        where:
        firstChatStatus             | newChatStatus
        ChatStatus.LICENSE_CREATE   | ChatStatus.LICENSE_CREATE
        ChatStatus.ROLE_MANAGE      | ChatStatus.GROUP_SELECT
    }

    def "Given non-empty stack, number of phase changing exceeds limit"() {
        given:
        def userId = "test_user_id"
        def chatEnv = new ChatEnv(ChatEnvType.USER, userId)
        def chat = Mock(Chat)
        def firstChatData = Mock(ObjectNode)
        def stack = new ArrayDeque<ChatFrame>()
        stack.addLast(new ChatFrame(firstChatStatus, firstChatData))
        def firstChatPhase = Mock(ChatPhase)
        def newChatPhaseData = Mock(ObjectNode)
        def newChatPhase = Mock(ChatPhase)
        def newPhaseReturnData = Mock(ObjectNode)
        chatManager = new ChatManager(
                guard,
                account,
                chatEnv,
                userId,
                objectMapper,
                chatPhaseFactory
        )
        def limit = ChatManager.MAX_ROUND_PER_MESSAGE

        when:
        chatManager.onReceiveTextMessage("test_text")

        then:
        _ * account.getMid() >> "test_mid"
        1 * guard.getChat("test_mid", userId, chatEnv) >> chat
        _ * chat.getStack() >> stack

        // Create first chat phase from the stack frame
        _ * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == firstChatStatus &&
                    chatFrame.data.is(firstChatData)
        } as ChatFrame) >> firstChatPhase

        // Handle the message
        1 * firstChatPhase.onReceiveTextMessage("test_text")
        _ * firstChatPhase.isLeaving() >> false

        // The phase wants to enter another phase
        // The 2nd stack frame is pushed
        _ * firstChatPhase.isCalling() >> true
        _ * firstChatPhase.getData() >> firstChatData
        _ * firstChatPhase.getNewPhaseStatus() >> newChatStatus
        _ * firstChatPhase.getNewPhaseData() >> newChatPhaseData

        // Create chat phase for the new chat frame
        _ * chatPhaseFactory.createChatPhase({ ChatFrame chatFrame ->
            chatFrame.chatStatus == newChatStatus &&
                    chatFrame.data.is(newChatPhaseData)
        } as ChatFrame) >> newChatPhase

        // Enter the new chat phase
        Math.ceil(limit / 2.0) * newChatPhase.onEnter()
        _ * newChatPhase.getData() >> newChatPhaseData

        // The new phase wants to return
        _ * newChatPhase.isLeaving() >> true
        _ * newChatPhase.isCalling() >> false
        _ * newChatPhase.getReturnData() >> newPhaseReturnData

        // First phase get return, and repeat...
        Math.floor(limit / 2.0) * firstChatPhase.onReturn(newChatStatus, newPhaseReturnData)

        // Exceeding limit, send error message
        1 * account.sendTextMessage(_ as String, userId)

        // Store empty stack
        1 * guard.setChat({ Chat arg ->
            arg.stack.isEmpty()
        } as Chat)
        0 * _._

        where:
        firstChatStatus             | newChatStatus
        ChatStatus.LICENSE_CREATE   | ChatStatus.LICENSE_CREATE
        ChatStatus.ROLE_MANAGE      | ChatStatus.GROUP_SELECT
    }
}
