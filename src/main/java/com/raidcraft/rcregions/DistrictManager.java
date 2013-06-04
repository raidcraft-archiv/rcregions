package com.raidcraft.rcregions;

import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 21.01.12 - 10:21
 *
 * @author Silthus
 */
public final class DistrictManager implements Component {

    private final RegionsPlugin plugin;
    private final HashMap<String, District> districts = new HashMap<>();

    protected DistrictManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(DistrictManager.class, this);
        load();
    }

    private void load() {

        for (String district : plugin.getDistrictConfig().getDistricts()) {
            districts.put(district, new District(district));
        }
    }

    public void reload() {

        districts.clear();
        load();
    }

    public District getDistrict(String name) throws UnknownDistrictException {

        if (districts.containsKey(name)) {
            return districts.get(name);
        }
        if (plugin.getDistrictConfig().getDistricts().contains(name)) {
            District district = new District(name);
            districts.put(name, district);
            return district;
        }
        throw new UnknownDistrictException("The district with the name " + name + " is not configured!");
    }

    public District getDistrictFromIdentifier(String identifier) {

        for (District district : districts.values()) {
            if (district.getIdentifier().equalsIgnoreCase(identifier)) {
                return district;
            }
        }
        return null;
    }

    public Map<String, District> getDistricts() {

        return districts;
    }
}
