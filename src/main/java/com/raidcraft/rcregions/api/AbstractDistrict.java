package com.raidcraft.rcregions.api;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
public abstract class AbstractDistrict implements District {

    private final String name;
    private final String friendlyName;
    private final String identifier;
    private final String description;
    private final Set<String> applicableWorlds = new HashSet<>();
    private final double pricePerBlock;

    public AbstractDistrict(String name, ConfigurationSection config) {

        this.name = name;
        this.friendlyName = config.getString("name", name);
        this.identifier = config.getString("identifier", name.substring(0, 3));
        this.pricePerBlock = config.getDouble("price-per-block", 0.0);
        this.description = config.getString("description");
        this.applicableWorlds.addAll(config.getStringList("worlds"));
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getFriendlyName() {

        return friendlyName;
    }

    @Override
    public String getIdentifier() {

        return identifier;
    }

    @Override
    public String getDescription() {

        return description;
    }

    @Override
    public Set<String> getApplicableWorlds() {

        return applicableWorlds;
    }

    @Override
    public double getPricePerBlock() {

        return pricePerBlock;
    }
}
