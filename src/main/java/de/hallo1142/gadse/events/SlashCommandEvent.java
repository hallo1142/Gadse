package de.hallo1142.gadse.events;

import de.hallo1142.gadse.commands.AllianceAdminCommand;
import de.hallo1142.gadse.commands.AllianceCommand;
import de.hallo1142.gadse.commands.CommandExecutor;
import de.hallo1142.gadse.commands.SettingsCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;

public class SlashCommandEvent extends ListenerAdapter {

    private final HashMap<String, CommandExecutor> commands = new HashMap<>();

    public SlashCommandEvent() {
        this.commands.put("settings", new SettingsCommand());
        this.commands.put("allianceadmin", new AllianceAdminCommand());
        this.commands.put("alliance", new AllianceCommand());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        CommandExecutor command = commands.get(event.getName());
        if (command != null) {
            command.handleCommand(event);
        } else {
            System.err.println("Command not found: " + event.getName());
        }
    }
}
