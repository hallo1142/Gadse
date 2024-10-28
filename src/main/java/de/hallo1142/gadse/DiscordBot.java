package de.hallo1142.gadse;

import de.hallo1142.gadse.events.SlashCommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class DiscordBot {

    public DiscordBot() {
        JDA api = JDABuilder
                .createDefault(System.getenv("JDA_TOKEN"))
                .setActivity(Activity.watching("Katze"))
                .build();
        this.registerCommands(api);
        api.addEventListener(new SlashCommandEvent());
        Database db = new Database();
    }

    private void registerCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("allianceadmin", "Verwaltung für Allianzen")
                        .addSubcommands(
                                new SubcommandData("create", "Erstellt eine neue Allianz")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true),
                                new SubcommandData("delete", "Löscht eine Allianz")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true),
                                new SubcommandData("list", "Liste der aktuellen Allianzen"),
                                new SubcommandData("addadmin", "Fügt einer Allianz einen Admin hinzu")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true)
                                        .addOption(OptionType.USER, "user", "User welcher als Admin angelegt werden soll", true),
                                new SubcommandData("removeadmin", "Entferne einen Admin einer Allianz")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true)
                                        .addOption(OptionType.USER, "user", "User welcher als Admin entfernt werden soll", true),
                                new SubcommandData("addmember", "Fügt ein Mitglied einer Allianz hinzu")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true)
                                        .addOption(OptionType.USER, "user", "User welcher als Mitglied hinzugeügt werden soll", true),
                                new SubcommandData("removemember", "Entfernt ein Mitglied aus einer Allianz")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true)
                                        .addOption(OptionType.USER, "user", "User welcher als Mitglied entfernt werden soll"),
                                new SubcommandData("info", "Info über eine Allianz")
                                        .addOption(OptionType.STRING, "name", "Name der Allianz", true)
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES)),
                Commands.slash("alliance", "User-Commands für bestehende Allianzen")
                        .addSubcommands(
                                new SubcommandData("addmember", "Fügt deiner Allianz ein Mitglied hinzu")
                                        .addOption(OptionType.USER, "user", "User der hinzugefügt werden soll", true),
                                new SubcommandData("removemember", "Entferne ein Mitglied aus deiner Allianz")
                                        .addOption(OptionType.USER, "user", "User der entfernt werden soll", true),
                                new SubcommandData("info", "Info über deine Allianz")
                        ),
                Commands.slash("settings", "Einstellungen für den Bot")
                    .addSubcommands(
                            new SubcommandData("alliancecategory", "Setze die Kategorie für Allianzchannel")
                                    .addOption(OptionType.INTEGER, "id", "Kategorie-ID", true),
                            new SubcommandData("alliancerole", "Setze die Bündnis Rolle")
                                    .addOption(OptionType.ROLE, "role", "Rolle die gesetzt werden soll", true)
                    )
                        .addSubcommandGroups(
                                new SubcommandGroupData("channelwhitelist", "Rollen die immer Bündnischanneln hinzugefügt werden sollen")
                                        .addSubcommands(
                                                new SubcommandData("list", "Liste der aktuellen Rollen in der Channelwhitelist"),
                                                new SubcommandData("add", "Füge eine Rolle der Channelwhitelist hinzu")
                                                        .addOption(OptionType.ROLE, "role", "Rolle die hinzugefügt werden soll", true),
                                                new SubcommandData("remove", "Entferne eine Rolle aus der Channelwhitelist")
                                                        .addOption(OptionType.ROLE, "role", "Rolle die entfernt werden soll", true)
                                        )
                        )
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        ).queue();
    }

}
