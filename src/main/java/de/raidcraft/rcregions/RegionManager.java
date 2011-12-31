package de.raidcraft.rcregions;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.rcregions.config.MainConfig;
import de.raidcraft.rcregions.exceptions.UnknownRegionException;

import java.util.HashMap;

/**
 * 17.12.11 - 11:49
 *
 * @author Silthus
 */
public class RegionManager {

    private static RegionManager self;
    private final HashMap<String, Region> _regions;

    private RegionManager() {
        _regions = new HashMap<String, Region>();
    }

    public static RegionManager get() {
        if (self == null) {
            self = new RegionManager();
        }
        return self;
    }

    public Region getRegion(String name) throws UnknownRegionException {
        if (_regions.containsKey(name)) {
            return _regions.get(name);
        } else {
            ProtectedRegion region = WorldGuardManager.getRegion(name);
            if (region != null && isAllowedRegion(region)) {
                _regions.put(name, new Region(region));
                return getRegion(name);
            }
        }
        throw new UnknownRegionException("Die Region " + name + " existiert nicht!");
    }

    public boolean isAllowedRegion(ProtectedRegion region) {
        return !(MainConfig.getIgnoredRegions().contains(region.getId()));
    }
}
