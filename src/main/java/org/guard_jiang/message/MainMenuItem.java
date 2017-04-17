package org.guard_jiang.message;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/15/2017.
 */
public enum MainMenuItem {
    CREATE_LICENSE("建立新的金鑰"),
    PROTECT_GROUP("保護我的群組"),
    DO_NOTHING("目前沒有需要幫忙的");
    private final String text;
    MainMenuItem(@Nonnull String text) {
        this.text = text;
    }

    @Nonnull
    public String getText() {
        return text;
    }
}
