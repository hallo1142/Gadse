package de.hallo1142.gadse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@IdClass(AllianceMemberId.class)
public class AllianceMember {

    @Id
    private Long userId;

    @Id
    private Long guildId;

    private boolean isAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    private Alliance alliance;

}
