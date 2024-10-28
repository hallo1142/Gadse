package de.hallo1142.gadse.commands;

import de.hallo1142.gadse.Database;
import de.hallo1142.gadse.entities.AllianceChannelWhitelist;
import de.hallo1142.gadse.entities.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.hibernate.Session;

import java.awt.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
            case "add":
                if (event.getSubcommandGroup() != null == event.getSubcommandGroup().equals("channelwhitelist")) {
                    this.handleChannelWhitelistAdd(event);
                }
                break;
            case "remove":
                if (event.getSubcommandGroup() != null == event.getSubcommandGroup().equals("channelwhitelist")) {
                    this.handleChannelWhitelistRemove(event);
                }
                break;
            case "list":
                if (event.getSubcommandGroup() != null == event.getSubcommandGroup().equals("channelwhitelist")) {
                    this.handleChannelWhitelistList(event);
                }
                break;
            default:
                System.err.println("Unknown settings subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleChannelWhitelistList(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        try (Session session = this.database.getSessionFactory().openSession()) {
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null || settings.getAllianceChannelRoles() == null || settings.getAllianceChannelRoles().isEmpty()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Keine Rollen gefunden")
                        .setDescription("<a:catNo:1300217987675979917> Aktuell sind keine Rollen auf der Whitelist.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            Set<AllianceChannelWhitelist> roleSet = settings.getAllianceChannelRoles();
            session.detach(settings);
            session.close();

            StringBuilder sb = new StringBuilder();

            roleSet.forEach(role -> {
               Role guildRole = event.getGuild().getRoleById(role.getRoleId());
               if (guildRole == null) {
                   database.getSessionFactory().inTransaction(session1 -> session1.remove(role));
                   return;
               }
               sb.append(guildRole.getAsMention()).append("\n");
            });

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Aktuelle Rollen")
                    .setDescription(sb.toString())
                    .build()).setEphemeral(true).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).setEphemeral(true).queue();
            e.printStackTrace();
        }

    }

    private void handleChannelWhitelistRemove(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getOption("role") == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Rolle")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Rolle an.")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();
        AtomicBoolean success = new AtomicBoolean(true);

        database.getSessionFactory().inTransaction(session -> {

            AllianceChannelWhitelist acw = session.find(AllianceChannelWhitelist.class, role.getId());
            if (acw == null) {
                success.set(false);
                return;
            }
            session.remove(acw);
        });

        if (success.get()) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Rolle entfernt")
                    .setDescription("<a:catYes:1300217972324962380> Die Rolle " + role.getAsMention() + " wurde erfolgreich entfernt.")
                    .build()).setEphemeral(true).queue();
        } else {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Rolle existiert nicht")
                    .setDescription("<a:catNo:1300217987675979917> Diese Rolle ist nicht auf der Whitelist.")
                    .build()).setEphemeral(true).queue();
        }
    }

    private void handleChannelWhitelistAdd(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getOption("role") == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Rolle")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Rolle an.")
                    .build()).setEphemeral(true).queue();
            return;
        }

        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();
        AtomicBoolean success = new AtomicBoolean(true);

        database.getSessionFactory().inTransaction(session -> {

            AllianceChannelWhitelist acw = session.find(AllianceChannelWhitelist.class, role.getId());
            if (acw != null) {
                success.set(false);
                return;
            }

            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null) {
                settings = new GuildSettings();
                settings.setGuildId(event.getGuild().getIdLong());
                session.persist(settings);
            }

            AllianceChannelWhitelist acwnew = new AllianceChannelWhitelist();
            acwnew.setRoleId(role.getIdLong());
            acwnew.setGuildSettings(settings);
            session.persist(acwnew);
        });

        if (success.get()) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Rolle hinzugefügt")
                    .setDescription("<a:catYes:1300217972324962380> Die Rolle " + role.getAsMention() + " wurde erfolgreich hinzugefügt.")
                    .build()).setEphemeral(true).queue();
        } else {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Rolle existiert bereits")
                    .setDescription("<a:catNo:1300217987675979917> Diese Rolle wurde bereits hinzugefügt.")
                    .build()).setEphemeral(true).queue();
        }
    }

    private void handleAllianceCategory(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getOption("id") == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Kategorie")
                    .setDescription("<a:catNo:1300217987675979917> Bitte gib eine gültige Kategorie-ID an.")
                    .build()).setEphemeral(true).queue();
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
                    .build()).setEphemeral(true).queue();
            return;
        }

        Category category = Objects.requireNonNull(event.getGuild()).getCategoryById(id);

        if (category == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ungültige Kategorie")
                    .setDescription("<a:catNo:1300217987675979917> Die angegebene Kategorie konnte nicht gefunden werden.")
                    .build()).setEphemeral(true).queue();
            return;
        }

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null) {
                settings = new GuildSettings();
                settings.setGuildId(event.getGuild().getIdLong());
            }
            settings.setAllianceCategoryId(category.getIdLong());
            session.persist(settings);
            session.getTransaction().commit();
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Kategorie geändert")
                    .setDescription("<a:catYes:1300217972324962380> Die Kategorie wurde erfolgreich zu " + category.getName() + " geändert.")
                    .build()).setEphemeral(true).queue();
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).setEphemeral(true).queue();
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
                    .build()).setEphemeral(true).queue();
            return;
        }

        Role role = Objects.requireNonNull(event.getOption("role")).getAsRole();

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (settings == null) {
                settings = new GuildSettings();
                settings.setGuildId(event.getGuild().getIdLong());
            }
            settings.setAllianceRole(role.getIdLong());
            session.persist(settings);
            session.getTransaction().commit();
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Rolle geändert")
                    .setDescription("<a:catYes:1300217972324962380> Die Rolle wurde erfolgreich zu " + role.getAsMention() + " geändert.")
                    .build()).setEphemeral(true).queue();
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

}
