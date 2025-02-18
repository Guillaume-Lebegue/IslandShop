package world.bentobox.upgrades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.limits.objects.IslandBlockCount;
import world.bentobox.upgrades.config.Settings;

public class UpgradesManager {

	public UpgradesManager(UpgradesAddon addon) {
		this.addon = addon;
		this.hookedGameModes = new HashSet<>();
	}

	protected void addGameModes(List<String> gameModes) {
		this.hookedGameModes.addAll(gameModes);
	}

	public boolean canOperateInWorld(World world) {
		Optional<GameModeAddon> addon = this.addon.getPlugin().getIWM().getAddon(world);

		return addon.isPresent() && this.hookedGameModes.contains(addon.get().getDescription().getName());
	}

	public int getIslandLevel(Island island) {
		if (!this.addon.isLevelProvided())
			return 0;

		if (island == null) {
			this.addon.logError("Island couldn't be found");
			return 0;
		}

		int islandLevel = (int) this.addon.getLevelAddon().getIslandLevel(island.getWorld(), island.getOwner());

		if (islandLevel < 0) {
			this.addon.logWarning("Island " + island.getUniqueId() + " has an invalid level: " + islandLevel);
			islandLevel = 0;
		}

		return islandLevel;
	}

	public List<Settings.UpgradeTier> getAllRangeUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null)
			return Collections.emptyList();

		Map<String, Settings.UpgradeTier> defaultTiers = this.addon.getSettings().getDefaultRangeUpgradeTierMap();
		Map<String, Settings.UpgradeTier> customAddonTiers = this.addon.getSettings().getAddonRangeUpgradeTierMap(name);

		List<Settings.UpgradeTier> tierList;

		if (customAddonTiers.isEmpty())
			tierList = new ArrayList<>(defaultTiers.values());
		else {
			Set<String> uniqueIDSet = new HashSet<>(customAddonTiers.keySet());
			uniqueIDSet.addAll(defaultTiers.keySet());
			tierList = new ArrayList<>(uniqueIDSet.size());

			uniqueIDSet.forEach(id -> tierList.add(customAddonTiers.getOrDefault(id, defaultTiers.get(id))));
		}

		if (tierList.isEmpty())
			return Collections.emptyList();

		tierList.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel));

		return tierList;
	}

	public Map<Material, List<Settings.UpgradeTier>> getAllBlockLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<Material, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultBlockLimitsUpgradeTierMap();
		Map<Material, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonBlockLimitsUpgradeTierMap(name);

		Map<Material, List<Settings.UpgradeTier>> tierList = new EnumMap<>(Material.class);

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((mat, tiers) -> tierList.put(mat, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((mat, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(mat))
					uniqueIDSet.addAll(defaultTiers.get(mat).keySet());
				List<Settings.UpgradeTier> matTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> matTier.add(tiers.getOrDefault(id, defaultTiers.get(mat).get(id))));
				tierList.put(mat, matTier);
			});

			defaultTiers.forEach((mat, tiers) -> tierList.putIfAbsent(mat, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((mat, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

	public Map<EntityType, List<Settings.UpgradeTier>> getAllEntityLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<EntityType, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultEntityLimitsUpgradeTierMap();
		Map<EntityType, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonEntityLimitsUpgradeTierMap(name);

		Map<EntityType, List<Settings.UpgradeTier>> tierList = new EnumMap<>(EntityType.class);

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((ent, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(ent))
					uniqueIDSet.addAll(defaultTiers.get(ent).keySet());
				List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent).get(id))));
				tierList.put(ent, entTier);
			});

			defaultTiers.forEach((ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

	public Map<String, List<Settings.UpgradeTier>> getAllEntityGroupLimitsUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Settings.UpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultEntityGroupLimitsUpgradeTierMap();
		Map<String, Map<String, Settings.UpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonEntityGroupLimitsUpgradeTierMap(name);

		Map<String, List<Settings.UpgradeTier>> tierList = new TreeMap<>();

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((ent, tiers) -> tierList.put(ent, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((ent, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(ent))
					uniqueIDSet.addAll(defaultTiers.get(ent).keySet());
				List<Settings.UpgradeTier> entTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> entTier.add(tiers.getOrDefault(id, defaultTiers.get(ent).get(id))));
				tierList.put(ent, entTier);
			});

			defaultTiers.forEach((ent, tiers) -> tierList.putIfAbsent(ent, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((ent, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

	public Map<String, List<Settings.CommandUpgradeTier>> getAllCommandUpgradeTiers(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		if (name == null) {
			return Collections.emptyMap();
		}

		Map<String, Map<String, Settings.CommandUpgradeTier>> defaultTiers = this.addon.getSettings()
				.getDefaultCommandUpgradeTierMap();
		Map<String, Map<String, Settings.CommandUpgradeTier>> customAddonTiers = this.addon.getSettings()
				.getAddonCommandUpgradeTierMap(name);

		Map<String, List<Settings.CommandUpgradeTier>> tierList = new TreeMap<>();

		if (customAddonTiers.isEmpty()) {
			defaultTiers.forEach((cmd, tiers) -> tierList.put(cmd, new ArrayList<>(tiers.values())));
		} else {
			customAddonTiers.forEach((cmd, tiers) -> {
				Set<String> uniqueIDSet = new HashSet<>(tiers.keySet());
				if (defaultTiers.containsKey(cmd))
					uniqueIDSet.addAll(defaultTiers.get(cmd).keySet());
				List<Settings.CommandUpgradeTier> cmdTier = new ArrayList<>(uniqueIDSet.size());

				uniqueIDSet.forEach(id -> cmdTier.add(tiers.getOrDefault(id, defaultTiers.get(cmd).get(id))));
				tierList.put(cmd, cmdTier);
			});

			defaultTiers.forEach((cmd, tiers) -> tierList.putIfAbsent(cmd, new ArrayList<>(tiers.values())));
		}

		if (tierList.isEmpty()) {
			return Collections.emptyMap();
		}

		tierList.forEach((cmd, tiers) -> tiers.sort(Comparator.comparingInt(Settings.UpgradeTier::getMaxLevel)));

		return tierList;
	}

	public Settings.UpgradeTier getRangeUpgradeTier(int rangeLevel, World world) {
		List<Settings.UpgradeTier> tierList = this.getAllRangeUpgradeTiers(world);

		if (tierList.isEmpty())
			return null;

		Settings.UpgradeTier rangeUpgradeTier = tierList.get(0);

		if (rangeUpgradeTier.getMaxLevel() < 0)
			return rangeUpgradeTier;

		for (int i = 0; i < tierList.size(); i++) {
			if (rangeLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	public Settings.UpgradeTier getBlockLimitsUpgradeTier(Material mat, int limitsLevel, World world) {
		Map<Material, List<Settings.UpgradeTier>> matTierList = this.getAllBlockLimitsUpgradeTiers(world);

		if (matTierList.isEmpty()) {
			return null;
		}

		if (!matTierList.containsKey(mat)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = matTierList.get(mat);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}
		return null;
	}

	public Settings.UpgradeTier getEntityLimitsUpgradeTier(EntityType ent, int limitsLevel, World world) {
		Map<EntityType, List<Settings.UpgradeTier>> entTierList = this.getAllEntityLimitsUpgradeTiers(world);

		if (entTierList.isEmpty()) {
			return null;
		}

		if (!entTierList.containsKey(ent)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = entTierList.get(ent);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	public Settings.UpgradeTier getEntityGroupLimitsUpgradeTier(String group, int limitsLevel, World world) {
		Map<String, List<Settings.UpgradeTier>> entTierList = this.getAllEntityGroupLimitsUpgradeTiers(world);

		if (entTierList.isEmpty()) {
			return null;
		}

		if (!entTierList.containsKey(group)) {
			return null;
		}

		List<Settings.UpgradeTier> tierList = entTierList.get(group);

		for (int i = 0; i < tierList.size(); i++) {
			if (limitsLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	public Settings.CommandUpgradeTier getCommandUpgradeTier(String cmd, int cmdLevel, World world) {
		Map<String, List<Settings.CommandUpgradeTier>> cmdTierList = this.getAllCommandUpgradeTiers(world);

		if (cmdTierList.isEmpty()) {
			return null;
		}

		if (!cmdTierList.containsKey(cmd)) {
			return null;
		}

		List<Settings.CommandUpgradeTier> tierList = cmdTierList.get(cmd);

		for (int i = 0; i < tierList.size(); i++) {
			if (cmdLevel <= tierList.get(i).getMaxLevel())
				return tierList.get(i);
		}

		return null;
	}

	public Map<String, Integer> getRangeUpgradeInfos(int rangeLevel, int islandLevel, int numberPeople, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return null;

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) rangeUpgradeTier.calculateIslandMinLevel(rangeLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) rangeUpgradeTier.calculateVaultCost(rangeLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) rangeUpgradeTier.calculateUpgrade(rangeLevel, islandLevel, numberPeople));

		return info;
	}

	public int getRangePermissionLevel(int rangeLevel, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return 0;
		return rangeUpgradeTier.getPermissionLevel();
	}

	public String getRangeUpgradeTierName(int rangeLevel, World world) {
		Settings.UpgradeTier rangeUpgradeTier = this.getRangeUpgradeTier(rangeLevel, world);

		if (rangeUpgradeTier == null)
			return null;
		return rangeUpgradeTier.getTierName();
	}

	public int getRangeUpgradeMax(World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxRangeUpgrade(name);
	}

	public Map<String, Integer> getBlockLimitsUpgradeInfos(Material mat, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	public int getBlockLimitsPermissionLevel(Material mat, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	public String getBlockLimitsUpgradeTierName(Material mat, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getBlockLimitsUpgradeTier(mat, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	public int getBlockLimitsUpgradeMax(Material mat, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxBlockLimitsUpgrade(mat, name);
	}

	public Map<String, Integer> getEntityLimitsUpgradeInfos(EntityType ent, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	public Map<String, Integer> getEntityGroupLimitsUpgradeInfos(String group, int limitsLevel, int islandLevel,
			int numberPeople, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);
		if (limitsUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel",
				(int) limitsUpgradeTier.calculateIslandMinLevel(limitsLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) limitsUpgradeTier.calculateVaultCost(limitsLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) limitsUpgradeTier.calculateUpgrade(limitsLevel, islandLevel, numberPeople));

		return info;
	}

	public int getEntityLimitsPermissionLevel(EntityType ent, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	public int getEntityGroupLimitsPermissionLevel(String group, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return 0;
		return limitsUpgradeTier.getPermissionLevel();
	}

	public String getEntityLimitsUpgradeTierName(EntityType ent, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityLimitsUpgradeTier(ent, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	public String getEntityGroupLimitsUpgradeTierName(String group, int limitsLevel, World world) {
		Settings.UpgradeTier limitsUpgradeTier = this.getEntityGroupLimitsUpgradeTier(group, limitsLevel, world);

		if (limitsUpgradeTier == null)
			return null;
		return limitsUpgradeTier.getTierName();
	}

	public int getEntityLimitsUpgradeMax(EntityType ent, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxEntityLimitsUpgrade(ent, name);
	}

	public int getEntityGroupLimitsUpgradeMax(String group, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxEntityGroupLimitsUpgrade(group, name);
	}

	public Map<String, Integer> getCommandUpgradeInfos(String cmd, int cmdLevel, int islandLevel, int numberPeople,
			World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);
		if (cmdUpgradeTier == null) {
			return null;
		}

		Map<String, Integer> info = new TreeMap<>();

		info.put("islandMinLevel", (int) cmdUpgradeTier.calculateIslandMinLevel(cmdLevel, islandLevel, numberPeople));
		info.put("vaultCost", (int) cmdUpgradeTier.calculateVaultCost(cmdLevel, islandLevel, numberPeople));
		info.put("upgrade", (int) cmdUpgradeTier.calculateUpgrade(cmdLevel, islandLevel, numberPeople));

		return info;
	}

	public int getCommandPermissionLevel(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return 0;
		return cmdUpgradeTier.getPermissionLevel();
	}

	public String getCommandUpgradeTierName(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return null;
		return cmdUpgradeTier.getTierName();
	}

	public int getCommandUpgradeMax(String cmd, World world) {
		String name = this.addon.getPlugin().getIWM().getAddon(world).map(a -> a.getDescription().getName())
				.orElse(null);
		return this.addon.getSettings().getMaxCommandUpgrade(cmd, name);
	}

	public List<String> getCommandList(String cmd, int cmdLevel, Island island, String playerName) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, island.getWorld());

		if (cmdUpgradeTier == null)
			return Collections.emptyList();
		return cmdUpgradeTier.getCommandList(playerName, island, cmdLevel);
	}

	public Boolean isCommantConsole(String cmd, int cmdLevel, World world) {
		Settings.CommandUpgradeTier cmdUpgradeTier = this.getCommandUpgradeTier(cmd, cmdLevel, world);

		if (cmdUpgradeTier == null)
			return false;
		return cmdUpgradeTier.getConsole();
	}

	public Map<EntityType, Integer> getEntityLimits(Island island) {
		if (!this.addon.isLimitsProvided())
			return Collections.emptyMap();

		Map<EntityType, Integer> entityLimits = new TreeMap<>(this.addon.getLimitsAddon().getSettings().getLimits());
		IslandBlockCount ibc = this.addon.getLimitsAddon().getBlockLimitListener().getIsland(island.getUniqueId());
		if (ibc != null)
			ibc.getEntityLimits().forEach(entityLimits::put);
		return entityLimits;
	}

	public Map<String, Integer> getEntityGroupLimits(Island island) {
		if (!this.addon.isLimitsProvided())
			return Collections.emptyMap();

		Map<String, Integer> entityGroupLimits = new TreeMap<>(
				this.addon.getLimitsAddon().getSettings().getGroupLimits().values().stream().flatMap(e -> e.stream())
						.distinct().collect(Collectors.toMap(e -> e.getName(), e -> e.getLimit())));
		IslandBlockCount ibc = this.addon.getLimitsAddon().getBlockLimitListener().getIsland(island.getUniqueId());
		if (ibc != null)
			ibc.getEntityGroupLimits().forEach(entityGroupLimits::put);
		return entityGroupLimits;
	}

	private UpgradesAddon addon;

	private Set<String> hookedGameModes;

}
