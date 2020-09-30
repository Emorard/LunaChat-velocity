/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc.command;

import java.util.List;

import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * formatコマンドの実行クラス
 * @author ucchy
 */
public class FormatCommand extends LunaChatSubCommand {

    private static final String COMMAND_NAME = "format";
    private static final String PERMISSION_NODE = "lunachat." + COMMAND_NAME;

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
        return CommandType.MODERATOR;
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
        sender.sendMessage(Messages.usageFormat(label));
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
        // このコマンドは、デフォルトチャンネルでない人も実行できるが、その場合はチャンネル名を指定する必要がある
        String format = "";
        String cname = null;
        if ( args.length >= 2 ) {
            Channel def = api.getDefaultChannel(sender.getName());
            if ( def != null ) {
                cname = def.getName();
            }
            for (int i = 1; i < args.length; i++) {
                format = format + args[i] + " ";
            }
        } else if ( args.length >= 3 ) {
            cname = args[1];
            for (int i = 2; i < args.length; i++) {
                format = format + args[i] + " ";
            }
        } else {
            sender.sendMessage(Messages.errmsgCommand());
            return true;
        }
        format = format.trim();

        // チャンネルが存在するかどうかをチェックする
        Channel channel = api.getChannel(cname);
        if ( channel == null ) {
            sender.sendMessage(Messages.errmsgNotExist());
            return true;
        }

        // モデレーターかどうか確認する
        if ( !channel.hasModeratorPermission(sender) ) {
            sender.sendMessage(Messages.errmsgNotModerator());
            return true;
        }

        // 制約キーワードを確認する
        List<String> constraints = config.getFormatConstraint();
        String tempFormat = new String(format);
        for ( int i=0; i<=9; i++ ) {
            String key = "%" + i;
            if ( tempFormat.contains(key) && api.getTemplate(i + "") != null ) {
                tempFormat = tempFormat.replace(key, api.getTemplate("" + i));
                break;
            }
        }
        for ( String constraint : constraints ) {
            if ( !tempFormat.contains(constraint) ) {
                sender.sendMessage(Messages.errmsgFormatConstraint(constraint));
                return true;
            }
        }

        // フォーマットの設定
        channel.setFormat(format);
        sender.sendMessage(Messages.cmdmsgFormat(format));
        channel.save();
        return true;
    }
}
