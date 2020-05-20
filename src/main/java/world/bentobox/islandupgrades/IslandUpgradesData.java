package world.bentobox.islandupgrades;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;

public class IslandUpgradesData implements DataObject {

	@Expose
	private String uniqueId;
	
	@Expose
	private Map<String, Long> upgradesLevels;
	
	public IslandUpgradesData() {}
	
	public IslandUpgradesData(String uniqueId, Map<String, Long> upgradesLevel) {
		this.uniqueId = uniqueId;
		this.upgradesLevels = upgradesLevel;
	}
	
	public IslandUpgradesData(String uniqueId) {
		this(uniqueId, new HashMap<>());
	}
	
	@Override
	public String getUniqueId() {
		return uniqueId;
	}
	
	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public long getUpgradeLevel(String name) {
		this.upgradesLevels.putIfAbsent(name, (long) 0);
		return this.upgradesLevels.get(name);
	}
	
	public void setUpgradeLevel(String name, long value) {
		this.upgradesLevels.put(name, value);
	}
	
}
