package de.hallo1142.gadse.commands;

import de.hallo1142.gadse.Database;
import de.hallo1142.gadse.entities.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.hibernate.Session;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class AllianceAdminCommand extends CommandExecutor{

    private final Database database;

    public AllianceAdminCommand(Database database) {
        this.database = database;
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {
        assert event.getSubcommandName() != null;
        switch (event.getSubcommandName()) {
            case "create":
                this.handleAllianceCreate(event);
                break;
            case "addadmin":
                this.handleAllianceAdminAdd(event);
                break;
            default:
                System.err.println("Unknown allianceadmin subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleAllianceAdminAdd(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String name = event.getOption("name").getAsString();
        Member member = event.getOption("user").getAsMember();

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            Alliance alliance = session.find(Alliance.class, name);
            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getId());
            if (alliance == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Allianz existiert nicht")
                        .setDescription("<a:catNo:1300217987675979917> Die angegebene Allianz existiert nicht.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            //ToDO Fix search
            AllianceMemberId allianceMemberId = new AllianceMemberId();
            allianceMemberId.setGuildId(event.getGuild().getIdLong());
            allianceMemberId.setUserId(member.getIdLong());
            AllianceMember allianceMember = session.find(AllianceMember.class, allianceMemberId);

            if (allianceMember != null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Spieler bereits in einem Bündnis")
                        .setDescription("<a:catNo:1300217987675979917> Dieser Spieler ist bereits in einem Bündnis.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            AllianceMember allianceMember1 = new AllianceMember();
            allianceMember1.setGuildId(event.getGuild().getIdLong());
            allianceMember1.setUserId(member.getIdLong());
            allianceMember1.setAdmin(true);
            allianceMember1.setAlliance(alliance);

            session.persist(allianceMember1);

            if (settings != null && settings.getAllianceRole() != null) {
                Role role = event.getGuild().getRoleById(settings.getAllianceRole());
                if (role != null) {
                    event.getGuild().addRoleToMember(member, role).queue();
                }
            }

            Role role = event.getGuild().getRoleById(alliance.getRoleId());
            if (role != null) {
                event.getGuild().addRoleToMember(member, role).queue();
            }

            TextChannel textChannel = event.getGuild().getTextChannelById(alliance.getTextChannelId());
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Neues Mitglied")
                        .setDescription(member.getAsMention() + " wurde von " + event.getMember().getAsMention() + " als Admin hinzugefügt.")
                        .build()).queue();
            }

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Mitglied hinzugefügt")
                    .setDescription("<a:catYes:1300217972324962380> Du hast " + member.getAsMention() + " zu `" + name + "` als Admin hinzugefügt.")
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

    private void handleAllianceCreate(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String name = event.getOption("name").getAsString();
        GuildSettings settings = null;
        assert event.getGuild() != null;

        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            Alliance possibleAlliance = session.find(Alliance.class, name);
            if (possibleAlliance != null) {
                session.close();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Name bereits vergeben")
                        .setDescription("<a:catNo:1300217987675979917> Eine Allianz mit diesem Namen existiert bereits.")
                        .build()).setEphemeral(true).queue();
                return;
            }
            settings = session.find(GuildSettings.class, event.getGuild().getId());

            Alliance alliance = new Alliance();
            alliance.setName(name);

            Role role = event.getGuild().createRole()
                    .setName(name)
                    .setHoisted(false)
                    .setMentionable(false)
                    .setPermissions(0L)
                    .reason("Alliance creation")
                    .complete();

            alliance.setRoleId(role.getIdLong());

            Category category = null;
            if (settings != null && settings.getAllianceCategoryId() != null) {
                category = event.getGuild().getCategoryById(settings.getAllianceCategoryId());
            }

            ChannelAction<TextChannel> channel = event.getGuild().createTextChannel(name, category).reason("Alliance creation");
            ChannelAction<VoiceChannel> vChannel = event.getGuild().createVoiceChannel(name, category).reason("Alliance creation");
            Collection<Permission> viewPermission = new ArrayList<>();
            viewPermission.add(Permission.VIEW_CHANNEL);
            channel = channel.addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, viewPermission);
            vChannel = vChannel.addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, viewPermission);
            channel = channel.addRolePermissionOverride(role.getIdLong(), viewPermission, null);
            vChannel = vChannel.addRolePermissionOverride(role.getIdLong(), viewPermission, null);

            if (settings != null && settings.getAllianceChannelRoles() != null && !settings.getAllianceChannelRoles().isEmpty()) {
                for (AllianceChannelWhitelist allianceChannelRole : settings.getAllianceChannelRoles()) {
                    Role whitelistRole = event.getGuild().getRoleById(allianceChannelRole.getRoleId());
                    if (whitelistRole == null) {
                            session.remove(allianceChannelRole);
                        continue;
                    }
                    channel = channel.addRolePermissionOverride(whitelistRole.getIdLong(), viewPermission, null);
                    vChannel = vChannel.addRolePermissionOverride(whitelistRole.getIdLong(), viewPermission, null);
                }
            }

            TextChannel textChannel = channel.complete();
            VoiceChannel voiceChannel = vChannel.complete();

            alliance.setTextChannelId(textChannel.getIdLong());
            alliance.setVoiceChannelId(voiceChannel.getIdLong());
            session.persist(alliance);

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Allianz erstellt")
                    .setDescription("<a:catYes:1300217972324962380> Die Allianz `" + name + "` wurde erstellt.")
                            .addField("Rolle", role.getAsMention(), true)
                            .addField("Text-Channel", textChannel.getAsMention(), true)
                            .addField("Voice-Channel", voiceChannel.getAsMention(), true)
                    .build()).setEphemeral(true).queue();

            textChannel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.CYAN)
                    .setTitle("Herzlich Willkommen")
                    .setDescription("Dies ist der private Channel für euren Clan **" + name + "**. Hier könnt ihr euch mit uns privat austauschen.")
                    .addField("Bündnis-Admins", "Ihr könnt einen oder mehrere Admins haben. Diese können selbstständig neue Mitglieder auf unserem Discord eurem Bündnis hinzufügen. Neue Admins können nur durch uns angelegt werden. Wendet euch hierzu bitte an einen <@&1299823398926680135>.", false)
                    .addField("Bündnis-Verwaltung", "Folgende Befehle stehen zur Verfügung:\n```\n/alliance addmember - Fügt dem Bündnis ein Mitglied hinzu.\n/alliance removemember - Entfernt ein Mitglied aus dem Bündnis.\n/alliance info - Zeigt eine Kurzinfo zum Bündnis.\n```", false)
                    .build()).complete().pin().queue();

            session.getTransaction().commit();
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
