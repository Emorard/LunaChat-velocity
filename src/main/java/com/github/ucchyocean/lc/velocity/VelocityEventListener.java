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
package com.github.ucchyocean.lc.velocity;

import com.github.ucchyocean.lc.*;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.event.EventResult;
import com.github.ucchyocean.lc.japanize.Japanizer;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.github.ucchyocean.lc.member.ChannelMemberOther;
import com.github.ucchyocean.lc.messaging.BukkitChatMessage;
import com.github.ucchyocean.lc.util.ChatColor;
import com.github.ucchyocean.lc.util.ClickableFormat;
import com.github.ucchyocean.lc.util.Utility;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VelocityEventListener {
    private static final int MAX_LIST_ITEMS = 8;

    private LunaChatVelocity parent;
    private LunaChatConfig config;
    private LunaChatAPI api;

    public VelocityEventListener(LunaChatVelocity parent) {
        this.parent = parent;
        config = parent.getConfig();
        api = parent.getLunaChatAPI();
    }

    @Subscribe
    public void onAsyncPlayerChat(PlayerChatEvent event) {
        if( !event.getResult().isAllowed() ) return;
        processChatEvent(event);
    }

    @Subscribe
    public void onJoin(PostLoginEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        Player player = event.getPlayer();

        // UUIDをキャッシュ
        LunaChat.getUUIDCacheData().put(player.getUniqueId().toString(), player.getUsername());
        LunaChat.getUUIDCacheData().save();

        // 強制参加チャンネル設定を確認し、参加させる
        forceJoinToForceJoinChannels(player);

        // グローバルチャンネル設定がある場合
        if ( !config.getGlobalChannel().equals("") ) {
            tryJoinToGlobalChannel(player);
        }

        // チャンネルチャット情報を表示する
        if ( config.isShowListOnJoin() ) {
            for ( Component msg : getListForMotd(player) ) {
                player.sendMessage(msg);
            }
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {

        Player player = event.getPlayer();
        String pname = player.getUsername();

        // お互いがオフラインになるPMチャンネルがある場合は
        // チャンネルをクリアする
        ArrayList<Channel> deleteList = new ArrayList<>();

        for ( Channel channel : LunaChat.getAPI().getChannels() ) {
            String cname = channel.getName();
            if ( channel.isPersonalChat() && cname.contains(pname) ) {
                boolean isAllOffline = true;
                for ( ChannelMember cp : channel.getMembers() ) {
                    if ( cp.isOnline() ) {
                        isAllOffline = false;
                    }
                }
                if ( isAllOffline ) {
                    deleteList.add(channel);
                }
            }
        }

        for ( Channel channel : deleteList ) {
            LunaChat.getAPI().removeChannel(
                    channel.getName(), ChannelMember.getChannelMember(player));
        }
    }

    @Subscribe
    public void onPluginMessageReceived(PluginMessageEvent event) {

        // 自分のチャンネルメッセージでない場合は無視する
        if( !event.getIdentifier().equals(parent.getLegacyChannelIdentifier()) ) {
            return;
        }

        // データをメッセージに復元する
        BukkitChatMessage msg = BukkitChatMessage.fromByteArray(event.getData());
        if ( msg == null ) return;

        // 受信者と発言者が一致しない場合は無視する
        if ( event.getSource() instanceof Player ) {
            Player receiver = (Player)event.getSource();
            if ( !receiver.getUsername().equals(msg.getMember().getName()) ) {
                return;
            }
        } else {
            return;
        }

        // 発言者を取得する　サーバー名を設定できる場合は設定する
        ChannelMemberOther member = msg.getMember();
        LunaChatVelocity.PROXY.getPlayer(member.getName()).flatMap(Player::getCurrentServer).ifPresent(server ->
                member.setServerName(server.getServerInfo().getName()));
        // 発言処理する
        processChat(member, msg.getMessage());
    }

    private void processChatEvent(PlayerChatEvent event) {

        // Bungeeパススルーモードなら何もしない
        if ( config.isBungeePassThroughMode() ) {
            return;
        }

        if(event.getMessage() == null) {
            System.out.println("null!");
        }

        // 発言内容を非同期で処理する
        LunaChatVelocity.getInstance().runAsyncTask(() ->
                processChat(ChannelMember.getChannelMember(event.getPlayer()), event.getMessage()));

        // イベントをキャンセル
        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

    private void processChat(ChannelMember member, String message) {
        // 頭にglobalMarkerが付いている場合は、グローバル発言にする
        if ( config.getGlobalMarker() != null &&
                !config.getGlobalMarker().equals("") &&
                message.startsWith(config.getGlobalMarker()) &&
                message.length() > config.getGlobalMarker().length() ) {

            int offset = config.getGlobalMarker().length();
            message = message.substring(offset);
            chatGlobal(member, message);
            return;
        }

        // クイックチャンネルチャット機能が有効で、専用の記号が含まれるなら、
        // クイックチャンネルチャットとして処理する。
        if ( config.isEnableQuickChannelChat() ) {
            String separator = config.getQuickChannelChatSeparator();
            if ( message.contains(separator) ) {
                String[] temp = message.split(separator, 2);
                String name = temp[0];
                String value = "";
                if ( temp.length > 0 ) {
                    value = temp[1];
                }

                Channel channel = api.getChannel(name);
                if ( channel != null ) {

                    if ( !channel.getMembers().contains(member) ) {
                        // 指定されたチャンネルに参加していないなら、エラーを表示して何も発言せずに終了する。
                        member.sendMessage(
                                TextComponent.builder(Messages.errmsgNomember()).build());
                        return;
                    }

                    // 指定されたチャンネルに発言して終了する。
                    chatToChannelWithEvent(member, channel, value);
                    return;
                }
            }
        }

        Channel channel = api.getDefaultChannel(member.getName());

        // デフォルトの発言先が無い場合
        if ( channel == null ) {
            if ( config.isNoJoinAsGlobal() ) {
                // グローバル発言にする
                chatGlobal(member, message);
                return;

            } else {
                // 何もせずに終了する
                return;
            }
        }

        chatToChannelWithEvent(member, channel, message);
    }

    private void chatGlobal(ChannelMember member, String message) {

        LunaChatConfig config = LunaChat.getConfig();

        if ( !config.getGlobalChannel().equals("") ) {
            // グローバルチャンネル設定がある場合

            // グローバルチャンネルの取得、無ければ作成
            Channel global = api.getChannel(config.getGlobalChannel());
            if ( global == null ) {
                global = api.createChannel(config.getGlobalChannel(), member);
            }

            // デフォルト発言先が無いなら、グローバルチャンネルに設定する
            Channel dchannel = api.getDefaultChannel(member.getName());
            if ( dchannel == null ) {
                api.setDefaultChannel(member.getName(), global.getName());
            }

            // チャンネルチャット発言
            chatToChannelWithEvent(member, global, message);

            return;

        } else {
            // グローバルチャンネル設定が無い場合

            // NGワードのマスク
            message = maskNGWord(message, config.getNgwordCompiled());

            // Japanizeをスキップするかどうかフラグ
            boolean skipJapanize = !LunaChat.getAPI().isPlayerJapanize(member.getName());

            // 一時的なJapanizeスキップが指定されているか確認する
            String marker = config.getNoneJapanizeMarker();
            if ( !marker.equals("") && message.startsWith(marker) ) {
                message = message.substring(marker.length());
                skipJapanize = true;
            }

            // 2byteコードを含む、または、半角カタカナのみなら、Japanize変換は行わない
            String kanaTemp = Utility.stripColorCode(message);

            if ( !skipJapanize &&
                    ( kanaTemp.getBytes(StandardCharsets.UTF_8).length > kanaTemp.length() ||
                            kanaTemp.matches("[ \\uFF61-\\uFF9F]+") ) ) {
                skipJapanize = true;
            }

            // Japanizeの付加
            if ( !skipJapanize ) {

                String japanize = Japanizer.japanize(Utility.stripColorCode(message), config.getJapanizeType(),
                        LunaChat.getAPI().getAllDictionary());
                if ( japanize.length() > 0 ) {

                    // NGワードのマスク
                    japanize = maskNGWord(japanize, config.getNgwordCompiled());

                    // フォーマット化してメッセージを上書きする
                    String japanizeFormat = config.getJapanizeDisplayLine() == 1 ?
                            config.getJapanizeLine1Format() :
                            "%msg\n" + config.getJapanizeLine2Format();
                    String preMessage = new String(message);
                    message = japanizeFormat.replace("%msg", preMessage).replace("%japanize", japanize);
                }
            }

            String result;

            String f = config.getNormalChatMessageFormat();
            ClickableFormat format = ClickableFormat.makeFormat(f, member);
            format.replace("%msg", message);

            // hideされているプレイヤーを除くすべてのプレイヤーに、
            // 発言内容を送信する。
            Component msg = format.makeTextComponent();
            List<ChannelMember> hidelist = api.getHidelist(member);

            for ( RegisteredServer info : parent.getProxy().getAllServers()) {
                for ( Player player : info.getPlayersConnected() ) {
                    if ( !containsHideList(player, hidelist) ) {
                        sendMessage(player, msg);
                    }
                }
            }

            result = format.toLegacyText();

            // コンソールに表示設定なら、コンソールに表示する
            if ( config.isDisplayNormalChatOnConsole() ) {
                parent.getLogger().info(result);
            }

            // ログに記録する
            LunaChat.getNormalChatLogger().log(Utility.stripColorCode(result), member.getName());
        }
    }

    /**
     * チャンネルに発言処理を行う
     * @param player プレイヤー
     * @param channel チャンネル
     * @param message 発言内容
     * @return イベントでキャンセルされたかどうか
     */
    private boolean chatToChannelWithEvent(ChannelMember player, Channel channel, String message) {

        // LunaChatPreChatEvent イベントコール
        EventResult result = LunaChat.getEventSender().sendLunaChatPreChatEvent(

                channel.getName(), player, message);
        if ( result.isCancelled() ) {
            return true;
        }
        Channel alt = result.getChannel();
        if ( alt != null ) {
            channel = alt;
        }
        message = result.getMessage();

        // チャンネルチャット発言
        channel.chat(player, message);

        return false;
    }

    /**
     * NGワードをマスクする
     * @param message メッセージ
     * @param ngwords NGワード
     * @return マスクされたメッセージ
     */
    private String maskNGWord(String message, List<Pattern> ngwords) {
        for ( Pattern pattern : ngwords ) {
            Matcher matcher = pattern.matcher(message);
            if ( matcher.find() ) {
                message = matcher.replaceAll(
                        Utility.getAstariskString(matcher.group(0).length()));
            }
        }
        return message;
    }

    /**
     * 強制参加チャンネルへ参加させる
     * @param player プレイヤー
     */
    private void forceJoinToForceJoinChannels(Player player) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        List<String> forceJoinChannels = config.getForceJoinChannels();

        for ( String cname : forceJoinChannels ) {

            // チャンネルが存在しない場合は作成する
            Channel channel = api.getChannel(cname);
            if ( channel == null ) {
                channel = api.createChannel(cname, ChannelMember.getChannelMember(player));
            }

            // チャンネルのメンバーでないなら、参加する
            ChannelMember cp = ChannelMember.getChannelMember(player);
            if ( !channel.getMembers().contains(cp) ) {
                channel.addMember(cp);
            }

            // デフォルト発言先が無いなら、グローバルチャンネルに設定する
            Channel dchannel = api.getDefaultChannel(player.getUsername());
            if ( dchannel == null ) {
                api.setDefaultChannel(player.getUsername(), cname);
            }
        }
    }

    /**
     * 既定のチャンネルへの参加を試みる。
     * @param player プレイヤー
     * @return 参加できたかどうか
     */
    private boolean tryJoinToGlobalChannel(Player player) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        String gcName = config.getGlobalChannel();

        // チャンネルが存在しない場合は作成する
        Channel global = api.getChannel(gcName);
        if ( global == null ) {
            global = api.createChannel(gcName, ChannelMember.getChannelMember(player));
        }

        // デフォルト発言先が無いなら、グローバルチャンネルに設定する
        Channel dchannel = api.getDefaultChannel(player.getUsername());
        if ( dchannel == null ) {
            api.setDefaultChannel(player.getUsername(), gcName);
        }

        return true;
    }

    /**
     * プレイヤーのサーバー参加時用の参加チャンネルリストを返す
     * @param player プレイヤー
     * @return リスト
     */
    private ArrayList<Component> getListForMotd(Player player) {

        ChannelMember cp = ChannelMember.getChannelMember(player);
        LunaChatAPI api = LunaChat.getAPI();
        Channel dc = api.getDefaultChannel(cp.getName());
        String dchannel = "";
        if ( dc != null ) {
            dchannel = dc.getName().toLowerCase();
        }

        // チャンネル一覧を取得して、参加人数でソートする
        ArrayList<Channel> channels = new ArrayList<>(api.getChannels());
        channels.sort((c1, c2) -> {
            if (c1.getOnlineNum() == c2.getOnlineNum()) return c1.getName().compareTo(c2.getName());
            return c2.getOnlineNum() - c1.getOnlineNum();
        });

        int count = 0;
        ArrayList<Component> items = new ArrayList<>();
        items.add(TextComponent.builder(Messages.motdFirstLine()).build());
        for ( Channel channel : channels ) {

            // BANされているチャンネルは表示しない
            if ( channel.getBanned().contains(cp) ) {
                continue;
            }

            // 個人チャットはリストに表示しない
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // 参加していないチャンネルは、グローバルチャンネルを除き表示しない
            if ( !channel.getMembers().contains(cp) &&
                    !channel.isGlobalChannel() ) {
                continue;
            }

            String disp = ChatColor.WHITE + channel.getName();
            if ( channel.getName().equals(dchannel) ) {
                disp = ChatColor.RED + channel.getName();
            }
            String desc = channel.getDescription();
            int onlineNum = channel.getOnlineNum();
            int memberNum = channel.getTotalNum();
            items.add(Messages.listFormat(disp, onlineNum, memberNum, desc));
            count++;

            if ( count > MAX_LIST_ITEMS ) {
                break;
            }
        }
        items.add(TextComponent.builder(Messages.listEndLine()).build());

        return items;
    }

    /**
     * 指定した対象にメッセージを送信する
     * @param reciever 送信先
     * @param message メッセージ
     */
    private void sendMessage(CommandSource reciever, Component message) {
        if ( message == null ) return;
        ChannelMember cm = ChannelMember.getChannelMember(reciever);
        if ( cm != null ) {
            cm.sendMessage(message);
        }
    }

    /**
     * 指定されたEventPriorityが、LunaChatConfigで指定されているEventPriorityと一致するかどうかを調べる
     * @param priority
     * @return 一致するかどうか
     */
    /*
    private boolean matchesEventPriority(int priority) {
        String c = LunaChat.getConfig().getPlayerChatEventListenerPriority().name();
        if ( priority == EventPriority.LOWEST ) return "LOWEST".equals(c);
        if ( priority == EventPriority.LOW ) return "LOW".equals(c);
        if ( priority == EventPriority.NORMAL ) return "NORMAL".equals(c);
        if ( priority == EventPriority.HIGH ) return "HIGH".equals(c);
        if ( priority == EventPriority.HIGHEST ) return "HIGHEST".equals(c);
        return false;
    }
     */

    /**
     * 指定されたプレイヤーが指定されたHideListに含まれるかどうかを判定する
     * @param p プレイヤー
     * @param list リスト
     * @return 含まれるかどうか
     */
    private boolean containsHideList(Player p, List<ChannelMember> list) {
        for ( ChannelMember m : list ) {
            if (m.getName().equals(p.getUsername())) return true;
        }
        return false;
    }

}
