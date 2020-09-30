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

import com.github.ucchyocean.lc.LunaChatVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

import java.util.Optional;

/**
 * ChannelMemberのBungee-ConsoleCommandSender実装
 * @author ucchy
 */
public class ChannelMemberVelocityConsole extends ChannelMemberVelocity {
    private CommandSource source;

    /**
     * コンストラクタ
     * @param source ConsoleのCommandSender
     */
    public ChannelMemberVelocityConsole(CommandSource source) {
        this.source = source;
    }

    /**
     * BungeeのProxiedPlayerを取得する
     * @return 常にnullが返される
     * @see com.github.ucchyocean.lc.member.ChannelMemberVelocity#getPlayer()
     */
    @Override
    public Optional<Player> getPlayer() {
        return Optional.empty();
    }

    /**
     * 発言者が今いるサーバーを取得する
     * @return 常にnullが返される
     * @see com.github.ucchyocean.lc.member.ChannelMemberVelocity#getServer()
     */
    @Override
    public Optional<ServerConnection> getServer() {
        return Optional.empty();
    }

    /**
     * 発言者がオンラインかどうかを取得する
     * @return 常にtrueが返される
     * @see com.github.ucchyocean.lc.member.ChannelMember#isOnline()
     */
    @Override
    public boolean isOnline() {
        return true;
    }

    /**
     * 発言者名を取得する
     * @return 発言者名
     * @see com.github.ucchyocean.lc.member.ChannelMember#getName()
     */
    @Override
    public String getName() {
        //return source instanceof Player ? ((Player)source).getUsername() : "";
        return "console";
    }

    /**
     * 発言者の表示名を取得する
     * @return 発言者の表示名
     * @see com.github.ucchyocean.lc.member.ChannelMember#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        //return source instanceof Player ? ((Player)source).getUsername() : "";
        return "console";
    }

    /**
     * プレフィックスを返す
     * @return 常に空文字列
     * @see com.github.ucchyocean.lc.member.ChannelMember#getPrefix()
     */
    @Override
    public String getPrefix() {
        return null;
    }

    /**
     * サフィックスを返す
     * @return 常に空文字列
     * @see com.github.ucchyocean.lc.member.ChannelMember#getSuffix()
     */
    @Override
    public String getSuffix() {
        return null;
    }

    /**
     * 発言者にメッセージを送信する
     * @param message メッセージ
     * @see com.github.ucchyocean.lc.member.ChannelMember#sendMessage(String)
     */
    @Override
    public void sendMessage(String message) {
        if ( message == null || message.isEmpty() ) return;
        source.sendMessage(TextComponent.of(message));
    }

    /**
     * メッセージを送る
     * @param message 送るメッセージ
     * @see com.github.ucchyocean.lc.member.ChannelMember#sendMessage(Component)
     */
    @Override
    public void sendMessage(Component message) {
        if ( message == null ) return;
        source.sendMessage(message);
    }

    /**
     * 指定されたパーミッションノードの権限を持っているかどうかを取得する
     * @param node パーミッションノード
     * @return 権限を持っているかどうか
     * @see com.github.ucchyocean.lc.member.ChannelMember#hasPermission(String)
     */
    @Override
    public boolean hasPermission(String node) {
        return source.hasPermission(node);
    }

    /**
     * 指定されたパーミッションノードが定義されているかどうかを取得する
     * @param node パーミッションノード
     * @return 定義を持っているかどうか
     * @see com.github.ucchyocean.lc.member.ChannelMember#isPermissionSet(String)
     */
    @Override
    public boolean isPermissionSet(String node) {
        return source.getPermissionValue(node).asBoolean();
    }

    /**
     * 指定されたメッセージの内容を発言する
     * @param message メッセージ
     * @see com.github.ucchyocean.lc.member.ChannelMember#chat(String)
     */
    public void chat(String message) {
        LunaChatVelocity.PROXY.broadcast(TextComponent.of("<" + getName() + ">" + message));
    }

    /**
     * IDを返す
     * @return 名前をそのまま返す
     * @see com.github.ucchyocean.lc.member.ChannelMember#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

}
