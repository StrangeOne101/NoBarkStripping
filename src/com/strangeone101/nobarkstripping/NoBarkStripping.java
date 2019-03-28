package com.strangeone101.nobarkstripping;

import org.bukkit.plugin.java.JavaPlugin;

public class NoBarkStripping extends JavaPlugin {

	public static NoBarkStripping INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;

		Config.load();

		getServer().getPluginManager().registerEvents(new BarkListener(), this);

		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled!");
	}
}
