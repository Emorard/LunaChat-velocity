/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.github.ucchyocean.lc.util.ChatColor;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

/**
 * listコマンドの実行クラス
 * @author ucchy
 */
public class ListCommand extends LunaChatSubCommand {

    private static final String COMMAND_NAME = "list";
    private static final String PERMISSION_NODE = "lunachat." + COMMAND_NAME;

    private static final int PAGE_SIZE = 8;

    /**
     * コマンドを取得します。
     * @return コマンド
     * @see com.github.ucchyocean.lc.command.LunaChatSubCommand#getCommandName()
     */
    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    /**
     * パーミッションノードを取得します。
     * @return パーミッションノード
     * @see com.github.ucchyocean.lc.command.LunaChatSubCommand#getPermissionNode()
     */
    @Override
    public String getPermissionNode() {
        return PERMISSION_NODE;
    }

    /**
     * コマンドの種別を取得します。
     * @return コマンド種別
     * @see com.github.ucchyocean.lc.command.LunaChatSubCommand#getCommandType()
     */
    @Override
    public CommandType getCommandType() {
        return CommandType.USER;
    }

    /**
     * 使用方法に関するメッセージをsenderに送信します。
     * @param sender コマンド実行者
     * @param label 実行ラベル
     * @see com.github.ucchyocean.lc.command.LunaChatSubCommand#sendUsageMessage()
     */
    @Override
    public void sendUsageMessage(
            ChannelMember sender, String label) {
        sender.sendMessage(Messages.usageList(label));
    }

    /**
     * コマンドを実行します。
     * @param sender コマンド実行者
     * @param label 実行ラベル
     * @param args 実行時の引数
     * @return コマンドが実行されたかどうか
     * @see com.github.ucchyocean.lc.command.LunaChatSubCommand#runCommand(String[])
     */
    @Override
    public boolean runCommand(
            ChannelMember sender, String label, String[] args) {

        int page = 1;
        if ( args.length >= 2 && args[1].matches("[0-9]+") ) {
            page = Integer.parseInt(args[1]);
        }

        // リストを取得して表示する
        for (Component msg : getList(sender, page)) {
            sender.sendMessage(msg);
        }
        return true;
    }

    /**
     * リスト表示用のリストを返す
     * @param player プレイヤー、指定しない場合はnullにする
     * @param page 表示するページ、0を指定した場合は全表示
     * @return リスト
     */
    private ArrayList<Component> getList(ChannelMember player, int page) {

        ArrayList<Component> list = getPlayerList(player);
        int size = list.size();
        int maxPage = (int)(size / PAGE_SIZE) + 1;

        if ( page < 0 ) page = 0;
        if ( page > maxPage ) page = maxPage;

        ArrayList<Component> items = new ArrayList<>();
        if ( page == 0 ) { // 全表示
            items.add(TextComponent.builder(Messages.listFirstLine()).build());
            items.addAll(list);
            items.add(TextComponent.builder(Messages.listEndLine()).build());
        } else { // ページ表示
            items.add(TextComponent.builder(Messages.listFirstLinePaging(page, maxPage)).build());
            int endIndex = (page * PAGE_SIZE > size) ? size : page * PAGE_SIZE;
            items.addAll(list.subList((page - 1) * PAGE_SIZE, endIndex));
            items.add(TextComponent.builder(Messages.listEndLine()).build());
        }

        return items;
    }

    /**
     * 指定されたプレイヤーに対するチャンネルリストを返す
     * @param player プレイヤー
     * @return チャンネルリスト
     */
    private ArrayList<Component> getPlayerList(ChannelMember player) {

        String dchannel = "";
        String playerName = "";
        if ( player != null ) {
            playerName = player.getName();
            Channel def = api.getDefaultChannel(playerName);
            if ( def == null ) {
                dchannel = "";
            } else {
                dchannel = def.getName();
            }
        }

        // チャンネルを取得して、参加人数でソートする
        ArrayList<Channel> channels = new ArrayList<>(api.getChannels());
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                if ( c1.getOnlineNum() == c2.getOnlineNum() ) return c1.getName().compareTo(c2.getName());
                return c2.getOnlineNum() - c1.getOnlineNum();
            }
        });

        // 指定されたプレイヤー名に合うように、フィルタ＆表示用整形する。
        ArrayList<Component> items = new ArrayList<>();
        for ( Channel channel : channels ) {

            // BANされているチャンネルは表示しない
            if ( channel.getBanned().contains(player) ) {
                continue;
            }

            // 個人チャットはリストに表示しない
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // デフォルト発言先なら赤に、非表示中なら暗青にする。
            String disp = ChatColor.WHITE + channel.getName();
            if ( channel.getName().equalsIgnoreCase(dchannel) ) {
                disp = ChatColor.RED + channel.getName();
            } else if ( channel.getHided().contains(player) ) {
                disp = ChatColor.DARK_AQUA + channel.getName();
            }

            if ( !channel.getMembers().contains(player) &&
                    !channel.isGlobalChannel() ) {

                // 未参加で visible=false のチャンネルは表示しない
                if ( !channel.isVisible() ) {
                    continue;
                }
                disp = ChatColor.GRAY + channel.getName();
            }
            String desc = channel.getDescription();
            int onlineNum = channel.getOnlineNum();
            int memberNum = channel.getTotalNum();
            items.add(Messages.listFormat(disp, onlineNum, memberNum, desc));
        }

        return items;
    }
}
