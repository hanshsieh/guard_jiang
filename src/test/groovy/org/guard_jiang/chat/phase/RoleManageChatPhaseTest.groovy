package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import org.guard_jiang.Account
import org.guard_jiang.Guard
import org.guard_jiang.Role
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification

/**
 * Test class for {@link RoleManageChatPhase}.
 */
class RoleManageChatPhaseTest extends Specification {

    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()
    RoleManageChatPhase roleManageChatPhase

    def setup() {
        roleManageChatPhase = Spy(RoleManageChatPhase, constructorArgs: [
                guard,
                account,
                userId,
                data
            ])
    }

    def "On enter, print menu list"(Role role) {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, role.id)

        when:
        roleManageChatPhase.onEnter()

        then:
        1 * roleManageChatPhase.sendTextMessage({ String str ->
            str ==~ /.*${role.name().toLowerCase()}.*\n1: [^\n]+\n2: [^\n]+\n3: [^\n]+/
        } as String) >> {}

        where:
        role << Role.values()
    }

    def "On enter, no role in data"() {
        when:
        roleManageChatPhase.onEnter()

        then:
        thrown(NullPointerException)
    }

    def "On enter, role ID in data isn't correct"() {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, roleId)

        when:
        roleManageChatPhase.onEnter()

        then:
        thrown(IllegalArgumentException)

        where:
        roleId << [-1, 100]
    }

    def "On return, leave the phase"() {
        given:
        def returnData = objectMapper.createObjectNode()

        when:
        roleManageChatPhase.onReturn(returnStatus, returnData)

        then:
        1 * roleManageChatPhase.leavePhase() >> {}

        where:
        returnStatus << [ChatStatus.ROLE_MANAGE]
    }

    def "On receive text message, should start correct phase"() {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, role.id)
        roleManageChatPhase.onEnter()

        def newPhaseData = objectMapper.createObjectNode()
        newPhaseData.put(newPhaseRoleKey, role.id)

        when:
        roleManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * roleManageChatPhase.startPhase(newPhase, newPhaseData) >> {}

        where:
        msg     | role           | newPhase                | newPhaseRoleKey
        "1"     | Role.SUPPORTER | ChatStatus.ROLES_ADD    | RolesAddChatPhase.ARG_ROLE_ID
        " 2\n"  | Role.DEFENDER  | ChatStatus.ROLES_REMOVE | RolesRemoveChatPhase.ARG_ROLE_ID
    }

    def "On receive invalid text message, should print prompt message"() {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, Role.DEFENDER.id)
        roleManageChatPhase.onEnter()

        when:
        roleManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * roleManageChatPhase.sendTextMessage(_ as String) >> {}
        0 * roleManageChatPhase.leavePhase() >> {}

        where:
        msg << ["hello", "", "-1", "0", String.valueOf(RoleManageChatPhase.Option.values().size() + 1)]
    }

    def "On receive text message, cannot found the corresponding option ID"() {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, Role.DEFENDER.id)
        def optionsNode = objectMapper.createArrayNode()
        data.set(RoleManageChatPhase.KEY_OPTIONS, optionsNode)
        optionsNode.add(optionId)

        when:
        roleManageChatPhase.onReceiveTextMessage(msg)

        then:
        thrown(IllegalArgumentException)

        where:
        msg     | optionId
        "1"     | -1
        "1"     | 3
    }

    def "On receive text message for do nothing, should leave phase"() {
        given:
        def data = roleManageChatPhase.data
        data.put(RoleManageChatPhase.ARG_ROLE_ID, role.id)
        roleManageChatPhase.onEnter()

        when:
        roleManageChatPhase.onReceiveTextMessage(msg)

        then:
        1 * roleManageChatPhase.leavePhase() >> {}

        where:
        msg     | role
        "3"     | Role.SUPPORTER
    }
}
