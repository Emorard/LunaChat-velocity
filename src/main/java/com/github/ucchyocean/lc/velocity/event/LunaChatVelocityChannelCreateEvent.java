/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.github.ucchyocean.lc.member.ChannelMember;

public class LunaChatVelocityChannelCreateEvent extends LunaChatVelocityBaseResultedEvent {

    private ChannelMember member;

    public LunaChatVelocityChannelCreateEvent(String channelName, ChannelMember member) {
        super(channelName);
        this.member = member;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ChannelMember getMember() {
        return member;
    }
}
