package com.github.ucchyocean.lc.command;

import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.member.ChannelMember;

public class SetCommand extends LunaChatSubCommand {

    private static final String COMMAND_NAME = "set";
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
    public void sendUsageMessage(ChannelMember sender, String label) {
        sender.sendMessage(Messages.usageSet1(label));
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

        // 引数チェック
        // このコマンドは、コンソールでも実行できる

        if ( args.length >= 3 && args[1].equalsIgnoreCase("default") ) {
            // 「/ch set default (player名) [channel名]」の実行

            String targetPlayer = args[2];

            Channel targetChannel = null;
            if ( args.length >= 4 ) {
                targetChannel = api.getChannel(args[3]);
            } else {
                targetChannel = api.getDefaultChannel(sender.getName());
            }

            if ( targetChannel == null ) {
                // チャンネルが正しく指定されなかった
                sender.sendMessage(Messages.errmsgNotExistOrNotSpecified());
                return true;
            }

            // 発言先を設定
            api.setDefaultChannel(targetPlayer, targetChannel.getName());

            sender.sendMessage(Messages.cmdmsgSetDefault(targetPlayer, targetChannel.getName()));

            // setされる相手のプレイヤーにも通知する
            ChannelMember target = ChannelMember.getChannelMember(targetPlayer);
            if ( target != null ) {
                target.sendMessage(Messages.cmdmsgSet(targetChannel.getName()));
            }

            return true;
        }

        return false;
    }

}
