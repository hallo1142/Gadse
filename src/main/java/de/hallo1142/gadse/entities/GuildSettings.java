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
    public Long guildId;

    public Long allianceCategoryId;

    public Long allianceRole;

    @OneToMany(mappedBy = AllianceChannelWhitelist_.GUILD_SETTINGS)
    public Set<AllianceChannelWhitelist> allianceChannelRoles;

}
