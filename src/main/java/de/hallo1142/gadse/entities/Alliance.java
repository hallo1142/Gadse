package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Alliance {

    @Id
    String name;

    Long channelId;

    Long roleId;

    @OneToMany(mappedBy = AllianceMember_.ALLIANCE)
    Set<AllianceMember> allianceMembers;

}
