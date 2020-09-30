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
package com.github.ucchyocean.lc.velocity.event;

import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * チャンネルチャットへの発言前に発生するイベント
 * @author ucchy
 */
public class LunaChatVelocityPreChatEvent extends LunaChatVelocityBaseResultedEvent {

    private ChannelMember member;
    private String message;

    public LunaChatVelocityPreChatEvent(String channelName, ChannelMember member, String message) {
        super(channelName);
        this.member = member;
        this.message = message;
    }

    /**
     * 発言を行ったプレイヤーを取得します。
     * @return 発言したプレイヤー
     */
    public ChannelMember getMember() {
        return member;
    }

    /**
     * 発言内容を取得します。
     * @return 発言内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 発言先チャンネルを変更します。
     * @param channelName 発言先チャンネル名
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * 発言先チャンネルを変更します。
     * @param channel 発言先チャンネル
     */
    public void setChannel(Channel channel) {
        if ( channel == null ) return;
        this.channelName = channel.getName();
    }

    /**
     * 発言内容を変更します。
     * @param message 発言内容
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
