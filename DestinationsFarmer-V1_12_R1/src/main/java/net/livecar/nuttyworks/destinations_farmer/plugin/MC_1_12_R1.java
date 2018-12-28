package net.livecar.nuttyworks.destinations_farmer.plugin;

import net.livecar.nuttyworks.destinations_farmer.plugin.plugin.VersionInterface;
import org.bukkit.Location;
import org.bukkit.Material;

public class MC_1_12_R1 implements VersionInterface {

    @Override
    public Material convertMaterial(FarmMaterial sourceMaterial) {
        switch (sourceMaterial)
        {

            case FARMLAND:
                return Material.SOIL;
            case GRASS_BLOCK:
                return Material.GRASS;
            case DIRT:
                return Material.DIRT;
            case COCOA_BEANS:
                return Material.COCOA;
            case COCOA:
                return Material.COCOA;
            case CACTUS:
                return Material.CACTUS;
            case CACTUS_GREEN:
                return Material.CACTUS;
            case SUGAR_CANE:
                return Material.SUGAR_CANE;
            case WHEAT:
                return Material.WHEAT;
            case WHEAT_SEEDS:
                return Material.SEEDS;
            case CARROT:
                return Material.CARROT;
            case CARROTS:
                return Material.CARROT;
            case POTATO:
                return Material.POTATO;
            case POTATOES:
                return Material.POTATO;
            case BEETROOT:
                return Material.BEETROOT;
            case BEETROOTS:
                return Material.BEETROOT;
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
            case SOIL:
                return FarmMaterial.FARMLAND;
            case GRASS:
                return FarmMaterial.GRASS_BLOCK;
            case DIRT:
                return FarmMaterial.DIRT;
            case COCOA:
                return FarmMaterial.COCOA;
            case CACTUS:
                return FarmMaterial.CACTUS;
            case SUGAR_CANE:
                return FarmMaterial.SUGAR_CANE;
            case WHEAT:
                return FarmMaterial.WHEAT;
            case SEEDS:
                return FarmMaterial.WHEAT_SEEDS;
            case CARROT:
                return FarmMaterial.CARROT;
            case POTATO:
                return FarmMaterial.POTATO;
            case BEETROOT:
                return FarmMaterial.BEETROOT;
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

    @SuppressWarnings("deprecation")
    @Override
    public Boolean isFullGrown(Location blockLocation) {
        switch (blockLocation.getBlock().getType())
        {
            case BEETROOT:
                if (blockLocation.getBlock().getData() == (byte) 3)
                    return true;
                return false;
            case CACTUS:
            case SUGAR_CANE:
                // Validate the height of the cactus
                int itemHeight = 0;

                for (int y = -3; y <= 3; y++) {
                    if (blockLocation.clone().add(0, y, 0).getBlock().getType() == blockLocation.getBlock().getType()) {
                        itemHeight++;
                    }
                }
                if (itemHeight > 2)
                    return true;

                return false;
            case COCOA:
            case WHEAT:
            case CARROT:
            case POTATO:
                if (blockLocation.getBlock().getData() == (byte) 7)
                    return true;
                return false;
            case MELON:
            case PUMPKIN:
                return true;
        }
        return false;

    }

    @SuppressWarnings("deprecation")
    @Override
    public void setMinAge(Location blockLocation) {
        blockLocation.getBlock().setData((byte) 0);
    }
}
