/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import java.util.List;

import com.github.ucchyocean.lc.member.ChannelMember;

public class LunaChatVelocityChannelMemberChangedEvent extends LunaChatVelocityBaseResultedEvent {

    private List<ChannelMember> before;
    private List<ChannelMember> after;

    public LunaChatVelocityChannelMemberChangedEvent(
            String channelName, List<ChannelMember> before, List<ChannelMember> after) {
        super(channelName);
        this.before = before;
        this.after = after;
    }

    public List<ChannelMember> getMembersBefore() {
        return before;
    }

    public List<ChannelMember> getMembersAfter() {
        return after;
    }
}
