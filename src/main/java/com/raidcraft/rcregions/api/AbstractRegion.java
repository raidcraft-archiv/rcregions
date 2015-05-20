package com.raidcraft.rcregions.api;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.api.events.RCClaimRegionEvent;
import com.raidcraft.rcregions.api.events.RCDropRegionEvent;
import com.raidcraft.rcregions.tables.TRegion;
import com.raidcraft.rcregions.util.RegionUtil;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.util.UUIDUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Silthus
 */
public abstract class AbstractRegion implements Region {

    private final int id;
    private final String name;
    private final District district;
    private final ProtectedRegion region;
    private UUID owner = null;
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
        // TODO: UUID rework - WorldGuard
        if (players.size() == 1) {
            owner = UUIDUtil.convertPlayer(players.get(0));
        } else if (!players.isEmpty()) {
            for (String player : players) {
                if (owner == null) {
                    owner = UUIDUtil.convertPlayer(player);
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

        RCClaimRegionEvent event = new RCClaimRegionEvent(player, this);
        RaidCraft.callEvent(event);
        if (event.isCancelled()) return;
        // first lets clear out all owners and members
        region.setMembers(new DefaultDomain());
        // and set the new owner in a new default domain
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(player.getName());
        region.setOwners(owners);
        // update our reference
        owner = player.getUniqueId();
        buyable = false;
        RegionUtil.setRegionClaimedFlags(region);
        save();
    }

    @Override
    public void drop(OfflinePlayer player) {

        RCDropRegionEvent event = new RCDropRegionEvent(player, this);
        RaidCraft.callEvent(event);
        if (event.isCancelled()) return;
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
    public UUID getOwnerId() {

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
    public boolean isInsideRegion(Location location) {

        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void updateOwner() {

        TRegion tRegion = RaidCraft.getDatabase(RegionsPlugin.class).find(TRegion.class).where().eq("name", name).findUnique();
        if(tRegion == null || tRegion.getOwnerId() == null) return;
        // TODO: UUID rework - WorldGuard
        String playerName = UUIDUtil.getNameFromUUID(tRegion.getOwnerId());
        if(playerName == null) {
            return;
        }
        if(WorldGuardManager.isOwner(playerName, region)) return;
        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(playerName);
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
