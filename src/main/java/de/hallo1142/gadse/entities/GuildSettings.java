package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GuildSettings {

    @Id
    private Long guildId;

    private Long allianceCategoryId;

    private Long allianceRole;

    @OneToMany(mappedBy = AllianceChannelWhitelist_.GUILD_SETTINGS)
    private Set<AllianceChannelWhitelist> allianceChannelRoles;

}
