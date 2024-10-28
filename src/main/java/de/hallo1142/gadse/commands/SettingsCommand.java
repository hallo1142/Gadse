package de.hallo1142.gadse.commands;

import de.hallo1142.gadse.Database;
import de.hallo1142.gadse.entities.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.hibernate.Session;

import java.awt.*;
import java.util.Objects;

public class SettingsCommand extends CommandExecutor {

    private final Database database;

    public SettingsCommand(Database database) {
        this.database = database;
    }

    public void handleCommand(SlashCommandInteractionEvent event) {
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "alliancecategory":
                this.handleAllianceCategory(event);
                break;
            case "alliancerole":
                this.handleAllianceRole(event);
                break;
            default:
                System.err.println("Unknown settings subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleAllianceCategory(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getOption("id") == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Kategorie")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Kategorie-ID an.")
                    .build()).queue();
            return;
        }

        String number = event.getOption("id").getAsString();
        long id;
        try {
            id = Long.parseLong(number);
        } catch (NumberFormatException e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Kategorie")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Kategorie-ID an.")
                    .build()).queue();
            return;
        }

        Category category = Objects.requireNonNull(event.getGuild()).getCategoryById(id);

        if (category == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Kategorie")
                    .setDescription("<a:catNo:1300217987675979917> Die angegebene Kategorie konnte nicht gefunden werden.")
                    .build()).queue();
            return;
        }

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null) {
                settings = new GuildSettings();
                settings.guildId = event.getGuild().getIdLong();
            }
            settings.allianceCategoryId = category.getIdLong();
            session.persist(settings);
            session.getTransaction().commit();
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Kategorie geändert")
                    .setDescription("<a:catYes:1300217972324962380> Die Kategorie wurde erfolgreich zu " + category.getName() + " geändert.")
                    .build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).queue();
            e.printStackTrace();
        }
    }

    private void handleAllianceRole(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getOption("role") == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Rolle")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Rolle an.")
                    .build()).queue();
            return;
        }

        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null) {
                settings = new GuildSettings();
                settings.guildId = event.getGuild().getIdLong();
            }
            settings.allianceRole = role.getIdLong();
            session.persist(settings);
            session.getTransaction().commit();
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Rolle geändert")
                    .setDescription("<a:catYes:1300217972324962380> Die Rolle wurde erfolgreich zu " + role.getAsMention() + " geändert.")
                    .build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).queue();
            e.printStackTrace();
        }
    }

}
