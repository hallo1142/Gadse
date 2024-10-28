package de.hallo1142.gadse.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AllianceMember {

    public AllianceMember() {
    }

    @Id
    Long userId;

    boolean isAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    Alliance alliance;

}
