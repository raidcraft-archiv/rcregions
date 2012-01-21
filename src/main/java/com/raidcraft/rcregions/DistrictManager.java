package com.raidcraft.rcregions;

import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;

import java.util.HashMap;

/**
 * 21.01.12 - 10:21
 *
 * @author Silthus
 */
public final class DistrictManager {

    private static DistrictManager _self;
    private final HashMap<String, District> _districts;

    private DistrictManager() {
        _districts = new HashMap<String, District>();
        load();
    }
    
    private void load() {
        for (String district : MainConfig.getDistricts()) {
            _districts.put(district, new District(district));
        }
    }
    
    public synchronized static void reload() {
        _self = null;
        _self = new DistrictManager();
    }
    
    public static void init() {
        get();
    }

    public static DistrictManager get() {
        if (_self == null) {
            _self = new DistrictManager();
        }
        return _self;
    }
    
    public District getDistrict(String name) throws UnknownDistrictException {
        if (_districts.containsKey(name)) {
            return _districts.get(name);
        }
        if (MainConfig.getDistricts().contains(name)) {
            District district = new District(name);
            _districts.put(name, district);
            return district;
        }
        throw new UnknownDistrictException("The district with the name " + name + " is not configured!");
    }
    
    public District getDistrictFromIdentifier(String identifier) {
        for (District district : _districts.values()) {
            if (district.getIdentifier().equalsIgnoreCase(identifier)) {
                return district;
            }
        }
        return null;
    }
}
