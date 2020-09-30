/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc.command;

import java.util.ArrayList;

import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * helpコマンドの実行クラス
 * @author ucchy
 */
public class HelpCommand extends LunaChatSubCommand {

    private static final String COMMAND_NAME = "help";
    private static final String PERMISSION_NODE = "lunachat." + COMMAND_NAME;

    // 1ページに表示するコマンドヘルプの項目数
    private static final int PAGE_ITEM_NUM = 6;

    private ArrayList<LunaChatSubCommand> commands;

    /**
     * コンストラクタ
     * @param commands
     */
    public HelpCommand(ArrayList<LunaChatSubCommand> commands) {
        this.commands = commands;
    }

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
        sender.sendMessage(Messages.usageHelp(label));
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
    public boolean runCommand(ChannelMember sender, String label, String[] args) {

        CommandType type = CommandType.USER;
        int page = 1;

        if ( args.length >= 2 &&
                (args[1].equalsIgnoreCase("mod")
                        || args[1].equalsIgnoreCase("moderator") ) ) {
            type = CommandType.MODERATOR;
        } else if ( args.length >= 2 && args[1].equalsIgnoreCase("admin") ) {
            type = CommandType.ADMIN;
        } else if ( args.length >= 2 && args[1].matches("[1-9]") ) {
            page = Integer.parseInt(args[1]);
        }

        if ( args.length >= 3 && args[2].matches("[1-9]") ) {
            page = Integer.parseInt(args[2]);
        }

        printUsage(sender, label, type, page);

        return true;
    }

    /**
     * コマンドの使い方を senderに送る
     * @param sender
     * @param label
     * @param type コマンド種別
     * @param page ページ
     */
    private void printUsage(ChannelMember sender, String label,
            CommandType type, int page) {

        String typeDesc;
        switch (type) {
        case USER:
            typeDesc = "user";
            break;
        case MODERATOR:
            typeDesc = "moderator";
            break;
        case ADMIN:
            typeDesc = "admin";
            break;
        default:
            typeDesc = "user";
        }

        // 種別に該当するコマンドを取得
        ArrayList<LunaChatSubCommand> com = new ArrayList<LunaChatSubCommand>();
        for ( LunaChatSubCommand c : commands ) {
            if ( c.getCommandType() == type
                    && sender.hasPermission(c.getPermissionNode()) ) {
                com.add(c);
            }
        }

        int lastPage = ( (com.size() - 1) / PAGE_ITEM_NUM) + 1;

        // 表示処理
        sender.sendMessage(Messages.usageTop(typeDesc, page, lastPage));
        for (int index=(page-1)*PAGE_ITEM_NUM; index<page*PAGE_ITEM_NUM; index++) {
            if ( index >= com.size() ) break;
            com.get(index).sendUsageMessage(sender, label);
        }
        sender.sendMessage(Messages.usageFoot());
        if ( page < lastPage ) {
            if ( type != CommandType.USER ) {
                sender.sendMessage(Messages.usageNoticeNextPage(label, typeDesc, (page + 1)));
            } else {
                sender.sendMessage(Messages.usageNoticeNextPage(label, "", (page + 1)));
            }
        }
    }

}
