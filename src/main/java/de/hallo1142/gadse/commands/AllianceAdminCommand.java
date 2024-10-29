package de.hallo1142.gadse.commands;

import de.hallo1142.gadse.Database;
import de.hallo1142.gadse.entities.Alliance;
import de.hallo1142.gadse.entities.AllianceChannelWhitelist;
import de.hallo1142.gadse.entities.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
            default:
                System.err.println("Unknown allianceadmin subcommand: " + event.getSubcommandName());
                break;
        }
    }

    private void handleAllianceCreate(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        String name = event.getOption("name").getAsString();
        GuildSettings settings = null;
        assert event.getGuild() != null;

        try (Session session = database.getSessionFactory().openSession()) {
            Alliance alliance = session.find(Alliance.class, name);
            if (alliance != null) {
                session.close();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Name bereits vergeben")
                        .setDescription("<a:catNo:1300217987675979917> Eine Allianz mit diesem Namen existiert bereits.")
                        .build()).setEphemeral(true).queue();
                return;
            }
            settings = session.find(GuildSettings.class, event.getGuild().getId());
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Fehler")
                    .setDescription("<a:catNo:1300217987675979917> Es ist ein Fehler aufgetreten.")
                    .build()).setEphemeral(true).queue();
            e.printStackTrace();
        }

        Alliance alliance = new Alliance();
        alliance.setName(name);

        Role role = event.getGuild().createRole()
                .setName(name)
                .setHoisted(false)
                .setMentionable(false)
                .reason("Alliance creation")
                .complete();

        alliance.setRoleId(role.getIdLong());

        Category category = null;
        if (settings != null && settings.getAllianceCategoryId() != null) {
            category = event.getGuild().getCategoryById(settings.getAllianceCategoryId());
        }

        ChannelAction<TextChannel> channel = event.getGuild().createTextChannel(name, category).reason("Alliance creation");
        Collection<Permission> viewPermission = new ArrayList<>();
        viewPermission.add(Permission.VIEW_CHANNEL);
        channel = channel.addRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, viewPermission);
        channel = channel.addRolePermissionOverride(role.getIdLong(), viewPermission, null);

        if (settings != null && settings.getAllianceChannelRoles() != null && !settings.getAllianceChannelRoles().isEmpty()) {
            for (AllianceChannelWhitelist allianceChannelRole : settings.getAllianceChannelRoles()) {

            }
        }

    }

}
