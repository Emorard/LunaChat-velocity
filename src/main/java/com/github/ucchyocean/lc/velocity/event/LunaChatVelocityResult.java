/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity.event;

import com.velocitypowered.api.event.ResultedEvent;

public class LunaChatVelocityResult implements ResultedEvent.Result {

    private boolean allowed;

    public LunaChatVelocityResult(boolean allowed) {
        this.allowed = allowed;
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }
}
