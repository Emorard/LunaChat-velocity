/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc;

import com.github.ucchyocean.lc.channel.ChannelManager;
import com.github.ucchyocean.lc.velocity.*;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;


import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Plugin(id = "lunachat",
        name = "LunaChat-velocity",
        version = "1.3.0-SNAPSHOT",
        description = "LunaChat for Velocity",
        authors = {"ucchy, YukiLeafX, LazyGon, tomo1560 (LunaChat)", "Emorard"}
)
public class LunaChatVelocity implements PluginInterface {
    public static ProxyServer PROXY;

    private static LunaChatVelocity instance;

    @Inject
    private ProxyServer proxy;
    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path path;

    private HashMap<String, String> history;
    private LunaChatConfig config;
    private ChannelManager manager;
    private UUIDCacheData uuidCacheData;
    private LunaChatLogger normalChatLogger;

    private File dataFolder;
    private File jarFile;

    private LegacyChannelIdentifier legacyChannelIdentifier;

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        PROXY = proxy;
        instance = this;

        dataFolder = path.toFile();
        jarFile = new File(dataFolder, "../LunaChat.jar");

        LunaChat.setPlugin(this);
        LunaChat.setMode(LunaChatMode.VELOCITY);

        // 初期化
        config = new LunaChatConfig(getDataFolder(), getPluginJarFile());
        uuidCacheData = new UUIDCacheData(getDataFolder());
        Messages.initialize(new File(getDataFolder(), "messages"), getPluginJarFile(), config.getLang());
        history = new HashMap<>();

        manager = new ChannelManager();
        normalChatLogger = new LunaChatLogger("==normalchat");

        // チャンネルチャット無効なら、デフォルト発言先をクリアする
        if (!config.isEnableChannelChat()) {
            manager.removeAllDefaultChannels();
        }

        /*
        // BungeePermsのロード
        Plugin temp = getProxy().getPluginManager().getPlugin("BungeePerms");
        if ( temp != null ) {
            bungeeperms = BungeePermsBridge.load(temp);
        }

        // LuckPermsのロード
        temp = getProxy().getPluginManager().getPlugin("LuckPerms");
        if ( temp != null ) {
            luckperms = LuckPermsBridge.load(temp);
        }
         */

        // コマンド登録
        proxy.getCommandManager().register(
                new LunaChatCommandVelocity(), "lunachat", "lc", "ch");
        proxy.getCommandManager().register(
                new MessageCommandVelocity(), "tell", "message", "m", "t", "w");
        proxy.getCommandManager().register(
                new ReplyCommandVelocity(), "reply", "r");
        proxy.getCommandManager().register(
                new JapanizeCommandVelocity(), "japanize", "jp");

        // リスナー登録
        proxy.getEventManager().register(this, new VelocityEventListener(this));

        // イベント実行クラスの登録
        LunaChat.setEventSender(new VelocityEventSender());

        // プラグインチャンネル登録
        legacyChannelIdentifier = new LegacyChannelIdentifier(LunaChat.PMC_MESSAGE);

        proxy.getChannelRegistrar().register(legacyChannelIdentifier);
    }

    public static LunaChatVelocity getInstance() {
        if (instance == null) {
            instance = (LunaChatVelocity) PROXY.getPluginManager().getPlugin("LunaChat").orElse(null);
        }
        return instance;
    }

    public LunaChatConfig getConfig() {
        return config;
    }

    /**
     * プライベートメッセージの受信履歴を記録する
     *
     * @param reciever 受信者
     * @param sender   送信者
     */
    protected void putHistory(String reciever, String sender) {
        history.put(reciever, sender);
    }

    /**
     * プライベートメッセージの受信履歴を取得する
     *
     * @param reciever 受信者
     * @return 送信者
     */
    protected String getHistory(String reciever) {
        return history.get(reciever);
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public LegacyChannelIdentifier getLegacyChannelIdentifier() {
        return legacyChannelIdentifier;
    }

    @Override
    public File getPluginJarFile() {
        return jarFile;
    }

    @Override
    public LunaChatConfig getLunaChatConfig() {
        return config;
    }

    @Override
    public LunaChatAPI getLunaChatAPI() {
        return manager;
    }

    /**
     * プラグインのデータ格納フォルダを取得する
     *
     * @return データ格納フォルダ
     */
    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public LunaChatLogger getNormalChatLogger() {
        return normalChatLogger;
    }

    @Override
    public Set<String> getOnlinePlayerNames() {
        return proxy.getAllPlayers().stream().map(Player::getUsername).collect(Collectors.toSet());
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.WARNING) {
            logger.warn(msg);
        } else if (level == Level.SEVERE) {
            logger.error(msg);
        } else {
            logger.info(msg);
        }
    }

    @Override
    public UUIDCacheData getUUIDCacheData() {
        return uuidCacheData;
    }

    @Override
    public void runAsyncTask(Runnable task) {
        proxy.getScheduler().buildTask(this, task).schedule();
    }
}
