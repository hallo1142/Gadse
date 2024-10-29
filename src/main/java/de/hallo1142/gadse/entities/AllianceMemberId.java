package de.hallo1142.gadse.entities;

import lombok.Data;

import java.io.Serializable;

@Data
public class AllianceMemberId implements Serializable {

    private Long userId;
    private Long guildId;

}
