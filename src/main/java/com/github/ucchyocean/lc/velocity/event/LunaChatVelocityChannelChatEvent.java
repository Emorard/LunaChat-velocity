/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.github.ucchyocean.lc.member.ChannelMember;

public class LunaChatVelocityChannelChatEvent extends LunaChatVelocityBaseResultedEvent {

    private ChannelMember member;
    private String originalMessage;
    private String ngMaskedMessage;
    private String messageFormat;

    public LunaChatVelocityChannelChatEvent(String channelName, ChannelMember member,
                                            String originalMessage, String ngMaskedMessage,
                                            String messageFormat) {
        super(channelName);
        this.member = member;
        this.originalMessage = originalMessage;
        this.ngMaskedMessage = ngMaskedMessage;
        this.messageFormat = messageFormat;
    }

    public ChannelMember getMember() {
        return member;
    }

    public String getPreReplaceMessage() {
        return originalMessage;
    }

    public String getNgMaskedMessage() {
        return ngMaskedMessage;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setNgMaskedMessage(String ngMaskedMessage) {
        this.ngMaskedMessage = ngMaskedMessage;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

}
