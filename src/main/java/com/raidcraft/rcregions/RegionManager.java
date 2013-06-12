package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.District;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.tables.TRegion;
import com.sk89q.util.StringUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class RegionManager implements Component {

    private final RegionsPlugin plugin;
    private final Map<String, Region> regions = new HashMap<>();

    protected RegionManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(RegionManager.class, this);
        loadRegions();
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
    }

    public Region createRegion(TRegion region) throws UnknownDistrictException {

        String name = StringUtils.formatName(region.getName().toLowerCase());
        if (regions.containsKey(name)) {
            return regions.get(name);
        }
        // create a new region in the cache
        SimpleRegion simpleRegion = new SimpleRegion(region);
        regions.put(name, simpleRegion);
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
            // lets check if the region is in a valid district
            District district = plugin.getDistrictManager().parseDistrict(name);
            // it seems so, or else an exception would be thrown
            // so lets create a new database entry for the region
            tRegion = new TRegion();
            tRegion.setName(name);
            tRegion.setDistrict(district.getName());
            tRegion.setBuyable(false);
            plugin.getDatabase().save(tRegion);
            Region region = createRegion(tRegion);
            tRegion.setOwner(region.getOwner());
            tRegion.setPrice(region.getPrice());
            plugin.getDatabase().update(tRegion);
            return region;
        }
        throw new UnknownRegionException("Es gibt keine Region mit dem Namen: " + name);
    }

    public Region getRegion(Location location) throws UnknownRegionException {

        List<Region> regionList = new ArrayList<>();
        ApplicableRegionSet regions = WorldGuardManager.getLocalRegions(location);
        for (ProtectedRegion region : regions) {
            if (this.regions.containsKey(region.getId())) {
                try {
                    regionList.add(getRegion(region.getId()));
                } catch (UnknownRegionException | UnknownDistrictException e) {
                    plugin.getLogger().warning(e.getMessage());
                }
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

        region.buy(player);
        // lets check for command we need to execute
        ConfigurationSection section = plugin.getDistrictConfig().getConfigurationSection(region.getDistrict().getName());
        if (section != null && section.isSet("command-on-claim")) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), section.getString("command-on-claim"));
        }
        player.sendMessage(ChatColor.GREEN + "Du hast das Grundstück "
                + ChatColor.AQUA + region.getName() + ChatColor.GREEN + " erfolgreich erworben.");
    }

    public void toggleRegionBuyableState(Player player, Region region) {

        region.setBuyable(!region.isBuyable());
        if (region.isBuyable()) {
            player.sendMessage(ChatColor.GREEN + "Dein Grundstück steht nun zum Verkauf an andere Spieler offen.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Dein Grundstück kann nun nicht mehr von anderen Spielern gekauft werden.");
        }
    }
}
