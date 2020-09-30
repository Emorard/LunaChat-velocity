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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import java.util.Optional;

public abstract class ChannelMemberVelocity extends ChannelMember {
    /**
     * BungeeのProxiedPlayerを取得する
     * @return ProxiedPlayer
     */
    public abstract Optional<Player> getPlayer();

    /**
     * 発言者が今いるサーバーを取得する
     * @return サーバー
     */
    public abstract Optional<ServerConnection> getServer();

    /**
     * 発言者が今いるサーバーのサーバー名を取得する
     * @return サーバー名
     */
    public String getServerName() {
        Optional<ServerConnection> server = getServer();
        if ( server.isPresent() ) {
            return server.get().getServerInfo().getName();
        }
        return "";
    }

    /**
     * 発言者が今いるワールド名を返す
     * @return 常に空文字列が返される
     * @see com.github.ucchyocean.lc.member.ChannelMember#getWorldName()
     */
    @Override
    public String getWorldName() {
        return "";
    }

    /**
     * CommandSenderから、ChannelMemberを作成して返す
     * @param sender
     * @return ChannelMember
     */
    public static ChannelMemberVelocity getChannelMemberVelocity(Object sender) {
        if (!(sender instanceof CommandSource)) return null;
        if ( sender instanceof Player ) {
            return new ChannelMemberPlayer(((Player)sender).getUniqueId());
        } else {
            // ProxiedPlayer以外のCommandSenderは、ConsoleSenderしかないはず
            return new ChannelMemberVelocityConsole((CommandSource) sender);
        }

    }
}
