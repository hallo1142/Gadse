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
public class Alliance {

    @Id
    String name;

    Long channelId;

    Long roleId;

    @OneToMany(mappedBy = AllianceMember_.ALLIANCE)
    Set<AllianceMember> allianceMembers;

}
