/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc.command;

import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * templateコマンドの実行クラス
 * @author ucchy
 */
public class TemplateCommand extends LunaChatSubCommand {

    private static final String COMMAND_NAME = "template";
    private static final String PERMISSION_NODE = "lunachat-admin." + COMMAND_NAME;

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
        return CommandType.ADMIN;
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
        sender.sendMessage(Messages.usageTemplate(label));
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

        // 引数チェック
        // このコマンドは、コンソールでも実行できる
        if ( args.length <= 1 ) {
            sender.sendMessage(Messages.errmsgCommand());
            return true;
        }

        if ( !args[1].matches("[0-9]") ) {
            sender.sendMessage(Messages.errmsgInvalidTemplateNumber());
            sender.sendMessage(Messages.usageTemplate(label));
            return true;
        }

        String id = args[1];
        StringBuilder buf = new StringBuilder();
        if ( args.length >= 3 ) {
            for (int i = 2; i < args.length; i++) {
                buf.append(args[i] + " ");
            }
        }
        String format = buf.toString().trim();

        // 登録を実行
        if ( format.equals("") ) {
            api.removeTemplate(id);
            sender.sendMessage(Messages.cmdmsgTemplateRemove(id));
        } else {
            api.setTemplate(id, format);
            sender.sendMessage(Messages.cmdmsgTemplate(id, format));
        }

        return true;

    }
}
