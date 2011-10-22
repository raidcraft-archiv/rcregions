/*
 * Copyright (C) 2011 RaidCraft <http://www.raid-craft.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silthus.rcregions;

import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.rcregions.config.RegionsConfig;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * 03.10.11 - 09:25
 * @author Silthus
 */
public class Region {

    /**
     * This is UID of the region and equal to
     * the ID of the WorldGuard region.
     */
    private final String id;
    /**
     * This is a refrence to the WorldGuard region
     */
    private final ProtectedRegion region;
    /**
     * This contains all owners of the region.
     * Owners are the only ones who get put up
     * the region for sale.
     */
    private Set<String> owners = new HashSet<String>();
    /**
     * This is the mainOwner and the one who bought the region
     */
    private String mainOwner;
    /**
     * This is the current price of the region.
     */
    private double price;
    /**
     * This is the minimum price for the region
     * either defined through the config or through
     * an extra command.
     */
    private double minPrice;
    /**
     * This bool defines if the region is for Sale.
     */
    private boolean forSale;

    /**
     * Creates a new Region and sets its defaults.
     * @param id region name
     */
    public Region(String id) throws UnknownRegionException {
        this.id = id;
        this.region = RCRegionManager.getWorldGuardRegion(id);
        setDefaults();
    }

    /**
     * Sets the defaults of the region to:
     * minPrice = ConfigOption
     * price = minPrice;
     * forSale = false;
     */
    private void setDefaults() {
        this.minPrice = RegionsConfig.getMinPrice();
        this.price = minPrice;
        this.forSale = false;
        // TODO: load owners from worldguard
        setOwners();
    }

    /**
     * Clears the region from all owners and
     * WorldGuard members
     */
    public void clearOwners() {
        // clear our owners set
        owners.clear();
        // clear the WorldGuard owner set
        region.setOwners(new DefaultDomain());
        // clear the WorldGuard member set
        region.setMembers(new DefaultDomain());
    }

    /**
     * Gets the name of the region
     * @return region name
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a set of all owners for the region
     * @return owners of the region
     */
    public Set<String> getOwners() {
        return owners;
    }

    /**
     * Gets the main owner of the region
     * @return mainOwner
     */
    public String getMainOwner() {
        return mainOwner;
    }

    /**
     * Gets the current price of the region
     * @return price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the minimum price of the region
     * @return minPrice
     */
    public double getMinPrice() {
        return minPrice;
    }

    /**
     * Gets if the region is for sale
     * @return region for sale
     */
    public boolean isForSale() {
        return forSale;
    }

    /**
     * Sets the price for the region
     * @param price to set
     */
    public void setPrice(double price) {
        if (price < getMinPrice()) {
            this.price = getMinPrice();
        } else {
            this.price = price;
        }
    }

    /**
     * Sets the minimum price for the region
     * @param minPrice to set
     */
    public void setMinPrice(double minPrice) {
        if (getPrice() < minPrice) {
            this.minPrice = minPrice;
            setPrice(minPrice);
        } else {
            this.minPrice = minPrice;
        }
    }

    /**
     * Sets the region for sale
     * @param forSale for sale
     */
    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    /**
     * Adds a owner to the region and WorldGuard
     * @param owners to add
     */
    public void addOwner(String... owners) {
        for (String s : owners) {
            this.owners.add(s);
            // add the owner to WorldGuard
            region.getOwners().addPlayer(s);
        }
    }

    /**
     * Sets the owners from the WorldGuard config
     * and also sets the first WorldGuard owner to
     * the MainOwner
     */
    private void setOwners() {
        boolean check = true;
        for (String player : region.getOwners().getPlayers()) {
            if (check) {
                setMainOwner(player);
                check = false;
            }
            addOwner(player);
        }
    }

    /**
     * Sets a mainOwner for the region
     * @param owner mainOwner to set
     */
    public void setMainOwner(String owner) {
        addOwner(owner);
        this.mainOwner = owner;
    }

    /**
     * Checks if the given player is the MainOwner of the region
     * @param player to check
     * @return true if player is main owner
     */
    public boolean isMainOwner(String player) {
        return player.equalsIgnoreCase(getMainOwner());
    }

    /**
     * Checks if the given player is a owner of the region
     * @param player to check
     * @return true if player is owner
     */
    public boolean isOwner(String player) {
        return owners.contains(player);
    }

    /**
     * Gives detailed information about the region
     * @return detailed information
     */
    public String[] toDetailedString() {
        return new String[]{"Region: " + RCMessaging.yellow(getId()),
                "Price: " + RCMessaging.yellow(getPrice() + ""),
                "Owner: " + RCMessaging.yellow(getMainOwner())};
    }

    /**
     * Gets the region String
     * @return region string
     */
    public String toString() {
        return "Region " + getId() + " can be bought for " + getPrice() + " from " + getMainOwner();
    }
}
