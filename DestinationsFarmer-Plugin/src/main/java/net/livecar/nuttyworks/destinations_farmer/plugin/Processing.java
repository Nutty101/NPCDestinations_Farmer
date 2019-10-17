package net.livecar.nuttyworks.destinations_farmer.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.livecar.nuttyworks.destinations_farmer.Farmer;
import net.livecar.nuttyworks.destinations_farmer.plugin.storage.Location_Setting;
import net.livecar.nuttyworks.destinations_farmer.plugin.storage.NPC_Setting;
import net.livecar.nuttyworks.destinations_farmer.plugin.storage.NPC_Setting.CurrentAction;
import net.livecar.nuttyworks.destinations_farmer.plugin.plugin.VersionInterface.FarmMaterial;

import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.worldguard.WorldGuardInterface.RegionShape;

public class Processing {

    private Farmer  farmerRef;

    public Processing(Farmer farmerReference)
    {
        farmerRef = farmerReference;
    }

    public void onPluginTick() {

        Iterator<Entry<Integer, Location_Setting>> farmingIterator = farmerRef.monitoredNPCs.entrySet().iterator();
        while (farmingIterator.hasNext()) {

            if (!farmingIterator.hasNext())
                return;

            Entry<Integer, Location_Setting> npcFarmer = farmingIterator.next();

            if (npcFarmer.getValue().locationID == null)
                continue;

            NPC npc = CitizensAPI.getNPCRegistry().getById(npcFarmer.getKey());
            if (npc == null)
                continue;

            if (!npc.isSpawned())
                continue;

            if (npc.getNavigator().isNavigating())
                continue;

            NPC_Setting farmSet = farmerRef.npcSettings.get(npcFarmer.getKey());
            Equipment npcEquip = npc.getTrait(Equipment.class);

            if (DestinationsPlugin.Instance != null)
                DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Current Action|" + farmSet.currentAction.toString());

            switch (farmSet.currentAction) {
                case ABORTING:
                    if (farmSet.destinationsTrait != null && farmSet.destinationsTrait.currentLocation != null && farmSet.destinationsTrait.currentLocation.destination.distanceSquared(npc.getEntity().getLocation()) > 3) {
                        if (DestinationsPlugin.Instance != null)
                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Not at destination|" + farmSet.destinationsTrait.currentLocation.destination
                                    + " Dist:" + npc.getEntity().getLocation().distanceSquared(farmSet.destinationsTrait.currentLocation.destination));

                        DestinationsPlugin.Instance.getPathClass.addToQueue(npc, farmSet.destinationsTrait, npc.getEntity().getLocation(), farmSet.destinationsTrait.currentLocation.destination, DestinationsPlugin.Instance.maxDistance,
                                new ArrayList<Material>(), 0, true, true, true, "DestinationsFarmer");
                        return;
                    } else {
                        if (farmerRef.monitoredNPCs.containsKey(npc.getId())) {
                            if (DestinationsPlugin.Instance != null)
                                DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|ABORTED|Removing monitors");

                            farmSet.destinationsTrait.unsetMonitoringPlugin();
                            farmerRef.npcSettings.get(npc.getId()).currentAction = CurrentAction.IDLE;
                            farmerRef.npcSettings.get(npc.getId()).currentDestination = null;
                            farmerRef.monitoredNPCs.remove(npc.getId());
                            continue;
                        }
                    }
                    break;
                case FARMING:

                    break;
                case TRAVERSING:
                    if (farmSet.currentDestination != null && !npc.getNavigator().isNavigating() && farmSet.currentDestination.distance(npc.getEntity().getLocation()) < 12) {
                        Location destBlock = farmSet.currentDestination.clone();

                        // Close enough, farm the block
                        FarmMaterial convertMat = farmerRef.getBridge.convertMaterial(farmSet.currentDestination.getBlock().getType());
                        if (convertMat != null) {
                            switch (convertMat) {
                                case GRASS_BLOCK:
                                case DIRT:
                                    if (DestinationsPlugin.Instance != null)
                                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination.getBlock()
                                                .getType().toString());

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

                                        farmSet.lastAction = new Date();
                                        destBlock.getBlock().setType(farmerRef.getBridge.convertMaterial(FarmMaterial.FARMLAND));
                                        farmSet.currentDestination = null;
                                        farmSet.currentAction = CurrentAction.IDLE;
                                        continue;
                                    }
                                    break;
                                case FARMLAND:
                                    if (destBlock.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                                        if (DestinationsPlugin.Instance != null)
                                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination.getBlock().getType().toString());

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
                        }

                        if (DestinationsPlugin.Instance != null)
                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|TRAVERSING|DestinationBlock: " + farmSet.currentDestination.getBlock().getType().toString());

                        convertMat = farmerRef.getBridge.convertMaterial(destBlock.getBlock().getType());
                        if (convertMat != null) {
                            switch (convertMat) {
                                case COCOA_BEANS:
                                case COCOA:
                                    net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);

                                    if (npc.getEntity() instanceof Player)
                                        net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                                    Random rnd = new Random();
                                    rnd.setSeed((new Date()).getTime());

                                    if (rnd.nextInt(1) == 1)

                                        addToInventory(npc, destBlock.clone().getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));

                                    PlaySound(destBlock, soundType.TILL_DIRT);

                                    if (npcFarmer.getValue().plantExisting) {
                                        farmerRef.getBridge.setMinAge(destBlock);
                                    } else
                                        destBlock.clone().getBlock().setType(Material.AIR);

                                    farmSet.lastAction = new Date();
                                    farmSet.currentDestination = null;
                                    farmSet.currentAction = CurrentAction.IDLE;

                                    if (DestinationsPlugin.Instance != null)
                                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Cactus");
                                    break;

                                case CACTUS:
                                case CACTUS_GREEN:
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

                                    if (DestinationsPlugin.Instance != null)
                                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Cactus");
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

                                    if (DestinationsPlugin.Instance != null)
                                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|SugarCane Farmed");
                                    break;
                                case WHEAT:
                                case CARROT:
                                case CARROTS:
                                case POTATOES:
                                case BEETROOT:
                                case BEETROOTS:
                                case BEETROOT_SEEDS:
                                    if (farmerRef.getBridge.isFullGrown(destBlock)) {
                                        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), destBlock);
                                        net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                                        PlaySound(destBlock, soundType.TILL_DIRT);
                                        addToInventory(npc, destBlock.clone().getBlock().getDrops().toArray(new ItemStack[destBlock.clone().getBlock().getDrops().size()]));

                                        if (npcFarmer.getValue().plantExisting) {
                                            farmerRef.getBridge.setMinAge(destBlock);
                                        } else
                                            destBlock.getBlock().setType(Material.AIR);

                                        farmSet.lastAction = new Date();
                                        farmSet.currentDestination = null;
                                        farmSet.currentAction = CurrentAction.IDLE;

                                        if (DestinationsPlugin.Instance != null)
                                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Potato");
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
                                    if (DestinationsPlugin.Instance != null)
                                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Activity Timeout");
                                    break;
                                default:
                                    break;
                            }
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
                        if (DestinationsPlugin.Instance != null)
                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|Activity Timeout");
                    }
                    break;
                case IDLE:
                default:

                    Location newLocation = null;
                    if (npcFarmer.getValue().regionName == null || npcFarmer.getValue().regionName.isEmpty()) {
                        newLocation = locateNPCWork(farmSet, npc.getEntity().getLocation(), npcFarmer.getValue().maxDistance, npc);
                    } else {
                        newLocation = locateNPCWork(farmSet, npc.getEntity().getLocation(), npcFarmer.getValue().regionName, npc);
                    }

                    if (newLocation == null) {
                        if (npcFarmer.getValue().blocking) {
                            npcFarmer.getValue().blockUntil = new Date().getTime() + 10000L;
                        }
                        continue;
                    }

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
                    if (DestinationsPlugin.Instance != null)
                        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "Farmer_Processing.onPluginTick|NPC:" + npc.getId() + "|NewLocation|" + newLocation + "Walkto: " + walkToLocation + " Dist:" + npc
                                .getEntity().getLocation().distanceSquared(walkToLocation) + " Block:" + newLocation.getBlock().getType().toString() + " D:" + newLocation.distanceSquared(npc.getEntity().getLocation()));

                    net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), walkToLocation);

                    DestinationsPlugin.Instance.getPathClass.addToQueue(npc, farmSet.destinationsTrait, npc.getEntity().getLocation(), walkToLocation, DestinationsPlugin.Instance.maxDistance,
                            new ArrayList<Material>(), 0, true, true, true, "DestinationsFarmer");
                    return;
            }
        }
    }

    private boolean plantSeeds(NPC npc, Location destBlock, Equipment npcEquip, String slot, Boolean plant) {
        // check for items in the main hand and offhand
        if (EquipmentSlot.valueOf(slot) != null && npcEquip.get(EquipmentSlot.valueOf(slot)) != null) {
            switch (farmerRef.getBridge.convertMaterial(npcEquip.get(EquipmentSlot.valueOf(slot)).getType())) {
                case POTATO:
                    // Plant a POTATO
                    if (plant)
                        destBlock.clone().add(0, 1, 0).getBlock().setType(farmerRef.getBridge.convertMaterial(FarmMaterial.POTATOES));
                    return true;
                case CARROTS:
                case CARROT:
                    // Plant a carrot
                    if (plant)
                        destBlock.clone().add(0, 1, 0).getBlock().setType(farmerRef.getBridge.convertMaterial(FarmMaterial.CARROTS));
                    return true;
                case BEETROOT_SEEDS:
                    // BEETROOT
                    if (plant)
                        destBlock.clone().add(0, 1, 0).getBlock().setType(farmerRef.getBridge.convertMaterial(FarmMaterial.BEETROOT_SEEDS));
                    return true;
                case WHEAT_SEEDS:
                    if (plant)
                        destBlock.clone().add(0, 1, 0).getBlock().setType(farmerRef.getBridge.convertMaterial(FarmMaterial.WHEAT_SEEDS));
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

                if (npcInventory[slot] != null) {
                    if (npcInventory[slot].getType() == item.getType() && npcInventory[slot].getAmount() < npcInventory[slot].getType().getMaxStackSize()) {
                        if (DestinationsPlugin.Instance != null)
                            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "NPC:" + npc.getId() + "|SlotCheck: " + npcInventory[slot].getAmount() + item.getAmount() + ">"
                                    + npcInventory[slot].getType().getMaxStackSize());
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

    public Location locateNPCWork(NPC_Setting farmSet, Location sourceLocation, String regionName, NPC npc) {
        Location[] regionBounds = DestinationsPlugin.Instance.getWorldGuardPlugin.getRegionBounds(sourceLocation.getWorld(), regionName);
        if (regionBounds == null || regionBounds.length == 0)
            return null;

        Double maxRadius = npc.getEntity().getLocation().distance(regionBounds[0]);
        if (npc.getEntity().getLocation().distance(regionBounds[1]) > maxRadius)
            maxRadius = npc.getEntity().getLocation().distance(regionBounds[1]);

        RegionShape regionShape = DestinationsPlugin.Instance.getWorldGuardPlugin.getRegionShape(regionBounds[0], regionName);

        // Look next to the NPC to see if there is any work to be done.
        int distance = 0;
        while (distance < maxRadius) {

            for (int x = (0 - distance); x <= distance; x++) {
                for (int z = (0 - distance); z <= distance; z++) {
                    for (int y = -3; y <= 3; y++) {


                        Location oNewDest = npc.getEntity().getLocation().add(x, y, z);

                        // Validate if the location we are looking at is outside
                        // the bounds.
                        if (oNewDest.getBlockX() < regionBounds[0].getBlockX() || oNewDest.getBlockX() > regionBounds[1].getBlockX())
                            continue;
                        if (oNewDest.getBlockY() < regionBounds[0].getBlockY() || oNewDest.getBlockY() > regionBounds[1].getBlockY())
                            continue;
                        if (oNewDest.getBlockZ() < regionBounds[0].getBlockZ() || oNewDest.getBlockZ() > regionBounds[1].getBlockZ())
                            continue;

                        if (oNewDest.equals(farmSet.currentDestination))
                            continue;

                        if (regionShape == RegionShape.POLYGON)
                            if (!DestinationsPlugin.Instance.getWorldGuardPlugin.isInRegion(oNewDest, regionName))
                                continue;

                        if (DestinationsPlugin.Instance.getPlotSquared != null) {
                            if (!DestinationsPlugin.Instance.getPlotSquared.locationInSamePlotAsNPC(npc, oNewDest))
                                continue;
                        }

                        if (oNewDest.getBlock().getType() == farmerRef.getBridge.convertMaterial(FarmMaterial.FARMLAND)) {
                            if (isFarmable(oNewDest.clone().add(0, 1, 0), npc)) {
                                if (!isFarmerNPCClose(oNewDest.clone().add(0, 1, 0), npc)) {
                                    return oNewDest.clone();
                                }
                            }
                        } else {
                            if (isFarmable(oNewDest, npc)) {
                                if (!isFarmerNPCClose(oNewDest.clone(), npc))
                                    return oNewDest;
                            }
                        }
                    }
                }
            }

            distance++;
        }

        return null;
    }

    public Location locateNPCWork(NPC_Setting farmSet, Location sourceLocation, int maxRadius, NPC npc) {

        if (farmSet.destinationsTrait.currentLocation == null)
            return null;

        // Look next to the NPC to see if there is any work to be done.
        int distance = 0;

        int maxDist = (maxRadius * maxRadius);

        Location startLoc = sourceLocation;

        for (int i = 0;i<2;i++) {
            while (distance < maxRadius) {
                for (int x = (0 - distance); x <= distance; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = (0 - distance); z <= distance; z++) {

                            Location oNewDest = startLoc.clone().add(x, y, z);

                            if (oNewDest.equals(farmSet.currentDestination))
                                continue;

                            if (DestinationsPlugin.Instance.getPlotSquared != null) {
                                if (!DestinationsPlugin.Instance.getPlotSquared.locationInSamePlotAsNPC(npc, oNewDest))
                                    continue;
                            }

                            if (oNewDest.getBlock().getType() == farmerRef.getBridge.convertMaterial(FarmMaterial.FARMLAND)) {
                                if (isFarmable(oNewDest.clone().add(0, 1, 0), npc)) {
                                    if (!isFarmerNPCClose(oNewDest.clone().add(0, 1, 0), npc)) {
                                        if (oNewDest.distanceSquared(farmSet.destinationsTrait.currentLocation.destination) <= maxDist)
                                            return oNewDest.clone();
                                    }
                                }
                            } else {
                                if (isFarmable(oNewDest, npc)) {
                                    if (!isFarmerNPCClose(oNewDest.clone(), npc))
                                        if (oNewDest.distanceSquared(farmSet.destinationsTrait.currentLocation.destination) <= maxDist)
                                            return oNewDest.clone();
                                }
                            }
                        }
                    }
                }
                distance++;
            }
            startLoc = farmSet.destinationsTrait.currentLocation.destination;
        }

        return null;
    }

    private boolean isFarmerNPCClose(Location location, NPC npc) {
        for (Map.Entry<Integer, Location_Setting> entry : farmerRef.monitoredNPCs.entrySet()) {
            if (entry.getKey() == npc.getId())
                continue;

            NPC tmpNPC = CitizensAPI.getNPCRegistry().getById(entry.getKey());
            if (tmpNPC != null && tmpNPC.isSpawned())
                if (tmpNPC.getEntity().getLocation().distanceSquared(location) < 25)
                    return true;

        }
        return false;
    }

    private boolean isFarmable(Location blockLocation, NPC npc) {
        Equipment npcEquip = npc.getTrait(Equipment.class);

        // Close enough, farm the block
        FarmMaterial convertMat = farmerRef.getBridge.convertMaterial(blockLocation.getBlock().getType());
        if (convertMat != null) {

            switch (convertMat) {
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
                case COCOA:
                case COCOA_BEANS:
                    if (farmerRef.getBridge.isFullGrown(blockLocation))
                        return true;
                    break;
                case MELON:
                case PUMPKIN:
                case WHEAT:
                    return true;
                case GRASS_BLOCK:
                case DIRT:
                    if (blockLocation.getBlock().getRelative(0, 1, 0).getType() != Material.AIR)
                        return false;

                    if (handMatches(npc, EquipmentSlot.HAND, "_HOE"))
                        return true;

                    if (EquipmentSlot.valueOf("OFF_HAND") != null)
                        if (handMatches(npc, EquipmentSlot.OFF_HAND, "_HOE"))
                            return true;

                    break;
                case AIR:
                    if (blockLocation.clone().add(0, -1, 0).getBlock().getType() == farmerRef.getBridge.convertMaterial(FarmMaterial.FARMLAND)) {
                        // Can we plant anything?
                        if (this.plantSeeds(npc, blockLocation.clone().add(0, -1, 0), npcEquip, "HAND", false))
                            return true;
                        if (this.plantSeeds(npc, blockLocation.clone().add(0, -1, 0), npcEquip, "OFF_HAND", false))
                            return true;
                    }
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
        if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(farmLocation.clone(),false,false,false)) {
            return farmLocation.clone();
        }

        if (!blockLocation.getBlock().getType().isSolid())
            return blockLocation;

        for (byte x = -1; x < 2; x++) {
            for (byte z = -1; z < 2; z++) {
                for (byte y = -1; y <= 1; y++) {
                    if (DestinationsPlugin.Instance.getPathClass.isLocationWalkable(blockLocation.clone().add(x, y, z), false, false, false)) {
                        return blockLocation.clone().add(x, y, z);
                    }
                }
            }

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
