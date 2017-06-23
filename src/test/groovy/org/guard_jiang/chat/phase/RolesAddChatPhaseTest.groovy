package org.guard_jiang.chat.phase

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.guard_jiang.Account
import org.guard_jiang.GroupRole
import org.guard_jiang.Guard
import org.guard_jiang.GuardGroup
import org.guard_jiang.License
import org.guard_jiang.Role
import org.guard_jiang.chat.AccountSelectChatPhase
import org.guard_jiang.chat.ChatStatus
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

/**
 * Test class for {@link RolesAddChatPhase}.
 */
class RolesAddChatPhaseTest extends Specification {

    def objectMapper = new ObjectMapper()
    def guard = Mock(Guard)
    def account = Mock(Account)
    def userId = "test_user_id"
    def data = objectMapper.createObjectNode()

    RolesAddChatPhase rolesAddChatPhase

    def setup() {
        rolesAddChatPhase = Spy(RolesAddChatPhase, constructorArgs: [
            guard,
            account,
            userId,
            data
        ])
    }

    def "On enter the phase, should start license key select phase"(Role role) {
        given:
        def data = rolesAddChatPhase.data
        data.put(RolesAddChatPhase.ARG_ROLE_ID, role.id)

        when:
        rolesAddChatPhase.onEnter()

        then:
        1 * rolesAddChatPhase.sendTextMessage(_ as String) >> {}
        1 * rolesAddChatPhase.startPhase(ChatStatus.LICENSE_SELECT) >> {}

        where:
        role << Role.values()
    }

    def "On enter the phase, if no role ID in data"() {
        given:
        def data = rolesAddChatPhase.data
        data.put(RolesAddChatPhase.ARG_ROLE_ID, -1)

        when:
        rolesAddChatPhase.onEnter()

        then:
        thrown(IllegalArgumentException)
    }

    def "On enter the phase, if role ID in data is invalid"() {
        when:
        rolesAddChatPhase.onEnter()

        then:
        thrown(NullPointerException)
    }

    def "When receive text message, should throw exception"() {
        when:
        rolesAddChatPhase.onReceiveTextMessage("hello")

        then:
        thrown(IllegalStateException)
    }

    def "On return from license select, should enter group select phase"() {
        given:
        def licenseId = "1111"
        def retData = objectMapper.createObjectNode()
        retData.put(LicenseSelectChatPhase.RET_CANCELED, false)
        retData.put(LicenseSelectChatPhase.RET_LICENSE_ID, licenseId)
        def data = rolesAddChatPhase.data

        when:
        rolesAddChatPhase.onReturn(ChatStatus.LICENSE_SELECT, retData)

        then:
        _ * rolesAddChatPhase.sendTextMessage(_ as String) >> {}
        data.get(RolesAddChatPhase.KEY_LICENSE_ID).asText() == licenseId
        1 * rolesAddChatPhase.startPhase(ChatStatus.GROUP_SELECT) >> {}
    }

    @Unroll
    def "On return from group select, should enter accounts select phase for role #role"(Role role, int maxAccounts) {
        given:
        def groupId = "1111"
        def licenseId = "2222"
        def retData = objectMapper.createObjectNode()
        retData.put(GroupSelectChatPhase.RET_CANCELED, false)
        retData.put(GroupSelectChatPhase.RET_GROUP_ID, groupId)
        def data = rolesAddChatPhase.data
        data.put(RolesAddChatPhase.ARG_ROLE_ID, role.id)
        data.put(RolesAddChatPhase.KEY_LICENSE_ID, licenseId)
        def license = new License(licenseId, "my_license_key_1", userId, Instant.now()).with {
            numDefenders = 1
            maxDefenders = 10
            numSupporters = 2
            maxSupporters = 8
            return it
        }
        def guardGroup = Mock(GuardGroup)
        def existingRoles = [
            new GroupRole("group_role_id_1", groupId, "other_user_1", Role.DEFENDER, "my_license_key_2"),
            new GroupRole("group_role_id_2", groupId, "other_user_2", Role.SUPPORTER, "my_license_key_3"),
            new GroupRole("group_role_id_2", groupId, "my_guard_id_2", Role.ADMIN, "my_license_key_3")
        ]
        def accountSelPhaseData = objectMapper.valueToTree([
            (AccountSelectChatPhase.ARG_ACCOUNT_IDS): [ // "my_guard_id_2" already has role, should be excluded
                                                        "my_guard_id_1",
                                                        "my_guard_id_3"
            ],
            (AccountSelectChatPhase.ARG_MIN_NUM)    : 1,
        ]) as ObjectNode
        if (maxAccounts >= 0) {
            accountSelPhaseData.put(AccountSelectChatPhase.ARG_MAX_NUM, maxAccounts)
        }

        when:
        rolesAddChatPhase.onReturn(ChatStatus.GROUP_SELECT, retData)

        then:
        _ * rolesAddChatPhase.sendTextMessage(_ as String) >> {}
        data.get(RolesAddChatPhase.KEY_GROUP_ID).asText() == groupId
        1 * guard.getLicense(licenseId) >> license
        1 * guard.getGuardIds() >> (["my_guard_id_1", "my_guard_id_2", "my_guard_id_3"] as Set)
        1 * guard.getGroup(groupId) >> guardGroup
        1 * guardGroup.getRoles() >> existingRoles
        1 * rolesAddChatPhase.startPhase(ChatStatus.ACCOUNTS_SELECT, accountSelPhaseData) >> {}

        where:
        role            | maxAccounts
        Role.DEFENDER   | 9   // 10 - 1
        Role.SUPPORTER  | 6   // 8 - 2
        Role.ADMIN      | -1
    }

    def "On return from group select, the license cannot be found"(Role role) {
        given:
        def groupId = "1111"
        def licenseId = "2222"
        def retData = objectMapper.createObjectNode()
        retData.put(GroupSelectChatPhase.RET_CANCELED, false)
        retData.put(GroupSelectChatPhase.RET_GROUP_ID, groupId)
        def data = rolesAddChatPhase.data
        data.put(RolesAddChatPhase.ARG_ROLE_ID, role.id)
        data.put(RolesAddChatPhase.KEY_LICENSE_ID, licenseId)

        when:
        rolesAddChatPhase.onReturn(ChatStatus.GROUP_SELECT, retData)

        then:
        _ * rolesAddChatPhase.sendTextMessage(_ as String) >> {}
        !data.has(RolesAddChatPhase.KEY_LICENSE_ID)
        1 * guard.getLicense(licenseId) >> {throw new IllegalArgumentException()}
        1 * rolesAddChatPhase.startPhase(ChatStatus.LICENSE_SELECT) >> {}

        where:
        role << Role.values()
    }

    def "On return from accounts select, should add roles"(Role role) {
        given:
        def selAccountIds = objectMapper.valueToTree(["my_account_1", "my_account_2"])
        def retData = objectMapper.valueToTree([
            (AccountSelectChatPhase.RET_CANCELED): false,
            (AccountSelectChatPhase.RET_SELECTED_ACCOUNT_IDS): selAccountIds
        ]) as ObjectNode
        def data = rolesAddChatPhase.data
        data.put(RolesAddChatPhase.ARG_ROLE_ID, role.id)
        data.put(RolesAddChatPhase.KEY_GROUP_ID, "test_group_id")
        data.put(RolesAddChatPhase.KEY_LICENSE_ID, "test_license_id")
        def guardGroup = Mock(GuardGroup)

        when:
        rolesAddChatPhase.onReturn(ChatStatus.ACCOUNTS_SELECT, retData)

        then:
        data.get(RolesAddChatPhase.KEY_ACCOUNT_IDS) == selAccountIds
        1 * guard.getGroup("test_group_id") >> guardGroup
        1 * guardGroup.addRole("my_account_1", role, "test_license_id") >> {}
        1 * guardGroup.addRole("my_account_2", role, "test_license_id") >> {}
        1 * rolesAddChatPhase.leavePhase() >> {}

        where:
        role << Role.values()
    }

    def "On return from unknown phase"() {
        given:
        def retData = objectMapper.createObjectNode()

        when:
        rolesAddChatPhase.onReturn(chatStatus, retData)

        then:
        thrown(IllegalStateException)

        where:
        chatStatus << [ChatStatus.ROLES_REMOVE]
    }

    def "On return from a canceled phase"() {
        given:
        def retData = objectMapper.createObjectNode()
        retData.put(canceledKey, true)

        when:
        rolesAddChatPhase.onReturn(retStatus, retData)

        then:
        1 * rolesAddChatPhase.leavePhase()

        where:
        retStatus                   | canceledKey
        ChatStatus.LICENSE_SELECT   | LicenseSelectChatPhase.RET_CANCELED
        ChatStatus.GROUP_SELECT     | GroupSelectChatPhase.RET_CANCELED
        ChatStatus.ACCOUNTS_SELECT  | AccountSelectChatPhase.RET_CANCELED
    }
}
