package de.hallo1142.gadse.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class CommandExecutor {

    public abstract void handleCommand(SlashCommandInteractionEvent event);

}
