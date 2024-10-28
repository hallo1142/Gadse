package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AllianceChannelWhitelist {

    @Id
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    private GuildSettings guildSettings;

}

