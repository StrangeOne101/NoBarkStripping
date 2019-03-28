package com.strangeone101.nobarkstripping;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Config {

	private static String message = "&cYou must craft logs and an axe together if you want to strip logs!";

	private static FileConfiguration getConfig() {
		if (!new File(NoBarkStripping.INSTANCE.getDataFolder(), "config.yml").exists()) {
			NoBarkStripping.INSTANCE.saveDefaultConfig();
		}
		return NoBarkStripping.INSTANCE.getConfig();
	}

	public static void load() {
		FileConfiguration config = getConfig();

		message = ChatColor.translateAlternateColorCodes('&', config.getString("attempted-strip-message", message));
	}

	public static String getMessage() {
		return message;
	}
}
