package com.raidcraft.rcregions.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

/**
 * @author Silthus
 */
public interface Region {

    int getId();

    String getName();

    District getDistrict();

    boolean hasOwner();

    UUID getOwnerId();

    Set<String> getMembers();

    boolean isInsideRegion(Location location);

    void setBuyable(boolean buyable);

    boolean isBuyable();

    double getPrice();

    void setPrice(double price);

    void claim(OfflinePlayer player);

    void drop(OfflinePlayer player);

    void save();

    void updateOwner();
}
