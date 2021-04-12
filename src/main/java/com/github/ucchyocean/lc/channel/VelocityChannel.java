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
package com.github.ucchyocean.lc.channel;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import com.github.ucchyocean.lc.LunaChatConfig;
import com.github.ucchyocean.lc.LunaChatVelocity;
import com.github.ucchyocean.lc.event.EventResult;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.github.ucchyocean.lc.util.ClickableFormat;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VelocityChannel extends Channel {

    /**
     * コンストラクタ
     * @param name チャンネル名
     */
    protected VelocityChannel(String name) {
        super(name);
    }

    /**
     * メッセージを表示します。指定したプレイヤーの発言として処理されます。
     * @param player プレイヤー（ワールドチャット、範囲チャットの場合は必須です）
     * @param message メッセージ
     * @param format フォーマット
     * @param sendDynmap dynmapへ送信するかどうか
     */
    @Override
    protected void sendMessage(ChannelMember player, String message,
                               @Nullable ClickableFormat format, boolean sendDynmap) {

        LunaChatConfig config = LunaChat.getConfig();

        String originalMessage = new String(message);

        // 受信者を設定する
        List<ChannelMember> recipients = new ArrayList<>();

        if ( isBroadcastChannel() ) {
            // ブロードキャストチャンネル

            // NOTE: BungeeChannelは範囲チャットやワールドチャットをサポートしない

            // 通常ブロードキャスト（全員へ送信）
            for ( Player p : LunaChatVelocity.PROXY.getAllPlayers() ) {
                ChannelMember cp = ChannelMember.getChannelMember(p);
                if ( !getHided().contains(cp) ) {
                    recipients.add(cp);
                }
            }

        } else {
            // 通常チャンネル

            for ( ChannelMember mem : getMembers() ) {
                if ( mem != null && mem.isOnline() && !getHided().contains(mem) ) {
                    recipients.add(mem);
                }
            }
        }

        // opListenAllChannel 設定がある場合は、
        // パーミッション lunachat-admin.listen-all-channels を持つプレイヤーを
        // 受信者に加える。
        if ( config.isOpListenAllChannel() ) {
            for ( Player p : LunaChatVelocity.PROXY.getAllPlayers() ) {
                ChannelMember cp = ChannelMember.getChannelMember(p);
                if ( cp.hasPermission("lunachat-admin.listen-all-channels")
                        && !recipients.contains(cp) ) {
                    recipients.add(cp);
                }
            }
        }

        // hideされている場合は、受信対象者から抜く。
        LunaChatAPI api = LunaChat.getAPI();
        for ( ChannelMember cp : api.getHidelist(player) )  {
            if ( recipients.contains(cp) ) {
                recipients.remove(cp);
            }
        }

        // フォーマットがある場合は置き換える

        // LunaChatChannelMessageEvent イベントコール
        String name = (player != null) ? player.getDisplayName() : "<null>";
        EventResult result = LunaChat.getEventSender().sendLunaChatChannelMessageEvent(
                getName(), player, message, recipients, name, originalMessage);
        message = result.getMessage();
        recipients = result.getRecipients();

        // 送信する
        if ( format != null ) {
            LunaChatVelocity.getInstance().getLogger().info("message :" + message);
            format.replace("%msg", message);
            Component comps = format.makeTextComponent();
            for ( ChannelMember p : recipients ) {
                p.sendMessage(comps);
            }
            message = format.toLegacyText();
        } else {
            for ( ChannelMember p : recipients ) {
                p.sendMessage(message);
            }
        }

        // 設定に応じて、コンソールに出力する
        if ( config.isDisplayChatOnConsole() ) {
            LunaChatVelocity.getInstance().getLogger().info(message);
        }

        // ロギング
        log(originalMessage, name);
    }

    /**
     * チャンネルのオンライン人数を返す
     * @return オンライン人数
     * @see com.github.ucchyocean.lc.channel.Channel#getOnlineNum()
     */
    @Override
    public int getOnlineNum() {

        // ブロードキャストチャンネルならサーバー接続人数を返す
        if ( isBroadcastChannel() ) {
            return LunaChatVelocity.PROXY.getPlayerCount();
        }

        return super.getOnlineNum();
    }

    /**
     * チャンネルの総参加人数を返す
     * @return 総参加人数
     * @see com.github.ucchyocean.lc.channel.Channel#getTotalNum()
     */
    @Override
    public int getTotalNum() {

        // ブロードキャストチャンネルならサーバー接続人数を返す
        if ( isBroadcastChannel() ) {
            return LunaChatVelocity.PROXY.getPlayerCount();
        }

        return super.getTotalNum();
    }

    /**
     * チャンネルのメンバーを返す
     * @return チャンネルのメンバー
     * @see com.github.ucchyocean.lc.channel.Channel#getMembers()
     */
    @Override
    public List<ChannelMember> getMembers() {

        // ブロードキャストチャンネルなら、
        // 現在サーバーに接続している全プレイヤーをメンバーとして返す
        if ( isBroadcastChannel() ) {
            List<ChannelMember> mem = new ArrayList<>();
            for ( Player p : LunaChatVelocity.PROXY.getAllPlayers() ) {
                mem.add(ChannelMember.getChannelMember(p));
            }
            return mem;
        }

        return super.getMembers();
    }

    /**
     * ログを記録する
     * @param name 発言者
     * @param message 記録するメッセージ
     */
    @Override
    protected void log(String message, String name) {

        // LunaChatのチャットログへ記録
        LunaChatConfig config = LunaChat.getConfig();
        if ( config.isLoggingChat() && logger != null ) {
            logger.log(message, name);
        }

        // TODO ログ記録プラグイン連携を検討する
    }

}
