package com.raidcraft.rcregions.api;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.tables.TRegion;
import com.raidcraft.rcregions.util.RegionUtil;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
    private boolean buyable;
    private double price;

    public AbstractRegion(int id, ProtectedRegion region, District district) {

        this.id = id;
        this.name = region.getId();
        this.district = district;
        this.region = region;
        this.price = RegionUtil.getPrice(region, district.getPricePerBlock());
        this.buyable = district.isDefaultBuyable();

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
    public void claim(OfflinePlayer player) {

        // first lets clear out all owners and members
        region.setMembers(new DefaultDomain());
        // and set the new owner in a new default domain
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(player.getName());
        region.setOwners(owners);
        // update our reference
        owner = player.getName();
        buyable = false;
        RegionUtil.setRegionClaimedFlags(region);
        save();
    }

    @Override
    public void drop() {

        region.setMembers(new DefaultDomain());
        region.setOwners(new DefaultDomain());
        owner = null;
        buyable = true;
        RegionUtil.setRegionDroppedFlags(region);
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

    @Override
    public void updateOwner() {

        TRegion tRegion = RaidCraft.getDatabase(RegionsPlugin.class).find(TRegion.class).where().eq("name", name).findUnique();
        if(tRegion == null || tRegion.getOwner() == null || tRegion.getOwner().isEmpty()) return;
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(Bukkit.getOfflinePlayer(tRegion.getOwner()).getName());
        region.setOwners(owners);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof AbstractRegion)) return false;

        AbstractRegion that = (AbstractRegion) o;

        return id == that.id && name.equals(that.name);

    }

    @Override
    public int hashCode() {

        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return name;
    }
}
