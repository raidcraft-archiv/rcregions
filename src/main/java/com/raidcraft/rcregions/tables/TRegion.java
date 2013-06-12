package com.raidcraft.rcregions.tables;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Silthus
 */
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
    private String owner;
    private double price;
    private boolean buyable;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDistrict() {

        return district;
    }

    public void setDistrict(String district) {

        this.district = district;
    }

    public String getOwner() {

        return owner;
    }

    public void setOwner(String owner) {

        this.owner = owner;
    }

    public double getPrice() {

        return price;
    }

    public void setPrice(double price) {

        this.price = price;
    }

    public boolean isBuyable() {

        return buyable;
    }

    public void setBuyable(boolean buyable) {

        this.buyable = buyable;
    }
}
