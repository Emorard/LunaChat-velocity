/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.member;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatVelocity;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

import java.util.Optional;
import java.util.UUID;

/**
 * ChannelMemberのPlayer実装
 * @author ucchy
 */
public class ChannelMemberPlayer extends ChannelMemberVelocity {

    private UUID id;

    /**
     * コンストラクタ
     * @param id プレイヤーID
     */
    public ChannelMemberPlayer(String id) {
        this.id = UUID.fromString(id);
    }

    /**
     * コンストラクタ
     * @param id UUID
     */
    public ChannelMemberPlayer(UUID id) {
        this.id = id;
    }

    /**
     * プレイヤー名からUUIDを取得してChannelMemberPlayerを作成して返す
     * @param nameOrUuid 名前、または、"$" + UUID
     * @return ChannelMemberPlayer
     */
    public static ChannelMemberPlayer getChannelMember(String nameOrUuid) {
        if ( nameOrUuid.startsWith("$") ) {
            return new ChannelMemberPlayer(UUID.fromString(nameOrUuid.substring(1)));
        } else {
            Optional<Player> player = LunaChatVelocity.PROXY.getPlayer(nameOrUuid);
            if ( player.isPresent() ) return new ChannelMemberPlayer(player.get().getUniqueId());
        }
        return null;
    }

    /**
     * オンラインかどうか
     * @return オンラインかどうか
     */
    @Override
    public boolean isOnline() {
        return (LunaChatVelocity.PROXY.getPlayer(id).isPresent());
    }

    /**
     * プレイヤー名を返す
     * @return プレイヤー名
     */
    @Override
    public String getName() {
        String cache = LunaChat.getUUIDCacheData().get(id.toString());
        if ( cache != null ) {
            return cache;
        }
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return player.get().getUsername();
        }
        return id.toString();
    }

    /**
     * プレイヤー表示名を返す
     * @return プレイヤー表示名
     */
    @Override
    public String getDisplayName() {
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return player.get().getUsername();
        }
        return getName();
    }

    /**
     * プレフィックスを返す
     * @return プレフィックス
     */
    @Override
    public String getPrefix() {
        /*
        LuckPermsBridge luckperms = LunaChatBungee.getInstance().getLuckPerms();
        if ( luckperms != null ) {
            return luckperms.getPlayerPrefix(id);
        }
        BungeePermsBridge bungeeperms = LunaChatBungee.getInstance().getBungeePerms();
        if ( bungeeperms != null ) {
            return bungeeperms.userPrefix(id.toString(), null, null);
        }
         */
        return "";
    }

    /**
     * サフィックスを返す
     * @return サフィックス
     */
    @Override
    public String getSuffix() {
        /*
        LuckPermsBridge luckperms = LunaChatBungee.getInstance().getLuckPerms();
        if ( luckperms != null ) {
            return luckperms.getPlayerSuffix(id);
        }
        BungeePermsBridge bungeeperms = LunaChatBungee.getInstance().getBungeePerms();
        if ( bungeeperms != null ) {
            return bungeeperms.userSuffix(id.toString(), null, null);
        }
         */
        return "";
    }

    /**
     * メッセージを送る
     * @param message 送るメッセージ
     */
    @Override
    public void sendMessage(String message) {
        if ( message == null || message.isEmpty() ) return;
        Optional<Player> player = getPlayer();
        player.ifPresent(value -> value.sendMessage(TextComponent.of(message)));
    }

    /**
     * メッセージを送る
     * @param message 送るメッセージ
     * @see com.github.ucchyocean.lc.member.ChannelMember#sendMessage(Component)
     */
    public void sendMessage(Component message) {
        if ( message == null ) return;
        Optional<Player> player = getPlayer();
        player.ifPresent(value -> value.sendMessage(message));
    }

    /**
     * 指定されたパーミッションノードの権限を持っているかどうかを取得する
     * @param node パーミッションノード
     * @return 権限を持っているかどうか
     */
    @Override
    public boolean hasPermission(String node) {
        Optional<Player> player = getPlayer();
        return player.map(value -> value.hasPermission(node)).orElse(false);
    }

    /**
     * 指定されたパーミッションノードが定義されているかどうかを取得する
     * @param node パーミッションノード
     * @return 定義を持っているかどうか
     * @see com.github.ucchyocean.lc.member.ChannelMember#isPermissionSet(String)
     */
    @Override
    public boolean isPermissionSet(String node) {
        Optional<Player> player = getPlayer();
        return player.map(value -> value.getPermissionValue(node).asBoolean()).orElse(false);
    }

    /**
     * 指定されたメッセージの内容を発言する
     * @param message メッセージ
     * @see com.github.ucchyocean.lc.member.ChannelMember#chat(String)
     */
    public void chat(String message) {
        Optional<Player> player = getPlayer();
        player.ifPresent(value -> value.spoofChatInput(message));
    }

    /**
     * IDを返す
     * @return "$" + UUID を返す
     */
    @Override
    public String toString() {
        return "$" + id.toString();
    }

    /**
     * Playerを取得して返す
     * @return Player
     */
    @Override
    public Optional<Player> getPlayer() {
        return LunaChatVelocity.PROXY.getPlayer(id);
    }

    /**
     * 発言者が今いるサーバーを取得する
     * @return サーバー
     */
    @Override
    public Optional<ServerConnection> getServer() {
        Optional<Player> player = getPlayer();
        return player.map(Player::getCurrentServer).orElse(null);
    }
}
