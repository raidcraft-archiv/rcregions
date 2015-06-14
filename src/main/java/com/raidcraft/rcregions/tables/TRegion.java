package com.raidcraft.rcregions.tables;

import com.avaje.ebean.validation.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * @author Silthus
 */
@Getter
@Setter
@Entity
@Table(name = "rcregions_regions")
public class TRegion {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String name;
    @NotNull
    private String district;
    private String world;
    private String owner;
    private UUID ownerId;
    private double price;
    private boolean buyable;
}
