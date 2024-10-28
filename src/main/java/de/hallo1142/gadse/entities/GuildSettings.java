package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class GuildSettings {

    public GuildSettings() {
    }

    @Id
    Long guildId;

    Long allianceCategoryId;

    Long allianceRole;

    @OneToMany(mappedBy = AllianceChannelWhitelist_.GUILD_SETTINGS)
    Set<AllianceChannelWhitelist> allianceChannelRoles;

}
