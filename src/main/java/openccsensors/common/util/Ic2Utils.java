package openccsensors.common.util;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropTile;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.NodeStats;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.tile.IEnergyStorage;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import openccsensors.common.sensor.CropSensor;

public class Ic2Utils {

	public static final int MASSFAB_MAX_ENERGY = 1100000;
	protected static final String MASS_FAB_CLASS = "ic2.core.block.machine.tileentity.TileEntityMatter";
	
	public static boolean isValidPowerTarget(Object target) {
		TileEntity energyTarget = null;
		if (target instanceof TileEntity) {
			TileEntity tile = (TileEntity) target;
			energyTarget = EnergyNet.instance.getTileEntity(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		}
		
		return energyTarget != null &&
			    (
		    		energyTarget instanceof IEnergySink ||
		    		energyTarget instanceof IEnergySource ||
		    		energyTarget instanceof IEnergyConductor ||
		    		energyTarget instanceof IEnergyStorage
			    );
	}

	public static boolean isValidMachineTarget(Object target) {
		return target != null &&
				 (
					//target.getClass().getName() == MASS_FAB_CLASS ||
					target instanceof IReactor ||
					target instanceof IReactorChamber
				 );
	}
	
	public static HashMap getMachineDetails(World world, Object obj, boolean additional) {

		HashMap response = new HashMap();
		
		if (obj == null || !(obj instanceof TileEntity) || !additional) {
			return response;
		}

		TileEntity tile = (TileEntity) obj;
		
		IReactor reactor = null;
		if (tile instanceof IReactor) {
			reactor = (IReactor) tile;
		}else if (tile instanceof IReactorChamber) {
			reactor = ((IReactorChamber) tile).getReactor();
		}
		
		if (reactor != null) {
			int maxHeat = reactor.getMaxHeat();
			int heat = reactor.getHeat();
			response.put("Heat", heat);
			response.put("MaxHeat", maxHeat);
			response.put("Output", reactor.getReactorEUEnergyOutput());
			response.put("Active", reactor.produceEnergy());
			response.put("HeatPercentage", 0);
			if (maxHeat > 0) {
				double heatPercentage = ((100.0 / maxHeat) * heat);
				response.put("HeatPercentage", Math.max(Math.min(heatPercentage, 100), 0));
			}
		}

		if (tile.getClass().getName() == MASS_FAB_CLASS) {
			NBTTagCompound tagCompound = getTagCompound(tile);
			response.put("Energy", tagCompound.getInteger("energy"));
			response.put("MaxEnergy", MASSFAB_MAX_ENERGY);
			response.put("Progress", 0);
			double progress = ((100.0 / MASSFAB_MAX_ENERGY) * tagCompound.getInteger("energy"));
			response.put("Progress",  Math.min(Math.max(0, progress), 100));
		}
		
		return response;
	}
	
	public static HashMap getPowerDetails(World world, Object obj, boolean additional) {
		
		HashMap response = new HashMap();
		
		if (obj == null || !(obj instanceof TileEntity) || !additional) {
			return response;
		}
		
		TileEntity tile = (TileEntity) obj;
		
		if (tile instanceof IEnergyStorage) {
			
			IEnergyStorage storage = (IEnergyStorage) tile;
			int capacity = storage.getCapacity();
			int stored = storage.getStored();
	
			response.put("Stored", stored);
			response.put("Capacity", capacity);
			response.put("Output", storage.getOutput());
			response.put("StoredPercentage", 0);

			if (capacity > 0) {
				response.put("StoredPercentage", Math.max(Math.min(100,((100.0 / capacity) * stored)), 0));
			}
		}
		
		if (tile instanceof IEnergySink ||
			tile instanceof IEnergySource ||
			tile instanceof IEnergyConductor) {
			
			tile = EnergyNet.instance.getTileEntity(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
			if (tile != null) {
				NodeStats stats = EnergyNet.instance.getNodeStats(tile);
				double energyIn = stats.getEnergyIn();
				double energyOut = stats.getEnergyOut();
				response.put("EnergyEmitted", energyOut);
				response.put("EnergySunken", energyIn);
				response.put("EnergyOut", energyOut);
				response.put("EnergyIn", energyIn);
				response.put("Voltage", stats.getVoltage());
			}
		}
		
		return response;
	}

	protected static NBTTagCompound getTagCompound(TileEntity tile) {
		NBTTagCompound tagCompound = new NBTTagCompound();
		tile.writeToNBT(tagCompound);
		return tagCompound;
	}

	public static boolean isValidCropTarget(TileEntity tile) {
		return tile instanceof ICropTile;
	}

	public static Map getCropDetails(Object obj, ChunkCoordinates sensorPos, boolean additional) {
		HashMap response = new HashMap();
		if (obj == null) return response;

		TileEntity tile = (TileEntity) obj;
		HashMap position = new HashMap();
		position.put("X", tile.xCoord - sensorPos.posX);
		position.put("Y", tile.yCoord - sensorPos.posY);
		position.put("Z", tile.zCoord - sensorPos.posZ);
		response.put("Position", position);

		ItemStack stack = new ItemStack(tile.getBlockType(), 1, tile.getBlockMetadata());
		
		response.put("Name", InventoryUtils.getNameForItemStack(stack));
		response.put("RawName", InventoryUtils.getRawNameForStack(stack));
		response.put("DamageValue", stack.getItemDamage());
		
		if (obj instanceof ICropTile && additional) {
			ICropTile crop = (ICropTile) obj;
			response.put("AirQuality", crop.getAirQuality());
			response.put("Growth", crop.getGrowth());
			response.put("Gain", crop.getGain());
			response.put("Humidity", crop.getHumidity());
			response.put("HydrationStorage", crop.getHydrationStorage());
			response.put("LightLevel", crop.getLightLevel());
			response.put("Nutrients", crop.getNutrients());
			response.put("NutrientStorage", crop.getNutrientStorage());
			response.put("Resistance", crop.getResistance());
			response.put("ScanLevel", crop.getScanLevel());
			response.put("Size", crop.getSize());
			response.put("WeedExStorage", crop.getWeedExStorage());
			response.put("Status", "Empty");
			CropCard[] cards = Crops.instance.getCropList();
			if (crop.getID() >= 0 && crop.getID() < cards.length) {
				CropCard cropCard = Crops.instance.getCropList()[crop.getID()];
				response.put("IsWeed", cropCard.isWeed(crop));
				response.put("CanBeHarvested", cropCard.canBeHarvested(crop));
				if (cropCard.canBeHarvested(crop)) {
					response.put("Status", CropSensor.STATUS_GROWN);
				}else if (crop.getSize() == 0) {
					response.put("Status", CropSensor.STATUS_NEW);
				}else {
					response.put("Status", CropSensor.STATUS_GROWING);
				}
				response.put("DiscoveredBy", cropCard.discoveredBy());
				response.put("EmittedLight", cropCard.getEmittedLight(crop));
				response.put("SizeAfterHarvest", cropCard.getSizeAfterHarvest(crop));
				response.put("CanCross", cropCard.canCross(crop));
				response.put("CanGrow", cropCard.canGrow(crop));
				response.put("Name", cropCard.getClass().getSimpleName());
			}
		}
		return response;
	}
}
