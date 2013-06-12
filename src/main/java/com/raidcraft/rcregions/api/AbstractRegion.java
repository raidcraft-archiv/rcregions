package com.raidcraft.rcregions.api;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public abstract class AbstractRegion implements Region {

    private final int id;
    private final String name;
    private final District district;
    private final ProtectedRegion region;
    private String owner = null;
    private boolean buyable = false;
    private double price;

    public AbstractRegion(int id, ProtectedRegion region, District district) {

        this.id = id;
        this.name = region.getId();
        this.district = district;
        this.region = region;
        this.price = region.getFlag(DefaultFlag.PRICE);

        List<String> players = new ArrayList<>(region.getOwners().getPlayers());
        if (players.size() == 1) {
            owner = players.get(0);
        } else if (!players.isEmpty()) {
            for (String player : players) {
                if (owner == null) {
                    owner = player;
                } else {
                    // switch over all other "owners" to members
                    // a region can only have one owner at a time
                    region.getOwners().removePlayer(player);
                    region.getMembers().addPlayer(player);
                }
            }
        }
    }

    @Override
    public void buy(Player player) {

        // first lets clear out all owners and members
        region.setMembers(new DefaultDomain());
        // and set the new owner in a new default domain
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(player.getName());
        region.setOwners(owners);
        // update our reference
        owner = player.getName();
        save();
    }

    @Override
    public int getId() {

        return id;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public District getDistrict() {

        return district;
    }

    @Override
    public boolean hasOwner() {

        return owner != null;
    }

    @Override
    public String getOwner() {

        return owner;
    }

    @Override
    public Set<String> getMembers() {

        return region.getMembers().getPlayers();
    }

    @Override
    public void setBuyable(boolean buyable) {

        this.buyable = buyable;
        save();
    }

    @Override
    public boolean isBuyable() {

        return buyable;
    }

    @Override
    public double getPrice() {

        return price;
    }

    @Override
    public void setPrice(double price) {

        this.price = price;
        save();
    }
}
