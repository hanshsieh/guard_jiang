package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.chat.Chat;
import org.guard_jiang.chat.ChatEnv;

import javax.annotation.Nonnull;

/**
 * Created by icand on 2017/8/26.
 */
public interface ChatMapper {

    @Nonnull
    Chat getChat(
            @Param("guardId") @Nonnull String guardId,
            @Param("userId") @Nonnull String userId,
            @Param("chatEnv") @Nonnull ChatEnv chatEnv
    );

    void setChat(
            @Param("chat") @Nonnull Chat chat
    );
}
