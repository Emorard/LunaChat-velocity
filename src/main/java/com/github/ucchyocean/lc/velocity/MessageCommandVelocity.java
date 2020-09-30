/*
 * @author     Emorard
 * @license    LGPLv3
 * @copyright  Copyright Emorard 2020
 */
package com.github.ucchyocean.lc.velocity;

import com.github.ucchyocean.lc.command.LunaChatMessageCommand;
import com.github.ucchyocean.lc.member.ChannelMember;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageCommandVelocity implements Command {

    private LunaChatMessageCommand command;

    public MessageCommandVelocity() {
        command = new LunaChatMessageCommand();
    }
    /**
     * Executes the command for the specified {@link CommandSource}.
     *
     * @param source the source of this command
     * @param args   the arguments for this command
     */
    @Override
    public void execute(CommandSource source, @NotNull @NonNull String[] args) {
        command.execute(ChannelMember.getChannelMember(source), "m", args);
    }

    /**
     * Provides tab complete suggestions for a command for a specified {@link CommandSource}.
     *
     * @param source      the source to run the command for
     * @param currentArgs the current, partial arguments for this command
     * @return tab complete suggestions
     */
    @Override
    public List<String> suggest(CommandSource source, @NotNull @NonNull String[] currentArgs) {
        return command.onTabComplete(ChannelMember.getChannelMember(source), "m", currentArgs);
    }
}
