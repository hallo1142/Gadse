package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AllianceChannelWhitelist {

    public AllianceChannelWhitelist() {
    }

    @Id
    Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    GuildSettings guildSettings;

}

