package net.livecar.nuttyworks.destinations_farmer.plugin;

import net.livecar.nuttyworks.destinations_farmer.plugin.plugin.VersionInterface;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;

public class MC_1_13_R2 implements VersionInterface {

    @Override
    public Material convertMaterial(FarmMaterial sourceMaterial) {
        switch (sourceMaterial)
        {

            case FARMLAND:
                return Material.FARMLAND;
            case GRASS_BLOCK:
                return Material.GRASS_BLOCK;
            case DIRT:
                return Material.DIRT;
            case COCOA_BEANS:
                return Material.COCOA_BEANS;
            case COCOA:
                return Material.COCOA;
            case CACTUS:
                return Material.CACTUS;
            case CACTUS_GREEN:
                return Material.CACTUS_GREEN;
            case SUGAR_CANE:
                return Material.SUGAR_CANE;
            case WHEAT:
                return Material.WHEAT;
            case WHEAT_SEEDS:
                return Material.WHEAT_SEEDS;
            case CARROT:
                return Material.CARROT;
            case CARROTS:
                return Material.CARROTS;
            case POTATO:
                return Material.POTATO;
            case POTATOES:
                return Material.POTATOES;
            case BEETROOT:
                return Material.BEETROOT;
            case BEETROOTS:
                return Material.BEETROOTS;
            case BEETROOT_SEEDS:
                return Material.BEETROOT_SEEDS;
            case MELON:
                return Material.MELON;
            case PUMPKIN:
                return Material.PUMPKIN;
            case AIR:
                return Material.AIR;
        }
        return null;
    }

    @Override
    public FarmMaterial convertMaterial(Material sourceMaterial) {
        switch (sourceMaterial)
        {
            case FARMLAND:
                return FarmMaterial.FARMLAND;
            case GRASS_BLOCK:
                return FarmMaterial.GRASS_BLOCK;
            case DIRT:
                return FarmMaterial.DIRT;
            case COCOA_BEANS:
                return FarmMaterial.COCOA_BEANS;
            case CACTUS:
                return FarmMaterial.CACTUS;
            case CACTUS_GREEN:
                return FarmMaterial.CACTUS_GREEN;
            case SUGAR_CANE:
                return FarmMaterial.SUGAR_CANE;
            case WHEAT:
                return FarmMaterial.WHEAT;
            case WHEAT_SEEDS:
                return FarmMaterial.WHEAT_SEEDS;
            case CARROT:
                return FarmMaterial.CARROT;
            case CARROTS:
                return FarmMaterial.CARROTS;
            case POTATO:
                return FarmMaterial.POTATO;
            case POTATOES:
                return FarmMaterial.POTATOES;
            case BEETROOT:
                return FarmMaterial.BEETROOT;
            case BEETROOTS:
                return FarmMaterial.BEETROOTS;
            case BEETROOT_SEEDS:
                return FarmMaterial.BEETROOT_SEEDS;
            case MELON:
                return FarmMaterial.MELON;
            case PUMPKIN:
                return FarmMaterial.PUMPKIN;
            case AIR:
                return FarmMaterial.AIR;
        }
        return null;
    }

    @Override
    public Boolean isFullGrown(Location blockLocation) {
        Ageable blockAge = null;
        if (blockLocation.getBlock().getBlockData() instanceof Ageable) {
            blockAge = (Ageable) blockLocation.getBlock().getBlockData();
            if (blockAge.getAge() == blockAge.getMaximumAge())
                return true;
        }
        return false;

    }

    @Override
    public void setMinAge(Location blockLocation) {
        if (blockLocation.getBlock().getBlockData() instanceof Ageable) {
            Ageable blockAge = (Ageable) blockLocation.getBlock().getBlockData();
            blockAge.setAge(0);
            blockLocation.getBlock().setBlockData(blockAge);
        }
    }
}
