package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.District;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.api.events.RCBuyRegionEvent;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.tables.TRegion;
import com.sk89q.util.StringUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.economy.BalanceSource;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Silthus
 */
public class RegionManager implements Component {

    private final RegionsPlugin plugin;
    private final Map<String, Region> regions = new CaseInsensitiveMap<>();

    protected RegionManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(RegionManager.class, this);
    }

    public void reload() {

        regions.clear();
        loadRegions();
    }

    private void loadRegions() {

        // lets get all regions from the database first
        for (TRegion entry : plugin.getDatabase().find(TRegion.class).findList()) {
            if (!regions.containsKey(StringUtils.formatName(entry.getName()))) {
                try {
                    createRegion(entry);
                } catch (UnknownDistrictException e) {
                    plugin.getLogger().warning(e.getMessage());
                }
            }
        }
        WorldGuardManager.save();
    }

    public Region createRegion(TRegion region) throws UnknownDistrictException {

        if (regions.containsKey(region.getName())) {
            return regions.get(region.getName());
        }

        // check if worldguard region still exists
        if (WorldGuardManager.getRegion(region.getName()) == null) {
            RaidCraft.LOGGER.warning("[RCRegion] WorldGuard region doesn't exist for plot '" + region.getName() + "'!");
            return null;
        }

        // create a new region in the cache
        SimpleRegion simpleRegion = new SimpleRegion(region);
        simpleRegion.updateOwner();
        regions.put(simpleRegion.getName(), simpleRegion);
        return simpleRegion;
    }

    public Region getRegion(String name) throws UnknownRegionException, UnknownDistrictException {

        name = StringUtils.formatName(name);
        if (!regions.containsKey(name)) {
            // try to find it in the database
            TRegion tRegion = plugin.getDatabase().find(TRegion.class).where().eq("name", name).findUnique();
            if (tRegion != null) {
                return createRegion(tRegion);
            }
            // lets check if the region is in a valid district and exists in worldguard
            if (WorldGuardManager.getRegion(name) != null) {
                District district = plugin.getDistrictManager().parseDistrict(name);
                // it seems so, or else an exception would be thrown
                // so lets create a new database entry for the region
                tRegion = new TRegion();
                tRegion.setName(name);
                tRegion.setDistrict(district.getName());
                tRegion.setBuyable(false);
                plugin.getDatabase().save(tRegion);
                Region region = createRegion(tRegion);
                tRegion.setOwnerId(region.getOwnerId());
                tRegion.setPrice(region.getPrice());
                plugin.getDatabase().update(tRegion);
                return region;
            }
            throw new UnknownRegionException("Es gibt keine Region mit dem Namen: " + name);
        } else {
            return regions.get(name);
        }
    }

    public Region getRegion(Location location) throws UnknownRegionException {

        List<Region> regionList = new ArrayList<>();
        ApplicableRegionSet regions = WorldGuardManager.getLocalRegions(location);
        for (ProtectedRegion region : regions) {
            try {
                regionList.add(getRegion(region.getId()));
            } catch (UnknownDistrictException ignored) {
            }
        }
        if (regionList.isEmpty()) {
            throw new UnknownRegionException("Es gibt keine Region an dieser Stelle.");
        }
        if (regionList.size() > 1) {
            throw new UnknownRegionException("Es gibt mehrere Regionen an dieser Stelle: " + StringUtil.joinString(regionList, ", ", 0));
        }
        return regionList.get(0);
    }

    public void buyRegion(Player player, Region region) {

        if (region.getPrice() > 0) {
            RCBuyRegionEvent event = new RCBuyRegionEvent(player, region, region.getPrice());
            RaidCraft.callEvent(event);
            if (event.isCancelled()) return;
            // lets substract the cost
            RaidCraft.getEconomy().substract(player.getUniqueId(), event.getPrice(), BalanceSource.BUY_REGION, region.getName());
            if (region.getOwnerId() != null) {
                // give the old owner the money substracted the taxes
                double amount = event.getPrice() - event.getPrice() * plugin.getMainConfig().taxes;
                RaidCraft.getEconomy().add(region.getOwnerId(),
                        amount,
                        BalanceSource.SELL_REGION, region.getName()
                );
                Player oldOwner = Bukkit.getPlayer(region.getOwnerId());
                if (oldOwner != null) {
                    oldOwner.sendMessage(ChatColor.GREEN + "Deine Region " + ChatColor.AQUA + region.getName() + ChatColor.GREEN
                            + " wurde erfolreich für " + RaidCraft.getEconomy().getFormattedAmount(amount) + " an " + player.getName() + " verkauft.");
                }
            }
        }
        region.claim(player);
        // lets check for command we need to execute
        ConfigurationSection section = plugin.getDistrictConfig().getConfigurationSection(region.getDistrict().getName());
        if (section != null && section.isSet("command-on-claim")) {
            String cmd = section.getString("command-on-claim").replace("%player%", player.getName());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
        player.sendMessage(ChatColor.GREEN + "Du hast das Grundstück "
                + ChatColor.AQUA + region.getName() + ChatColor.GREEN + " erfolgreich erworben.");
    }

    public void dropRegion(Player player, Region region) {

        region.drop(player);
        // lets check for command we need to execute
        player.sendMessage(ChatColor.RED + "Du hast das Grundstück "
                + ChatColor.AQUA + region.getName() + ChatColor.RED + " erfolgreich freigegeben.");
    }

    public void toggleRegionBuyableState(Player player, Region region) {

        region.setBuyable(!region.isBuyable());
        if (region.isBuyable()) {
            player.sendMessage(ChatColor.GREEN + "Dein Grundstück steht nun zum Verkauf an andere Spieler offen.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Dein Grundstück kann nun nicht mehr von anderen Spielern gekauft werden.");
        }
    }

    public int getPlayerRegionCount(Player player, District district) {

        return plugin.getDatabase().find(TRegion.class)
                .where()
                .eq("owner_id", player.getUniqueId())
                .eq("district", district.getName()).findList().size();
    }

    public List<Region> getPlayerRegions(UUID player) {

        List<Region> regions = new ArrayList<>();
        List<TRegion> result = plugin.getDatabase().find(TRegion.class)
                .where().eq("owner_id", player.toString()).findList();
        for (TRegion region : result) {
            try {
                regions.add(createRegion(region));
            } catch (UnknownDistrictException e) {
                plugin.getLogger().warning(e.getMessage());
            }
        }
        return regions;
    }

    public List<Region> getPlayerRegions(UUID player, District district) {

        List<Region> regions = new ArrayList<>();
        for (Region region : getPlayerRegions(player)) {
            if (region.getDistrict().equals(district)) {
                regions.add(region);
            }
        }
        return regions;
    }
}