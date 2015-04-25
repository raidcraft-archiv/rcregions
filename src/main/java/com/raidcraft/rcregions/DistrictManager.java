package com.raidcraft.rcregions;

import com.raidcraft.rcregions.api.District;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.config.DistrictConfig;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.tables.TRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class DistrictManager implements Component {

    private final RegionsPlugin plugin;
    private final Map<String, District> districts = new CaseInsensitiveMap<>();
    private final Pattern DISTRICT_PATTERN = Pattern.compile("^([a-zA-Z]+)(\\d+)$");

    protected DistrictManager(RegionsPlugin plugin) {

        this.plugin = plugin;
        RaidCraft.registerComponent(DistrictManager.class, this);
        loadDistricts();
    }

    public void reload() {

        districts.clear();
        loadDistricts();
    }

    private void loadDistricts() {

        DistrictConfig config = plugin.getDistrictConfig();
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            SimpleDistrict district = new SimpleDistrict(key, section);
            districts.put(key, district);
            // load all of the regions defined in the database
            List<TRegion> regions = RaidCraft.getDatabase(RegionsPlugin.class).find(TRegion.class)
                    .where().eq("district", district.getName()).findList();
            RegionManager regionManager = RaidCraft.getComponent(RegionManager.class);
            for (TRegion tRegion : regions) {
                try {
                    Region region = regionManager.createRegion(tRegion);
                    if(region != null) district.addRegion(region);
                } catch (UnknownDistrictException e) {
                    // this should never ever occur
                    e.printStackTrace();
                }
            }
            plugin.info("Loaded district: " + key);
        }
    }

    public District getDistrict(String name) throws UnknownDistrictException {

        if (districts.containsKey(name)) {
            return districts.get(name);
        }
        throw new UnknownDistrictException("District with the name " + name + " does not exist!");
    }

    public District parseDistrict(String regionName) throws UnknownDistrictException {

        regionName = StringUtils.formatName(regionName);
        Matcher matcher = DISTRICT_PATTERN.matcher(regionName);
        if (matcher.matches()) {
            String districtKey = matcher.group(1);
            for (District district : districts.values()) {
                if (district.getIdentifier().equalsIgnoreCase(districtKey)) {
                    return district;
                }
            }
        }
        throw new UnknownDistrictException("Es gibt keinen Distrikt dem die Region " + regionName + " zugeordnet werden kann.");
    }

    public Map<String, District> getDistricts() {

        return districts;
    }
}
