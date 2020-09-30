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

import com.github.ucchyocean.lc.member.ChannelMember;

/**
 * チャンネル削除イベント
 * @author ucchy
 */
public class LunaChatVelocityChannelRemoveEvent extends LunaChatVelocityBaseResultedEvent {

    private ChannelMember member;

    public LunaChatVelocityChannelRemoveEvent(String channelName, ChannelMember member) {
        super(channelName);
        this.member = member;
    }

    /**
     * チャンネルを削除した人を取得する。
     * @return チャンネルを削除したChannelMember
     */
    public ChannelMember getMember() {
        return member;
    }
}
