package de.hallo1142.gadse.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class SettingsCommand extends CommandExecutor {

    public void handleCommand(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "alliancecategory":
                this.handleAllianceCategory(event);
                break;
            default:
                System.err.println("Unknown settings subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleAllianceCategory(SlashCommandInteractionEvent event) {
        System.out.println(event.getOption("id").getAsLong());
    }

}
