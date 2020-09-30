package com.github.ucchyocean.lc.member;

import net.kyori.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.ucchyocean.lc.util.BlockLocation;
/**
 * 任意の内容を設定できるChannelMember
 * @author ucchy
 */
public class ChannelMemberOther extends ChannelMember {

    private String id;
    private String name;
    private String displayName;
    private String prefix;
    private String suffix;
    private BlockLocation location;
    private String serverName;
    private String worldName;

    public ChannelMemberOther(@NotNull String name) {
        this(name, name);
    }

    public ChannelMemberOther(@NotNull String name, @NotNull String displayName) {
        this(name, displayName, "", "");
    }

    public ChannelMemberOther(@NotNull String name, @NotNull String displayName,
            @NotNull String prefix, @NotNull String suffix) {
        this(name, displayName, prefix, suffix, null);
    }

    public ChannelMemberOther(@NotNull String name, @NotNull String displayName,
            @NotNull String prefix, @NotNull String suffix,
            @Nullable BlockLocation location) {
        this(name, displayName, prefix, suffix, location, null);
    }

    public ChannelMemberOther(@NotNull String name, @NotNull String displayName,
            @NotNull String prefix, @NotNull String suffix,
            @Nullable BlockLocation location, @Nullable String id) {
        this.name = name;
        this.displayName = displayName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.location = location;
        this.id = id;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void sendMessage(String message) {
        // do nothing.
    }

    @Override
    public void sendMessage(Component message) {
        // do nothing.
    }

    @Override
    public String getWorldName() {
        if ( worldName != null ) return worldName;
        if ( location != null ) return location.getWorldName();
        return "";
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public @Nullable BlockLocation getLocation() {
        return location;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName serverName
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public boolean hasPermission(String node) {
        return true;
    }

    @Override
    public String toString() {
        if ( id != null ) return "$" + id;
        return name;
    }

    @Override
    public boolean isPermissionSet(String node) {
        return true;
    }

    @Override
    public void chat(String message) {
        // do nothing.
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }
}
