package com.raidcraft.rcregions.tables;

/**
 * @author Dragonfire
 */
public enum RestrictRegionType {
    EXIT("Exit"),
    ENTRY("Entry");

    private String type;

    private RestrictRegionType(String friendlyName) {

        this.type = friendlyName;
    }

    public String getType() {

        return type;
    }
}
