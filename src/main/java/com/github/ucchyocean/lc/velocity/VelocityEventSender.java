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
package com.github.ucchyocean.lc.velocity;

import com.github.ucchyocean.lc.LunaChatVelocity;
import com.github.ucchyocean.lc.event.EventResult;
import com.github.ucchyocean.lc.event.EventSenderInterface;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.github.ucchyocean.lc.velocity.event.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VelocityEventSender implements EventSenderInterface {
    /**
     * チャンネルチャットのチャットイベント
     *
     * @param channelName     チャンネル名
     * @param member
     * @param originalMessage 発言内容
     * @param ngMaskedMessage 発言内容（NGマスク後）
     * @param messageFormat   発言に適用されるフォーマット
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelChatEvent(String channelName, ChannelMember member, String originalMessage, String ngMaskedMessage, String messageFormat) {
        EventResult result = new EventResult();
        LunaChatVelocityChannelChatEvent event = new LunaChatVelocityChannelChatEvent(channelName, member, originalMessage, ngMaskedMessage, messageFormat);
        LunaChatVelocity.PROXY.getEventManager().fire(event);
        result.setCancelled(event.isCancelled());
        result.setNgMaskedMessage(event.getNgMaskedMessage());
        result.setMessageFormat(event.getMessageFormat());
        return result;
    }

    /**
     * チャンネル作成イベント
     *
     * @param channelName チャンネル名
     * @param member      作成した人
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelCreateEvent(String channelName, ChannelMember member) {
        EventResult result = new EventResult();
        LunaChatVelocityChannelCreateEvent event = new LunaChatVelocityChannelCreateEvent(channelName, member);
        LunaChatVelocity.PROXY.getEventManager().fire(event);
        result.setCancelled(event.isCancelled());
        result.setChannelName(event.getChannelName());
        return result;
    }

    /**
     * メンバー変更イベント
     *
     * @param channelName チャンネル名
     * @param before      変更前のメンバー
     * @param after       変更後のメンバー
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelMemberChangedEvent(String channelName, List<ChannelMember> before, List<ChannelMember> after) {
        EventResult result = new EventResult();
        LunaChatVelocityChannelMemberChangedEvent event = new LunaChatVelocityChannelMemberChangedEvent(channelName, before, after);
        LunaChatVelocity.PROXY.getEventManager().fire(event);
        result.setCancelled(event.isCancelled());
        return result;
    }

    /**
     * チャンネルチャットのメッセージイベント。このイベントはキャンセルできない。
     *
     * @param channelName     チャンネル名
     * @param member          発言者
     * @param message         発言内容（NGマスクやJapanizeされた後の内容）
     * @param recipients      受信者
     * @param displayName     発言者の表示名
     * @param originalMessage 発言内容（元々の内容）
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelMessageEvent(String channelName, ChannelMember member, String message, List<ChannelMember> recipients, String displayName, String originalMessage) {
        EventResult result = new EventResult();
        LunaChatVelocityChannelMessageEvent event = new LunaChatVelocityChannelMessageEvent(channelName, member, message, recipients, displayName, originalMessage);
        LunaChatVelocity.PROXY.getEventManager().fire(event);
        result.setMessage(event.getMessage());
        result.setRecipients(event.getRecipients());
        return result;
    }

    /**
     * オプション変更イベント
     *
     * @param channelName チャンネル名
     * @param member      オプションを変更した人
     * @param options     変更後のオプション
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelOptionChangedEvent(String channelName, ChannelMember member, Map<String, String> options) {
        EventResult result = new EventResult();
        LunaChatVelocity.PROXY.getEventManager().fire(
                new LunaChatVelocityChannelOptionChangedEvent(channelName, member, options)
        ).whenComplete((co, ex) -> {
            result.setCancelled(!co.getResult().isAllowed());
            result.setOptions(co.getOptions());
        });
        return result;
    }

    /**
     * チャンネル削除イベント
     *
     * @param channelName チャンネル名
     * @param member      削除を実行した人
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatChannelRemoveEvent(String channelName, ChannelMember member) {
        EventResult result = new EventResult();
        LunaChatVelocity.PROXY.getEventManager().fire(
                new LunaChatVelocityChannelRemoveEvent(channelName, member)
        ).whenComplete((co, ex) -> {
            result.setCancelled(!co.getResult().isAllowed());
            result.setChannelName(co.getChannelName());
        });
        return result;
    }

    /**
     * Japanize変換が行われた後に呼び出されるイベント
     *
     * @param channelName チャンネル名
     * @param member      発言したメンバー
     * @param original    変換前の文字列
     * @param japanized   変換後の文字列
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatPostJapanizeEvent(String channelName, ChannelMember member, String original, String japanized) {
        EventResult result = new EventResult();
        LunaChatVelocity.PROXY.getEventManager().fire(
                new LunaChatVelocityPostJapanizeEvent(channelName, member, original, japanized)
        ).whenComplete((co, ex) -> {
            result.setCancelled(!co.getResult().isAllowed());
            result.setJapanized(co.getJapanized());
        });
        return result;
    }

    /**
     * チャンネルチャットへの発言前に発生するイベント
     *
     * @param channelName チャンネル名
     * @param member      発言したメンバー
     * @param message     発言内容
     * @return イベント実行結果
     */
    @Override
    public EventResult sendLunaChatPreChatEvent(String channelName, ChannelMember member, String message) {
        EventResult result = new EventResult();
        LunaChatVelocity.PROXY.getEventManager().fire(
                new LunaChatVelocityPreChatEvent(channelName, member, message)
        ).whenComplete((co, ex) -> {
            result.setCancelled(!co.getResult().isAllowed());
            result.setMessage(co.getMessage());
        });
        return result;
    }
}
