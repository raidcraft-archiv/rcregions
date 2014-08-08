package com.raidcraft.rcregions.tables;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
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

    @Transient
    private ProtectedRegion region;
}
