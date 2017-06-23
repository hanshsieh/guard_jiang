package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.Role
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

/**
 * Test class for {@link GroupManageChatPhase}.
 */
class GroupManageChatPhaseTest extends Specification {

    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()
    GroupManageChatPhase groupManageChatPhase

    def setup() {
        groupManageChatPhase = Spy(GroupManageChatPhase, constructorArgs: [
            guard, account, userId, data
        ])
    }

    def "On enter, print menu list"() {
        given:
        def data = objectMapper.createObjectNode()
        def exData = objectMapper.valueToTree(["options":[1L, 2L, 3L, 0L]])

        when:
        groupManageChatPhase.onEnter()

        then:
        1 * groupManageChatPhase.sendTextMessage({ String str ->
            str ==~ /.*\n1: .*\n2: .*\n3: .*\n4: .*/
        } as String)
        1 * groupManageChatPhase.getData() >> data
        data == exData
    }

    def "On return, leave the phase"() {
        given:
        def returnData = objectMapper.createObjectNode()

        when:
        groupManageChatPhase.onReturn(returnStatus, returnData)

        then:
        1 * groupManageChatPhase.leavePhase() >> {}

        where:
        returnStatus << [ChatStatus.ROLE_MANAGE]
    }

    def "On receive text message, should start correct phase"() {
        given:
        def data = groupManageChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(GroupManageChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(option1.id)
        optionsNode.add(option2.id)

        def newPhaseData = objectMapper.createObjectNode()
        newPhaseData.put(RoleManageChatPhase.KEY_ROLE, role.getId())

        when:
        groupManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupManageChatPhase.startPhase(ChatStatus.ROLE_MANAGE, newPhaseData) >> {}

        where:
        msg     | option1                                       | option2                                       | role
        "2"     | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.MANAGE_SUPPORTERS | Role.SUPPORTER
        " 2\n"  | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.MANAGE_SUPPORTERS | Role.SUPPORTER
        " 2 "   | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.MANAGE_SUPPORTERS | Role.SUPPORTER
        "\n 2"  | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.MANAGE_SUPPORTERS | Role.SUPPORTER
        "1"     | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.MANAGE_SUPPORTERS | Role.DEFENDER
        "1"     | GroupManageChatPhase.Option.MANAGE_ADMINS     | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | Role.ADMIN
    }

    def "On receive text message for do nothing, leave the phase"() {
        given:
        def data = groupManageChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(UserMainMenuChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(option1.id)
        optionsNode.add(option2.id)

        when:
        groupManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupManageChatPhase.leavePhase() >> {}

        where:
        msg     | option1                                       | option2
        "2"     | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.DO_NOTHING
        " 2\n"  | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.DO_NOTHING
        "\n 2 " | GroupManageChatPhase.Option.MANAGE_DEFENDERS  | GroupManageChatPhase.Option.DO_NOTHING
        "1"     | GroupManageChatPhase.Option.DO_NOTHING        | GroupManageChatPhase.Option.MANAGE_DEFENDERS
    }

    def "On receive invalid text message, should print prompt message"() {
        given:
        groupManageChatPhase.onEnter()

        when:
        groupManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupManageChatPhase.sendTextMessage(_ as String) >> {}
        0 * groupManageChatPhase.leavePhase() >> {}

        where:
        msg << ["hello", "", "-1", "0", String.valueOf(GroupManageChatPhase.Option.values().size() + 1)]
    }

    def "On receive text message, cannot found the corresponding option ID"() {
        given:
        def data = groupManageChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(GroupManageChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(option1)

        when:
        groupManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * groupManageChatPhase.sendTextMessage(_ as String) >> {}
        1 * groupManageChatPhase.leavePhase() >> {}

        where:
        msg     | option1
        "1"     | -1
        "1"     | 4
    }

    def "On receive text message for do nothing, should leave the phase"() {
        given:
        def data = groupManageChatPhase.data
        def optionsNode = objectMapper.createArrayNode()
        data.set(GroupManageChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(option1.id)

        when:
        groupManageChatPhase.onReceiveTextMessage(msg)

        then:
        0 * groupManageChatPhase.sendTextMessage(_ as String) >> {}
        1 * groupManageChatPhase.leavePhase() >> {}

        where:
        msg     | option1
        "1"     | GroupManageChatPhase.Option.DO_NOTHING
    }
}
