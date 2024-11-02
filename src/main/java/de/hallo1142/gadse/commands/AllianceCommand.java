package de.hallo1142.gadse.commands;

import de.hallo1142.gadse.Database;
import de.hallo1142.gadse.entities.Alliance;
import de.hallo1142.gadse.entities.AllianceMember;
import de.hallo1142.gadse.entities.AllianceMemberId;
import de.hallo1142.gadse.entities.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.hibernate.Session;

import java.awt.*;
import java.util.Objects;

public class AllianceCommand extends CommandExecutor{

    private final Database database;

    public AllianceCommand(Database database) {
        this.database = database;
    }

    @Override
    public void handleCommand(SlashCommandInteractionEvent event) {
        assert event.getSubcommandName() != null;
        switch (event.getSubcommandName()) {
            case "addmember":
                this.handleAllianceAddMember(event);
                break;
            case "removemember":
                this.handleAllianceRemoveMember(event);
                break;
            default:
                System.err.println("Unknown alliance subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleAllianceRemoveMember(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            AllianceMemberId allianceMemberId = new AllianceMemberId();
            allianceMemberId.setUserId(event.getMember().getIdLong());
            allianceMemberId.setGuildId(event.getGuild().getIdLong());

            AllianceMember allianceMember = session.find(AllianceMember.class, allianceMemberId);
            if (allianceMember == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Keine Allianz gefunden")
                        .setDescription("<a:catNo:1300217987675979917> Du bist aktuell in keiner Allianz.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            if (!allianceMember.isAdmin()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Keine Berechtigung")
                        .setDescription("<a:catNo:1300217987675979917> Nur Admins einer Allianz können Mitglieder entfernen.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            Member member = event.getOption("user").getAsMember();
            AllianceMemberId newMemberId = new AllianceMemberId();
            newMemberId.setUserId(member.getIdLong());
            newMemberId.setGuildId(event.getGuild().getIdLong());

            AllianceMember possibleMember = session.find(AllianceMember.class, newMemberId);

            if (possibleMember == null || !Objects.equals(possibleMember.getAlliance().getName(), allianceMember.getAlliance().getName())) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Mitglied nicht gefunden")
                        .setDescription("<a:catNo:1300217987675979917> " + member.getAsMention() + " ist kein Mitglied dieser Allianz.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            Alliance alliance = allianceMember.getAlliance();

            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getIdLong());

            if (settings != null && settings.getAllianceRole() != null) {
                Role role = event.getGuild().getRoleById(settings.getAllianceRole());
                if (role != null) {
                    event.getGuild().removeRoleFromMember(member, role).queue();
                }
            }

            Role role = event.getGuild().getRoleById(alliance.getRoleId());
            if (role != null) {
                event.getGuild().removeRoleFromMember(member, role).queue();
            }

            session.remove(possibleMember);

            TextChannel textChannel = event.getGuild().getTextChannelById(alliance.getTextChannelId());
            if (textChannel != null) {
                textChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Mitglied entfernt")
                        .setDescription(member.getAsMention() + " wurde von " + event.getMember().getAsMention() + " entfernt.")
                        .build()).queue();
            }

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Mitglied entfernt")
                    .setDescription("<a:catYes:1300217972324962380> Du hast " + member.getAsMention() + " entfernt.")
                    .build()).setEphemeral(true).queue();

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

    private void handleAllianceAddMember(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        try (Session session = database.getSessionFactory().openSession()) {
            session.beginTransaction();
            AllianceMemberId allianceMemberId = new AllianceMemberId();
            allianceMemberId.setUserId(event.getMember().getIdLong());
            allianceMemberId.setGuildId(event.getGuild().getIdLong());

            AllianceMember allianceMember = session.find(AllianceMember.class, allianceMemberId);
            if (allianceMember == null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Keine Allianz gefunden")
                        .setDescription("<a:catNo:1300217987675979917> Du bist aktuell in keiner Allianz.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            if (!allianceMember.isAdmin()) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Keine Berechtigung")
                        .setDescription("<a:catNo:1300217987675979917> Nur Admins einer Allianz können Mitglieder hinzufügen.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            Member member = event.getOption("user").getAsMember();
            AllianceMemberId newMemberId = new AllianceMemberId();
            newMemberId.setUserId(member.getIdLong());
            newMemberId.setGuildId(event.getGuild().getIdLong());

            AllianceMember possibleMember = session.find(AllianceMember.class, newMemberId);

            if (possibleMember != null) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Bereits in Allianz")
                        .setDescription("<a:catNo:1300217987675979917> " + member.getAsMention() + " ist bereits in einer anderen Allianz.")
                        .build()).setEphemeral(true).queue();
                return;
            }

            Alliance alliance = allianceMember.getAlliance();
            AllianceMember newMember = new AllianceMember();
            newMember.setAlliance(alliance);
            newMember.setUserId(member.getIdLong());
            newMember.setGuildId(event.getGuild().getIdLong());
            newMember.setAdmin(false);

            session.persist(newMember);

            GuildSettings settings = session.find(GuildSettings.class, event.getGuild().getIdLong());

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
                        .setDescription(member.getAsMention() + " wurde von " + event.getMember().getAsMention() + " als Mitglied hinzugefügt.")
                        .build()).queue();
            }

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Mitglied hinzugefügt")
                    .setDescription("<a:catYes:1300217972324962380> Du hast " + member.getAsMention() + " als Mitglied hinzugefügt.")
                    .build()).setEphemeral(true).queue();

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
