package net.livecar.nuttyworks.destinations_farmer.plugin.plugin;

import org.bukkit.Location;
import org.bukkit.Material;

public interface VersionInterface {
    public Material convertMaterial(FarmMaterial sourceMaterial);
    public FarmMaterial convertMaterial(Material sourceMaterial);
    public Boolean isFullGrown(Location blockLocation);
    public void setMinAge(Location blockLocation);

    public enum FarmMaterial
    {
        FARMLAND,
        GRASS_BLOCK,
        DIRT,
        COCOA_BEANS,
        COCOA,
        CACTUS,
        CACTUS_GREEN,
        SUGAR_CANE,
        WHEAT,
        WHEAT_SEEDS,
        CARROT,
        CARROTS,
        POTATO,
        POTATOES,
        BEETROOT,
        BEETROOTS,
        BEETROOT_SEEDS,
        MELON,
        PUMPKIN,
        AIR,
    }
}
