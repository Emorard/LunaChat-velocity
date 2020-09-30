/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc.command;

import java.util.ArrayList;
import java.util.List;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.Messages;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * Lunachatコマンドの処理クラス
 * @author ucchy
 */
public class LunaChatCommand {

    private ArrayList<LunaChatSubCommand> commands;
    private ArrayList<LunaChatSubCommand> commonCommands;
    private JoinCommand joinCommand;
    private HelpCommand helpCommand;

    /**
     * コンストラクタ
     */
    public LunaChatCommand() {

        commands = new ArrayList<LunaChatSubCommand>();
        joinCommand = new JoinCommand();
        commands.add(joinCommand);
        commands.add(new LeaveCommand());
        commands.add(new ListCommand());
        commands.add(new InviteCommand());
        commands.add(new AcceptCommand());
        commands.add(new DenyCommand());
        commands.add(new KickCommand());
        commands.add(new BanCommand());
        commands.add(new PardonCommand());
        commands.add(new MuteCommand());
        commands.add(new UnmuteCommand());
        commands.add(new InfoCommand());
        commands.add(new LogCommand());
        commands.add(new CreateCommand());
        commands.add(new RemoveCommand());
        commands.add(new FormatCommand());
        commands.add(new ModeratorCommand());
        commands.add(new ModCommand());
        commands.add(new OptionCommand());
        commands.add(new TemplateCommand());
        commands.add(new SetCommand());
        helpCommand = new HelpCommand(commands);
        commands.add(helpCommand);

        commonCommands = new ArrayList<LunaChatSubCommand>();
        commonCommands.add(new HideCommand());
        commonCommands.add(new UnhideCommand());
        commonCommands.add(new DictionaryCommand());
        commonCommands.add(new DicCommand());
        commonCommands.add(new ReloadCommand());
    }

    /**
     * コマンドを実行したときに呼び出されるメソッド
     * @param sender 実行者
     * @param label 実行されたコマンドのラベル
     * @param args 実行されたコマンドの引数
     * @return 実行したかどうか（falseを返した場合、サーバーがUsageを表示する）
     */
    public boolean execute(ChannelMember sender, String label, String[] args) {

        // チャンネルチャットが無効でも利用できるコマンドはここで処理する
        // （hide, unhide, dic, dictionary, reload）
        if ( args.length >= 1 ) {
            for ( LunaChatSubCommand c : commonCommands ) {
                if ( c.getCommandName().equalsIgnoreCase(args[0]) ) {

                    // パーミッションの確認
                    String node = c.getPermissionNode();
                    if ( !sender.hasPermission(node) ) {
                        sender.sendMessage(Messages.errmsgPermission(node));
                        return true;
                    }

                    // 実行
                    return c.runCommand(sender, label, args);
                }
            }
        }

        // チャンネルチャット機能が無効になっている場合は、メッセージを表示して終了
        if ( !LunaChat.getConfig().isEnableChannelChat()
                && !sender.hasPermission("lunachat-admin") ) {
            sender.sendMessage(Messages.errmsgChannelChatDisabled());
            return true;
        }

        // 引数なしは、ヘルプを表示
        if (args.length == 0) {
            helpCommand.runCommand(sender, label, args);
            return true;
        }

        // 第1引数に指定されたコマンドを実行する
        for ( LunaChatSubCommand c : commands ) {
            if ( c.getCommandName().equalsIgnoreCase(args[0]) ) {

                // パーミッションの確認
                String node = c.getPermissionNode();
                if ( !sender.hasPermission(node) ) {
                    sender.sendMessage(Messages.errmsgPermission(node));
                    return true;
                }

                // 実行
                return c.runCommand(sender, label, args);
            }
        }

        // 第1引数がコマンドでないなら、joinが指定されたとみなす
        String node = joinCommand.getPermissionNode();
        if ( !sender.hasPermission(node) ) {
            sender.sendMessage(Messages.errmsgPermission(node));
            return true;
        }

        return joinCommand.runCommand(sender, label, args);
    }

    /**
     * TABキー補完が実行されたときに呼び出されるメソッド
     * @param sender TABキー補完の実行者
     * @param label 実行されたコマンドのラベル
     * @param args 実行されたコマンドの引数
     * @return 補完候補
     */
    public List<String> onTabComplete(ChannelMember sender, String label, String[] args) {
        if ( args.length == 1 ) {
            // コマンド名で補完する
            String arg = args[0].toLowerCase();
            ArrayList<String> coms = new ArrayList<String>();
            for ( LunaChatSubCommand c : commands ) {
                if ( c.getCommandName().startsWith(arg) &&
                        sender.hasPermission(c.getPermissionNode()) ) {
                    coms.add(c.getCommandName());
                }
            }
            for ( LunaChatSubCommand c : commonCommands ) {
                if ( c.getCommandName().startsWith(arg) &&
                        sender.hasPermission(c.getPermissionNode()) ) {
                    coms.add(c.getCommandName());
                }
            }
            return coms;

        } else if ( args.length == 2 && (
                args[0].equalsIgnoreCase("join") ||
                args[0].equalsIgnoreCase("info") ) ) {
            // 参加可能チャンネル名で補完する
            String arg = args[1].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : getListCanJoin(sender) ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        } else if ( args.length == 2 && (
                args[0].equalsIgnoreCase("ban") ||
                args[0].equalsIgnoreCase("pardon") ||
                args[0].equalsIgnoreCase("kick") ||
                args[0].equalsIgnoreCase("mute") ||
                args[0].equalsIgnoreCase("unmute") ) ) {
            // プレイヤー名で補完する
            String arg = args[1].toLowerCase();
            return getListPlayerNames(arg);

        } else if ( args.length == 3 && (
                args[0].equalsIgnoreCase("ban") ||
                args[0].equalsIgnoreCase("pardon") ||
                args[0].equalsIgnoreCase("kick") ||
                args[0].equalsIgnoreCase("mute") ||
                args[0].equalsIgnoreCase("unmute") ) ) {
            // チャンネル名で補完する
            String arg = args[2].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : getListCanJoin(sender) ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        } else if ( args.length == 2 && (
                args[0].equalsIgnoreCase("hide") ||
                args[0].equalsIgnoreCase("unhide") ) ) {

            // 参加可能チャンネル名とプレイヤー名と
            // 文字列"player", "channel"で補完する
            String arg = args[1].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : getListCanJoin(sender) ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            items.addAll(getListPlayerNames(arg));
            if ( "player".startsWith(arg) ) {
                items.add("player");
            }
            if ( "channel".startsWith(arg) ) {
                items.add("channel");
            }
            return items;

        } else if ( args.length == 3 &&
                (args[0].equalsIgnoreCase("hide") ||
                args[0].equalsIgnoreCase("unhide") ) &&
                args[1].equalsIgnoreCase("player") ) {

            // プレイヤー名で補完する
            String arg = args[2].toLowerCase();
            return getListPlayerNames(arg);

        } else if ( args.length == 3 &&
                (args[0].equalsIgnoreCase("hide") ||
                args[0].equalsIgnoreCase("unhide") ) &&
                args[1].equalsIgnoreCase("channel") ) {

            // チャンネル名で補完する
            String arg = args[2].toLowerCase();
            return getListPlayerNames(arg);

        } else if ( args.length == 2 && args[0].equalsIgnoreCase("remove") ) {
            // 削除可能チャンネル名で補完する
            String arg = args[1].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : getListCanRemove(sender) ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        } else if ( args.length == 2 &&
                (args[0].equalsIgnoreCase("dic") || args[0].equalsIgnoreCase("dictionary")) ) {
            // add、remove、viewで補完する
            String arg = args[1].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : new String[]{"add", "remove", "view"} ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        } else if ( args.length == 3 &&
                (args[0].equalsIgnoreCase("dic") || args[0].equalsIgnoreCase("dictionary")) &&
                args[1].equalsIgnoreCase("remove") ) {
            // 辞書に登録されているワードで補完する
            String arg = args[2].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name :
                    LunaChat.getAPI().getAllDictionary().keySet() ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        } else if ( args.length == 2 &&
                args[0].equalsIgnoreCase("set") ) {
            // "default" で補完する
            String arg = args[1].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            if ( "default".startsWith(arg) ) {
                items.add("default");
            }
            return items;

        } else if ( args.length == 3 &&
                args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("default") ) {
            // プレイヤー名で補完する
            String arg = args[2].toLowerCase();
            return getListPlayerNames(arg);

        } else if ( args.length == 4 &&
                args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("default") ) {
            // チャンネル名で補完する
            String arg = args[3].toLowerCase();
            ArrayList<String> items = new ArrayList<String>();
            for ( String name : getListCanJoin(sender) ) {
                if ( name.toLowerCase().startsWith(arg) ) {
                    items.add(name);
                }
            }
            return items;

        }
        return new ArrayList<String>();
    }

    /**
     * TAB補完用の参加可能チャンネルリストを返す
     * @param sender コマンド実行者
     * @return リスト
     */
    protected ArrayList<String> getListCanJoin(ChannelMember sender) {

        ArrayList<String> items = new ArrayList<String>();

        for ( Channel channel : LunaChat.getAPI().getChannels() ) {

            // BANされているチャンネルは対象外
            if ( channel.getBanned().contains(sender) ) {
                continue;
            }

            // 個人チャットは対象外
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // 未参加で visible=false のチャンネルは対象外
            if ( !channel.getMembers().contains(sender) &&
                    !channel.isGlobalChannel() && !channel.isVisible() ) {
                continue;
            }

            items.add(channel.getName());
        }

        return items;
    }

    /**
     * TAB補完用の削除可能チャンネルリストを返す
     * @param sender コマンド実行者
     * @return リスト
     */
    protected ArrayList<String> getListCanRemove(ChannelMember sender) {

        ArrayList<String> items = new ArrayList<String>();

        for ( Channel channel : LunaChat.getAPI().getChannels() ) {

            // 実行者がチャンネルモデレーターでない場合は対象外
            if ( !channel.hasModeratorPermission(sender) ) {
                continue;
            }

            // 個人チャットは対象外
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // グローバルチャンネルは対象外
            if ( channel.isGlobalChannel() ) {
                continue;
            }

            items.add(channel.getName());
        }

        return items;
    }

    /**
     * オンラインプレイヤーのうち、プレイヤー名が指定された文字列と前方一致するものをリストにして返す
     * @param pre 検索キー
     * @return プレイヤー名リスト
     */
    private List<String> getListPlayerNames(String pre) {
        String prefix = pre.toLowerCase();
        List<String> items = new ArrayList<String>();
        for ( String pname : LunaChat.getPlugin().getOnlinePlayerNames() ) {
            if ( pname.toLowerCase().startsWith(prefix) ) {
                items.add(pname);
            }
        }
        return items;
    }
}
