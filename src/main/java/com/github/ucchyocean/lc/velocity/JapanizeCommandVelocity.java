/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity;

import com.github.ucchyocean.lc.command.LunaChatJapanizeCommand;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class JapanizeCommandVelocity implements Command {

    private LunaChatJapanizeCommand command;

    public JapanizeCommandVelocity() {
        command = new LunaChatJapanizeCommand();
    }
    /**
     * Executes the command for the specified {@link CommandSource}.
     *
     * @param source the source of this command
     * @param args   the arguments for this command
     */
    @Override
    public void execute(CommandSource source, @NotNull @NonNull String[] args) {
        command.execute(ChannelMember.getChannelMember(source), "jp", args);
    }
}
