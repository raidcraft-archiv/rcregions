package com.raidcraft.rcregions.api;

import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

/**
 * @author Silthus
 */
public interface Region {

    public int getId();

    public String getName();

    public District getDistrict();

    public boolean hasOwner();

    public UUID getOwnerId();

    public Set<String> getMembers();

    public void setBuyable(boolean buyable);

    public boolean isBuyable();

    public double getPrice();

    public void setPrice(double price);

    public void claim(OfflinePlayer player);

    public void drop(OfflinePlayer player);

    public void save();

    public void updateOwner();
}
