package net.livecar.nuttyworks.destinations_farmer.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.livecar.nuttyworks.destinations_farmer.Destinations_Farmer;
import net.livecar.nuttyworks.destinations_farmer.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting;
import net.livecar.nuttyworks.destinations_farmer.storage.NPC_Setting.CurrentAction;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;

public class MC_1_13_R1 implements VersionInterface {

    public void pluginTick() {

        Iterator<Entry<Integer, Location_Setting>> farmingIterator = Destinations_Farmer.Instance.monitoredNPCs.entrySet().iterator();
        while (farmingIterator.hasNext()) {

            Entry<Integer, Location_Setting> npcFarmer = farmingIterator.next();

            if (npcFarmer.getValue().locationID == null)
                continue;

            NPC npc = CitizensAPI.getNPCRegistry().getById(npcFarmer.getKey());
            if (npc == null)
                continue;

            if (!npc.isSpawned())
                continue;

            if (npc.getNavigator().isNavigating())
                return;

            NPC_Setting farmSet = Destinations_Farmer.Instance.npcSettings.get(npcFarmer.getKey());
            Equipment npcEquip = npc.getTrait(Equipment.class);

            if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Current Action|" + farmSet.currentAction.toString());

            switch (farmSet.currentAction) {
            case ABORTING:
                if (farmSet.destinationsTrait != null && farmSet.destinationsTrait.currentLocation != null && farmSet.destinationsTrait.currentLocation.destination.distanceSquared(npc.getEntity().getLocation()) > 3) {
                    if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Not at destination|"
                                + farmSet.destinationsTrait.currentLocation.destination + " Dist:" + npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D).distanceSquared(farmSet.destinationsTrait.currentLocation.destination));

                    DestinationsPlugin.Instance.getPathClass.addToQueue(npc, farmSet.destinationsTrait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), farmSet.destinationsTrait.currentLocation.destination,
                            Destinations_Farmer.Instance.getDestinationsPlugin.maxDistance, new ArrayList<Material>(), 0, true, true, true, "DestinationsFarmer");
                } else {
                    if (Destinations_Farmer.Instance.monitoredNPCs.containsKey(npc.getId())) {
                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|ABORTED|Removing monitors");

                        farmSet.destinationsTrait.unsetMonitoringPlugin();
                        Destinations_Farmer.Instance.npcSettings.get(npc.getId()).currentAction = CurrentAction.IDLE;
                        Destinations_Farmer.Instance.npcSettings.get(npc.getId()).currentDestination = null;
                        Destinations_Farmer.Instance.monitoredNPCs.remove(npc.getId());
                        continue;
                    }
                }
                break;
            case FARMING:

                break;
            case TRAVERSING:
                if (farmSet.currentDestination != null && !npc.getNavigator().isNavigating() && farmSet.currentDestination.distanceSquared(npc.getEntity().getLocation()) < 4) {
                    Location destBlock = farmSet.currentDestination.clone();

                    // Close enough, farm the block
                    switch (farmSet.currentDestination.getBlock().getType()) {
                    case GRASS:
                    case DIRT:
                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination
                                    .getBlock().getType().toString() + " BD: " + destBlock.getBlock().getBlockData().getAsString());

                        boolean hasHoe = false;
                        if (this.handMatches(npc, EquipmentSlot.HAND, "_HOE"))
                            hasHoe = true;
                        if (EquipmentSlot.valueOf("OFF_HAND") != null) {
                            if (this.handMatches(npc, EquipmentSlot.OFF_HAND, "_HOE"))
                                hasHoe = true;
                        }

                        if (hasHoe) {
                            net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                            net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                            PlaySound(farmSet.currentDestination, soundType.TILL_DIRT);
                            addToInventory(npc, destBlock.clone().getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));

                            farmSet.lastAction = new Date();
                            destBlock.getBlock().setType(Material.FARMLAND);
                            farmSet.currentDestination = null;
                            farmSet.currentAction = CurrentAction.IDLE;
                            continue;
                        }
                        break;
                    case FARMLAND:
                        if (destBlock.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                            if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                                Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination
                                        .getBlock().getType().toString() + " BD: " + destBlock.getBlock().getBlockData().getAsString());

                            // Can we plant anything?
                            if (!this.plantSeeds(npc, destBlock, npcEquip, "HAND", true))
                                this.plantSeeds(npc, destBlock, npcEquip, "OFF_HAND", true);

                            continue;
                        } else {
                            destBlock.add(0, 1, 0);
                        }
                        ;
                        break;
                    default:
                        break;
                    }

                    if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination
                                .getBlock().getType().toString() + " BD: " + destBlock.getBlock().getBlockData().getAsString());

                    Ageable blockAge = null;
                    if (destBlock.getBlock().getBlockData() instanceof Ageable)
                        blockAge = (Ageable) destBlock.getBlock().getBlockData();

                    switch (destBlock.getBlock().getType()) {
                    case CACTUS:
                        // Validate the height of the cactus
                        int cactusTop = 0;
                        for (int y = -3; y <= 3; y++) {
                            if (destBlock.clone().add(0, y, 0).getBlock().getType() == Material.CACTUS) {
                                if (destBlock.clone().add(0, y + 1, 0).getBlock().getType() == Material.AIR)
                                    cactusTop = y;
                            }
                        }
                        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                        net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                        addToInventory(npc, destBlock.clone().add(0, cactusTop, 0).getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));

                        PlaySound(destBlock, soundType.TILL_DIRT);
                        destBlock.clone().add(0, cactusTop, 0).getBlock().setType(Material.AIR);
                        farmSet.lastAction = new Date();
                        farmSet.currentDestination = null;
                        farmSet.currentAction = CurrentAction.IDLE;

                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Cactus");
                        break;
                    case SUGAR_CANE:
                        // Validate the height of the cactus
                        int caneTop = 0;
                        for (int y = -3; y <= 3; y++) {
                            if (destBlock.clone().add(0, y, 0).getBlock().getType() == Material.SUGAR_CANE) {
                                if (destBlock.clone().add(0, y + 1, 0).getBlock().getType() == Material.AIR)
                                    caneTop = y;
                            }
                        }

                        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                        net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                        PlaySound(destBlock, soundType.TILL_DIRT);
                        addToInventory(npc, destBlock.clone().add(0, caneTop, 0).getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));
                        destBlock.clone().add(0, caneTop, 0).getBlock().setType(Material.AIR);
                        farmSet.lastAction = new Date();
                        farmSet.currentDestination = null;
                        farmSet.currentAction = CurrentAction.IDLE;

                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|SugarCane Farmed");
                        break;
                    case WHEAT:
                    case CARROT:
                    case CARROTS:
                    case POTATOES:
                    case BEETROOT:
                    case BEETROOTS:
                    case BEETROOT_SEEDS:
                        if (blockAge != null && blockAge.getAge() == blockAge.getMaximumAge()) {
                            net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                            net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                            PlaySound(destBlock, soundType.TILL_DIRT);
                            addToInventory(npc, destBlock.clone().getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));

                            if (npcFarmer.getValue().plantExisting) {
                                blockAge.setAge(0);
                                destBlock.getBlock().setBlockData(blockAge);
                            } else
                                destBlock.getBlock().setType(Material.AIR);

                            farmSet.lastAction = new Date();
                            farmSet.currentDestination = null;
                            farmSet.currentAction = CurrentAction.IDLE;

                            if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                                Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Potato");
                        }
                        break;
                    case MELON:
                    case PUMPKIN:
                        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                        net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                        PlaySound(destBlock, soundType.TILL_DIRT);
                        addToInventory(npc, destBlock.clone().getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));
                        destBlock.getBlock().setType(Material.AIR);

                        farmSet.lastAction = new Date();
                        farmSet.currentDestination = null;
                        farmSet.currentAction = CurrentAction.IDLE;
                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Activity Timeout");
                        break;
                    default:
                        break;
                    }
                    // No actions can be done.
                    farmSet.currentDestination = null;
                    farmSet.currentAction = CurrentAction.IDLE;
                    farmSet.lastAction = new Date();
                } else if (farmSet.lastAction.getTime() < (new Date().getTime() - 5000)) {
                    // Timeout..
                    farmSet.currentDestination = null;
                    farmSet.currentAction = CurrentAction.IDLE;
                    farmSet.lastAction = new Date();
                    if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|Activity Timeout");
                }
                break;
            case IDLE:
            default:

                Location newLocation = null;
                if (npcFarmer.getValue().regionName == null || npcFarmer.getValue().regionName.isEmpty()) {
                    newLocation = findRandomBlock(farmSet, npc.getEntity().getLocation(), npcFarmer.getValue().maxDistance * 2, npc);
                } else {
                    newLocation = findRandomBlock(farmSet, npc.getEntity().getLocation(), npcFarmer.getValue().regionName, npc);
                }

                if (newLocation == null)
                    continue;

                Location walkToLocation = newLocation;

                if (newLocation.distanceSquared(npc.getEntity().getLocation()) > 2) {
                    walkToLocation = findWalkableNextTo(npc, newLocation);
                    if (walkToLocation == null)
                        continue;
                } else if (newLocation.distanceSquared(npc.getEntity().getLocation()) < 2) {
                    farmSet.currentDestination = newLocation;
                    farmSet.lastAttempt = newLocation;
                    farmSet.lastAction = new Date();
                    farmSet.currentAction = CurrentAction.TRAVERSING;
                    net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), walkToLocation);
                    break;
                }

                farmSet.currentAction = CurrentAction.TRAVERSING;
                if (newLocation.getBlock().getType() == Material.AIR)
                    newLocation.add(0, -1, 0);

                farmSet.currentDestination = newLocation;
                farmSet.lastAttempt = newLocation;
                farmSet.lastAction = new Date();
                if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                    Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.pluginTick|NPC:" + npc.getId() + "|NewLocation|" + newLocation + "Walkto: " + walkToLocation + " Dist:"
                            + npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D).distanceSquared(walkToLocation) + " Block:" + newLocation.getBlock().getType().toString() + " D:" + newLocation.distanceSquared(npc.getEntity()
                                    .getLocation()));

                net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), walkToLocation);

                DestinationsPlugin.Instance.getPathClass.addToQueue(npc, farmSet.destinationsTrait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), walkToLocation, Destinations_Farmer.Instance.getDestinationsPlugin.maxDistance, new ArrayList<Material>(), 0, true, true, true, "DestinationsFarmer");
                break;
            }
        }
    }

    private boolean plantSeeds(NPC npc, Location destBlock, Equipment npcEquip, String slot, Boolean plant) {
        // check for items in the main hand and offhand
        if (EquipmentSlot.valueOf(slot) != null && npcEquip.get(EquipmentSlot.valueOf(slot)) != null) {
            switch (npcEquip.get(EquipmentSlot.valueOf(slot)).getType()) {
            case POTATO:
                // Plant a POTATO
                if (plant)
                    destBlock.clone().add(0, 1, 0).getBlock().setType(Material.POTATOES);
                return true;
            case CARROT:
                // Plant a carrot
                if (plant)
                    destBlock.clone().add(0, 1, 0).getBlock().setType(Material.CARROTS);
                return true;
            case BEETROOT_SEEDS:
                // BEETROOT
                if (plant)
                    destBlock.clone().add(0, 1, 0).getBlock().setType(Material.BEETROOT_SEEDS);
                return true;
            case WHEAT_SEEDS:
                if (plant)
                    destBlock.clone().add(0, 1, 0).getBlock().setType(Material.WHEAT_SEEDS);
                return true;
            default:
                break;
            }
        }
        return false;
    }

    private void addToInventory(NPC npc, ItemStack[] drops) {
        ItemStack[] npcInventory = npc.getTrait(Inventory.class).getContents();
        for (ItemStack item : drops) {
            int emptySlot = -1;
            for (int slot = 1; slot < npcInventory.length; slot++) {
                if (slot < 2 || (slot > 35 && slot < 41))
                    continue;

                if (npcInventory[slot] == null && emptySlot == -1) {
                    emptySlot = slot;
                    continue;
                } else if (npcInventory[slot] != null && npcInventory[slot].getType() == Material.AIR && emptySlot == -1) {
                    emptySlot = slot;
                    continue;
                }

                if (npcInventory[slot] != null)
                    if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Farmer_Processing.addToInventory|NPC:" + npc.getId() + "|Slot:" + slot + " Item:" + npcInventory[slot].getType() + "/"
                                + npcInventory[slot].getAmount() + "/" + npcInventory[slot].getType().getMaxStackSize() + " Inv Item:" + item.getType() + "/" + item.getAmount());
                if (npcInventory[slot] == null)
                    if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                        Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Farmer_Processing.addToInventory|NPC:" + npc.getId() + "|Slot: null" + " Inv Item:" + item.getType() + "/" + item
                                .getAmount());

                if (npcInventory[slot] != null) {
                    if (npcInventory[slot].getType() == item.getType() && npcInventory[slot].getAmount() < npcInventory[slot].getType().getMaxStackSize()) {
                        if (Destinations_Farmer.Instance.getDestinationsPlugin != null)
                            Destinations_Farmer.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Farmer_Processing.addToInventory|NPC:" + npc.getId() + "|SlotCheck: " + npcInventory[slot].getAmount() + item
                                    .getAmount() + ">" + npcInventory[slot].getType().getMaxStackSize());
                        if ((npcInventory[slot].getAmount() + item.getAmount()) > (npcInventory[slot].getType().getMaxStackSize())) {
                            int leftOver = Math.abs(npcInventory[slot].getType().getMaxStackSize() - (npcInventory[slot].getAmount() + item.getAmount()));
                            npcInventory[slot].setAmount(item.getAmount() - leftOver);
                            item.setAmount(item.getAmount() - (item.getAmount() - leftOver));
                        } else {
                            npcInventory[slot].setAmount(npcInventory[slot].getAmount() + item.getAmount());
                            item = null;
                            break;
                        }
                    }
                }
            }

            if (emptySlot != -1 && npcInventory[emptySlot] == null && item != null) {
                npcInventory[emptySlot] = item;
                item = null;
                emptySlot = npcInventory.length;
                continue;
            }
        }
        npc.getTrait(Inventory.class).setContents(npcInventory);
    }

    private Location findRandomBlock(NPC_Setting farmSet, Location sourceLocation, String regionName, NPC npc) {
        Random random = new Random(new Date().getTime());
        if (Destinations_Farmer.Instance.getWorldGuardPlugin == null)
            return null;

        ProtectedRegion region = Destinations_Farmer.Instance.getWorldGuardPlugin.getRegionManager(sourceLocation.getWorld()).getRegion(regionName);
        if (region == null)
            return null;

        Location minLocation = new Location(sourceLocation.getWorld(), region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());

        // Look next to the NPC to see if there is any work to be done.
        int distance = 0;
        while (distance < 5) {
            for (int x = (0 - distance); x <= distance; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = (0 - distance); z <= distance; z++) {
                        Location oNewDest = npc.getEntity().getLocation().add(x, y, z);

                        if (oNewDest.equals(farmSet))
                            continue;

                        if (region.contains(oNewDest.getBlockX(), oNewDest.getBlockY(), oNewDest.getBlockZ())) {
                            if (oNewDest.getBlock().getType() == Material.FARMLAND) {
                                if (isFarmable(oNewDest.clone().add(0, 1, 0), npc)) {
                                    return oNewDest;
                                }
                            } else {
                                if (isFarmable(oNewDest, npc)) {
                                    return oNewDest;
                                }
                            }
                        }
                    }
                }
            }
            distance++;
        }

        int nTrys = 0;
        while (nTrys < 10) {
            Location oNewDest = minLocation.clone().add(random.nextInt(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()), 0, random.nextInt(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint()
                    .getBlockZ()));
            distance = 0;
            if (region.contains(oNewDest.getBlockX(), oNewDest.getBlockY(), oNewDest.getBlockZ())) {
                while (distance < 5) {
                    for (int x = (0 - distance); x <= distance; x++) {
                        for (int y = -3; y <= 3; y++) {
                            for (int z = (0 - distance); z <= distance; z++) {
                                if (oNewDest.getBlock().getType() == Material.FARMLAND) {
                                    if (isFarmable(oNewDest.clone().add(0, y + 1, 0), npc)) {
                                        return oNewDest;
                                    }
                                } else {
                                    if (isFarmable(oNewDest.clone().add(0, y, 0), npc)) {
                                        return oNewDest;
                                    }
                                }
                            }
                        }
                    }
                    distance++;
                }
            }
            nTrys++;
        }
        return null;
    }

    private Location findRandomBlock(NPC_Setting farmSet, Location sourceLocation, int maxDistance, NPC npc) {
        Random random = new Random(new Date().getTime());

        // Look next to the NPC to see if there is any work to be done.
        int distance = 0;
        while (distance < 5) {
            for (int x = (0 - distance); x <= distance; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = (0 - distance); z <= distance; z++) {
                        Location oNewDest = npc.getEntity().getLocation().add(x, y, z);

                        if (oNewDest.equals(farmSet))
                            continue;

                        if (oNewDest.getBlock().getType() == Material.FARMLAND) {
                            if (isFarmable(oNewDest.clone().add(0, 1, 0), npc)) {
                                return oNewDest;
                            }
                        } else {
                            if (isFarmable(oNewDest, npc)) {
                                return oNewDest;
                            }
                        }
                    }
                }
            }
            distance++;
        }

        int nTrys = 0;
        while (nTrys < 10) {
            Location oNewDest = farmSet.destinationsTrait.currentLocation.destination.clone().add(random.nextInt((int) maxDistance * 2) - maxDistance, 0, random.nextInt((int) maxDistance * 2) - maxDistance);

            if (farmSet.destinationsTrait.currentLocation.destination.distanceSquared(oNewDest) <= (maxDistance * maxDistance)) {
                distance = 0;
                while (distance < 5) {
                    for (int x = (0 - distance); x <= distance; x++) {
                        for (int y = -3; y <= 3; y++) {
                            for (int z = (0 - distance); z <= distance; z++) {
                                if (oNewDest.getBlock().getType() == Material.FARMLAND) {
                                    if (isFarmable(oNewDest.clone().add(0, y + 1, 0), npc)) {
                                        return oNewDest;
                                    }
                                } else {
                                    if (isFarmable(oNewDest.clone().add(0, y, 0), npc)) {
                                        return oNewDest;
                                    }
                                }
                            }
                        }
                    }
                    distance++;
                }
            }
            nTrys++;
        }
        return null;
    }

    private boolean isFarmable(Location blockLocation, NPC npc) {
        Equipment npcEquip = npc.getTrait(Equipment.class);
        Ageable blockAge = null;

        if (blockLocation.getBlock().getBlockData() instanceof Ageable)
            blockAge = (Ageable) blockLocation.getBlock().getBlockData();

        switch (blockLocation.getBlock().getType()) {
        case CACTUS:
            // Validate the height of the cactus
            int cactusHeight = 0;

            for (int y = -3; y <= 3; y++) {
                if (blockLocation.clone().add(0, y, 0).getBlock().getType() == Material.CACTUS) {
                    cactusHeight++;
                }
            }
            if (cactusHeight > 2) {
                return true;
            } else {
                return false;
            }
        case SUGAR_CANE:
            // Validate the height of the cactus
            int caneHeight = 0;

            for (int y = -3; y <= 3; y++) {
                if (blockLocation.clone().add(0, y, 0).getBlock().getType() == Material.SUGAR_CANE) {
                    caneHeight++;
                }
            }
            if (caneHeight > 2) {
                return true;
            } else {
                return false;
            }
        case CARROT:
        case CARROTS:
        case POTATOES:
        case BEETROOT:
        case BEETROOTS:
        case BEETROOT_SEEDS:
            if (blockAge != null && blockAge.getAge() == blockAge.getMaximumAge()) {
                return true;
            }
            break;
        case MELON:
        case PUMPKIN:
        case WHEAT:
            return true;
        case GRASS:
        case DIRT:
            if (blockLocation.getBlock().getRelative(0, 1, 0).getType() != Material.AIR)
                return false;

            if (handMatches(npc, EquipmentSlot.HAND, "_HOE"))
                return true;

            if (EquipmentSlot.valueOf("OFF_HAND") != null)
                if (handMatches(npc, EquipmentSlot.OFF_HAND, "_HOE"))
                    return true;

            break;
        case FARMLAND:
            if (blockLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                // Can we plant anything?
                if (this.plantSeeds(npc, blockLocation, npcEquip, "HAND", false))
                    return true;
                if (this.plantSeeds(npc, blockLocation, npcEquip, "OFF_HAND", false))
                    return true;
            }
            break;
        default:
            break;
        }
        return false;
    }

    private boolean handMatches(NPC npc, EquipmentSlot slot, String name) {
        Equipment npcEquip = npc.getTrait(Equipment.class);

        if (npcEquip == null)
            return false;

        if (npcEquip.get(slot) == null)
            return false;

        if (npcEquip.get(slot).getType().toString().contains(name))
            return true;

        return false;

    }

    private Location findWalkableNextTo(NPC npc, Location blockLocation) {

        switch (blockLocation.getBlock().getType()) {
        case CACTUS:
        case SUGAR_CANE:
            break;
        default:
            return blockLocation;
        }

        float yaw = blockLocation.setDirection(npc.getEntity().getLocation().toVector().subtract(blockLocation.toVector())).getYaw();

        // North: -Z
        // East: +X
        // South: +Z
        // West: -X
        int xAxis = 0;
        int zAxis = 0;

        if (0 <= yaw && yaw < 22.5) {
            xAxis = 0;
            zAxis = -1;
        } else if (22.5 <= yaw && yaw < 67.5) {
            xAxis = 1;
            zAxis = -1;
        } else if (67.5 <= yaw && yaw < 112.5) {
            xAxis = 1;
            zAxis = 0;
        } else if (112.5 <= yaw && yaw < 157.5) {
            xAxis = 1;
            zAxis = 1;
        } else if (157.5 <= yaw && yaw < 202.5) {
            xAxis = 0;
            zAxis = 1;
        } else if (202.5 <= yaw && yaw < 247.5) {
            xAxis = -1;
            zAxis = 1;
        } else if (247.5 <= yaw && yaw < 292.5) {
            xAxis = -1;
            zAxis = 0;
        } else if (292.5 <= yaw && yaw < 337.5) {
            xAxis = -1;
            zAxis = -1;
        } else if (337.5 <= yaw && yaw < 360.0) {
            xAxis = 0;
            zAxis = -1;
        }

        Location farmLocation = blockLocation.clone().add(xAxis, 0, zAxis);
        if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(farmLocation.clone())) {
            return farmLocation.clone();
        }

        int counter = 0;
        while (counter < 25) {
            for (byte y = -1; y <= 1; y++) {
                xAxis = (Math.random() * 2 + 1) == 1 ? -1 : 1;
                zAxis = (Math.random() * 2 + 1) == 1 ? -1 : 1;
                if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(blockLocation.clone().add(xAxis, y, zAxis))) {
                    return blockLocation.clone().add(xAxis, y, zAxis);
                }
            }
            counter++;
        }
        return null;
    }

    private void PlaySound(Location soundLocation, soundType sound) {
        String soundName = "";
        switch (sound) {
        case PLANT:
            for (Sound sndName : Sound.values()) {
                if (sndName.name().equals("BLOCK_GRASS_PLACE")) {
                    soundName = "BLOCK_GRASS_PLACE";
                    break;
                }
                if (sndName.name().equals("STEP_GRASS")) {
                    soundName = "STEP_GRASS";
                    break;
                }
            }
            if (soundName.equals(""))
                return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getWorld().playSound(soundLocation, Sound.valueOf(soundName), 1F, 1F);
            }
            break;
        case TILL_DIRT:
            for (Sound sndName : Sound.values()) {
                if (sndName.name().equals("ITEM_HOE_TILL ")) {
                    soundName = "ITEM_HOE_TILL ";
                    break;
                }
                if (sndName.name().equals("DIG_GRASS")) {
                    soundName = "DIG_GRASS";
                    break;
                }
            }
            if (soundName.equals(""))
                return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getWorld().playSound(soundLocation, Sound.valueOf(soundName), 1F, 1F);
            }
            break;
        default:
            break;

        }
    }

    private enum soundType {
        TILL_DIRT, PLANT,
    }

}
