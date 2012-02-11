package com.raidcraft.rcregions;

import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class CustomFlag {
    
    public static final BooleanFlag WARNED = new BooleanFlag("warned");

    static {
        // TODO: wait until worldguard is fixed
        // DefaultFlag.addCustomFlag(WARNED);
    }
}
