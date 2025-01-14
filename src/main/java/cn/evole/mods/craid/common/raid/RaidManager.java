package cn.evole.mods.craid.common.raid;

import cn.evole.mods.craid.CRaid;
import cn.evole.mods.craid.CRaidUtil;
import cn.evole.mods.craid.api.*;
import cn.evole.mods.craid.api.events.RaidEvent;
import cn.evole.mods.craid.common.impl.CRaidAPIImpl;
import cn.evole.mods.craid.common.impl.RaidComponent;
import cn.evole.mods.craid.common.impl.SpawnComponent;
import cn.evole.mods.craid.common.impl.WaveComponent;
import cn.evole.mods.craid.common.impl.amount.ConstantAmount;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class RaidManager {

	private static final Map<ResourceLocation, IRaidComponent> RAID_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends IRaidComponent>> RAID_TYPE_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends IWaveComponent>> WAVE_TYPE_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends ISpawnComponent>> SPAWN_TYPE_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends IAmountComponent>> AMOUNT_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends IPlacementComponent>> PLACEMENT_MAP = Maps.newHashMap();
	private static final Map<String, Class<? extends IRewardComponent>> REWARD_MAP = Maps.newHashMap();

	public static void finishRaidMap(Map<ResourceLocation, IRaidComponent> map) {
		RAID_MAP.putAll(map);
	}

	public static void tickRaids(Level world) {
		if(! world.isClientSide) {
			final WorldRaidData data = WorldRaidData.getInvasionData(world);
			data.tick(world);
		}
	}

	public static boolean hasRaidNearby(ServerLevel world, BlockPos pos) {
		final List<Raid> list = getRaids(world);
		for(Raid r : list) {
			if(Math.abs(r.getCenter().getX() - pos.getX()) <= CRaidUtil.getRaidRange()
					|| Math.abs(r.getCenter().getY() - pos.getY()) <= CRaidUtil.getRaidRange()
					|| Math.abs(r.getCenter().getZ() - pos.getZ()) <= CRaidUtil.getRaidRange()) {
				return true;
			}
		}
		return false;
	}

	public static void createRaid(ServerLevel world, ResourceLocation res, BlockPos pos) {
		final WorldRaidData data = WorldRaidData.getInvasionData(world);
		final Raid raid = data.createRaid(world, res, pos);
		MinecraftForge.EVENT_BUS.post(new RaidEvent.RaidStartEvent(raid));
	}

	public static List<Raid> getRaids(ServerLevel world) {
		return WorldRaidData.getInvasionData(world).getRaids();
	}

	public static boolean isRaider(ServerLevel world, Entity entity) {
		final WorldRaidData data = WorldRaidData.getInvasionData(world);
		for(Raid raid : data.getRaids()) {
			if(raid.isRaider(entity)) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	public static IRaidComponent getRaidComponent(ResourceLocation res) {
		return RAID_MAP.getOrDefault(res, null);
	}

	/**
	 * {@link CRaidAPIImpl#registerSpawnAmount(String, Class)}
	 */
	public static void registerSpawnAmount(String name, Class<? extends IAmountComponent> c) {
		if(AMOUNT_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Spawn Amount : duplicate name, overwrited.");
		}
		AMOUNT_MAP.put(name, c);
	}

	/**
	 * {@link CRaidAPIImpl#registerSpawnPlacement(String, Class)}
	 */
	public static void registerSpawnPlacement(String name, Class<? extends IPlacementComponent> c) {
		if(PLACEMENT_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Spawn Placement : duplicate name, overwrited.");
		}
		PLACEMENT_MAP.put(name, c);
	}

	/**
	 * {@link CRaidAPIImpl#registerSpawnPlacement(String, Class)}
	 */
	public static void registerReward(String name, Class<? extends IRewardComponent> c) {
		if(REWARD_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Reward : duplicate name, overwrited.");
		}
		REWARD_MAP.put(name, c);
	}

	/**
	 * {@link CRaidAPIImpl#registerRaidType(String, Class)}
	 */
	public static void registerRaidType(String name, Class<? extends IRaidComponent> c) {
		if(RAID_TYPE_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Raid Type : duplicate name, overwrited.");
		}
		RAID_TYPE_MAP.put(name, c);
	}

	/**
	 * {@link CRaidAPIImpl#registerWaveType(String, Class)}
	 */
	public static void registerWaveType(String name, Class<? extends IWaveComponent> c) {
		if(WAVE_TYPE_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Wave Type : duplicate name, overwrited.");
		}
		WAVE_TYPE_MAP.put(name, c);
	}

	/**
	 * {@link CRaidAPIImpl#registerSpawnType(String, Class)}
	 */
	public static void registerSpawnType(String name, Class<? extends ISpawnComponent> c) {
		if(SPAWN_TYPE_MAP.containsKey(name)) {
			CRaid.LOGGER.warn("Register Spawn Type : duplicate name, overwrited.");
		}
		SPAWN_TYPE_MAP.put(name, c);
	}

	/**
	 * get amount component by type name.
	 */
	public static IAmountComponent getSpawnAmount(String name) {
		if(AMOUNT_MAP.containsKey(name)) {
			try {
				return AMOUNT_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			CRaid.LOGGER.warn("Spawn Amount Missing : can not find {}", name);
		}
		return new ConstantAmount();
	}

	/**
	 * get placement component by type name.
	 */
	@Nullable
	public static IPlacementComponent getSpawnPlacement(String name) {
		if(PLACEMENT_MAP.containsKey(name)) {
			try {
				return PLACEMENT_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			CRaid.LOGGER.warn("Spawn Placement Missing : can not find {}", name);
		}
		return null;
	}

	/**
	 * get placement component by type name.
	 */
	@Nullable
	public static IRewardComponent getReward(String name) {
		if(REWARD_MAP.containsKey(name)) {
			try {
				return REWARD_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			CRaid.LOGGER.warn("Reward Missing : can not find {}", name);
		}
		return null;
	}

	/**
	 * get raid component by type name.
	 */
	@Nullable
	public static IRaidComponent getRaidType(String name) {
		if(RAID_TYPE_MAP.containsKey(name)) {
			try {
				return RAID_TYPE_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if(! name.equals("")){
			CRaid.LOGGER.warn("Raid Type Missing : can not find {}", name);
		}
		return new RaidComponent();
	}

	/**
	 * get wave component by type name.
	 */
	@Nullable
	public static IWaveComponent getWaveType(String name) {
		if(WAVE_TYPE_MAP.containsKey(name)) {
			try {
				return WAVE_TYPE_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if(! name.equals("")){
			CRaid.LOGGER.warn("Wave Type Missing : can not find {}", name);
		}
		return new WaveComponent();
	}

	/**
	 * get spawn component by type name.
	 */
	@Nullable
	public static ISpawnComponent getSpawnType(String name) {
		if(SPAWN_TYPE_MAP.containsKey(name)) {
			try {
				return SPAWN_TYPE_MAP.get(name).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if(! name.equals("")){
			CRaid.LOGGER.warn("Spawn Type Missing : can not find {}", name);
		}
		return new SpawnComponent();
	}

}
