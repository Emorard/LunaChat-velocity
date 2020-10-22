/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.velocitypowered.api.event.ResultedEvent;

public class LunaChatVelocityBaseResultedEvent extends LunaChatVelocityBaseEvent implements ResultedEvent<ResultedEvent.GenericResult> {

    private GenericResult result = GenericResult.allowed();

    public LunaChatVelocityBaseResultedEvent(String channelName) {
        super(channelName);
    }

    public boolean isCancelled() {
        return !result.isAllowed();
    }

    public void setCancelled(boolean cancelled) {
        this.result = cancelled ? GenericResult.denied() : GenericResult.allowed();
    }

    @Override
    public GenericResult getResult() {
        return result;
    }

    @Override
    public void setResult(GenericResult result) {
        this.result = result;
    }
}
