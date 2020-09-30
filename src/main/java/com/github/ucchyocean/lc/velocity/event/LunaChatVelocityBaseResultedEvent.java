/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.velocitypowered.api.event.ResultedEvent;

public class LunaChatVelocityBaseResultedEvent extends LunaChatVelocityBaseEvent implements ResultedEvent<LunaChatVelocityResult> {

    private LunaChatVelocityResult result;

    public LunaChatVelocityBaseResultedEvent(String channelName) {
        super(channelName);
    }

    @Override
    public LunaChatVelocityResult getResult() {
        return result;
    }

    @Override
    public void setResult(LunaChatVelocityResult result) {
        this.result = result;
    }
}
