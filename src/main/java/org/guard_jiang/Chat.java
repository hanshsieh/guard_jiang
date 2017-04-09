package org.guard_jiang;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

/**
 * Created by someone on 4/8/2017.
 */
public class Chat {
    private String hostId;
    private String guestId;
    private ChatStatus status = ChatStatus.NONE;
    private Map<String, String> metadata = Collections.emptyMap();

    public Chat(@Nonnull String hostId, @Nonnull String guestId) {
        this.hostId = hostId;
        this.guestId = guestId;
    }

    @Nonnull
    public String getHostId() {
        return hostId;
    }

    public void setHostId(@Nonnull String hostId) {
        this.hostId = hostId;
    }

    @Nonnull
    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(@Nonnull String guestId) {
        this.guestId = guestId;
    }

    @Nonnull
    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(@Nonnull ChatStatus status) {
        this.status = status;
    }

    @Nonnull
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nonnull Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
