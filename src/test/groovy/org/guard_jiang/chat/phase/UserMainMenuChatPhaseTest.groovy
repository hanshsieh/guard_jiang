package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

/**
 * Test class for {@link UserMainMenuChatPhase}.
 */
class UserMainMenuChatPhaseTest extends Specification {
    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()
    UserMainMenuChatPhase userMainMenuChatPhase
    def setup() {
        userMainMenuChatPhase = Spy(UserMainMenuChatPhase, constructorArgs: [
                guard,
                account,
                userId,
                data]
        )
    }

    def "On enter, print menu list"() {
        given:
        def data = objectMapper.createObjectNode()
        def exOptionsNode = objectMapper.createArrayNode().with {
            UserMainMenuChatPhase.Option.values().each { UserMainMenuChatPhase.Option option ->
                add(option.id)
            }
            return it
        }
        def exData = objectMapper.createObjectNode().with {
            set(UserMainMenuChatPhase.KEY_OPTIONS, exOptionsNode)
            return it
        }

        when:
        userMainMenuChatPhase.onEnter()

        then:
        1 * userMainMenuChatPhase.sendTextMessage({ String str ->
            str ==~ /.*\n1: .*\n2: .*\n3: .*/
        } as String)
        1 * userMainMenuChatPhase.getData() >> data
        println data
        println exData
        data == exData
    }

    def "On return, print menu list"() {
        given:
        def returnStatus = ChatStatus.GROUP_MANAGE
        def returnData = Mock(ObjectNode)

        when:
        userMainMenuChatPhase.onReturn(returnStatus, returnData)

        then:
        1 * userMainMenuChatPhase.sendTextMessage({ String str ->
            str ==~ /.*\n1: .*\n2: .*\n3: .*/
        } as String)
    }

    def "On receive text message, should start correct phase"() {
        given:
        def data = userMainMenuChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(UserMainMenuChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(UserMainMenuChatPhase.Option.DO_NOTHING.id)
        optionsNode.add(UserMainMenuChatPhase.Option.MANAGE_GROUP.id)
        optionsNode.add(UserMainMenuChatPhase.Option.CREATE_LICENSE.id)

        when:
        userMainMenuChatPhase.onReceiveTextMessage(msg)

        then:
        1 * userMainMenuChatPhase.startPhase(newChatStatus) >> {}

        where:
        msg     | newChatStatus
        "3"     | ChatStatus.LICENSE_CREATE
        " 3\n"  | ChatStatus.LICENSE_CREATE
        " 3 "   | ChatStatus.LICENSE_CREATE
        "\n 3"  | ChatStatus.LICENSE_CREATE
        "2"     | ChatStatus.GROUP_MANAGE
    }

    def "On receive text message for do nothing, leave the phase"() {
        given:
        def data = userMainMenuChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(UserMainMenuChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(UserMainMenuChatPhase.Option.MANAGE_GROUP.id)
        optionsNode.add(UserMainMenuChatPhase.Option.DO_NOTHING.id)

        when:
        userMainMenuChatPhase.onReceiveTextMessage(msg)

        then:
        1 * userMainMenuChatPhase.sendTextMessage(_ as String) >> {}
        1 * userMainMenuChatPhase.leavePhase() >> {}

        where:
        msg << ["2", " 2\n", "\n 2 "]
    }

    def "On receive invalid text message, send prompt messages"() {
        given:
        def data = userMainMenuChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(UserMainMenuChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(UserMainMenuChatPhase.Option.MANAGE_GROUP.id)
        optionsNode.add(UserMainMenuChatPhase.Option.DO_NOTHING.id)

        when:
        userMainMenuChatPhase.onReceiveTextMessage(msg)

        then:
        2 * userMainMenuChatPhase.sendTextMessage(_ as String) >> {}
        0 * userMainMenuChatPhase.leavePhase() >> {}

        where:
        msg << ["abc", "3", "-1", "0"]
    }

    def "On receive text message, but the option ID stored in the data isn't correct"() {
        given:
        def data = userMainMenuChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(UserMainMenuChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(100)

        when:
        userMainMenuChatPhase.onReceiveTextMessage("1")

        then:
        1 * userMainMenuChatPhase.sendTextMessage(_ as String) >> {}
        1 * userMainMenuChatPhase.leavePhase() >> {}
    }
}
