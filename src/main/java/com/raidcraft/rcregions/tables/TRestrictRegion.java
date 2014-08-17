package com.raidcraft.rcregions.tables;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.UUID;

/**
 * @author Dragonfire
 */
@Setter
@Getter
@Entity
@Table(name = "rcregions_restrict_regions")
public class TRestrictRegion {

    @Id
    private int id;
    private UUID player;
    private String regionName;
    private String worldName;
    private String msg;
    @Enumerated(EnumType.STRING)
    private RestrictRegionType type;

    @Transient
    private ProtectedRegion region;
}
