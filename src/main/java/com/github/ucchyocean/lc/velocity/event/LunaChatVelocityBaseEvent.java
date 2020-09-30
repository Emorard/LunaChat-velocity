/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.channel.Channel;

public abstract class LunaChatVelocityBaseEvent {

    protected String channelName;

    public LunaChatVelocityBaseEvent(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public Channel getChannel() {
        return LunaChat.getAPI().getChannel(channelName);
    }
}
