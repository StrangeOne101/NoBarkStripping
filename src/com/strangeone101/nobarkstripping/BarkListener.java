package com.strangeone101.nobarkstripping;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarkListener implements Listener {
	
	private List<Player> clickers = new ArrayList<Player>();
	
	private static final Material[] LOGS = {Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.ACACIA_LOG,
			Material.JUNGLE_LOG, Material.DARK_OAK_LOG, Material.OAK_WOOD, Material.BIRCH_WOOD, Material.SPRUCE_WOOD,
			Material.ACACIA_WOOD, Material.JUNGLE_WOOD, Material.DARK_OAK_WOOD};
	private static final Material[] AXES = {Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE,
			Material.DIAMOND_AXE};
	
	public BarkListener() {
		addRecipe(Material.OAK_LOG, Material.STRIPPED_OAK_LOG);
		addRecipe(Material.BIRCH_LOG, Material.STRIPPED_BIRCH_LOG);
		addRecipe(Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG);
		addRecipe(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG);
		addRecipe(Material.ACACIA_LOG, Material.STRIPPED_ACACIA_LOG);
		addRecipe(Material.DARK_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG);
		addRecipe(Material.OAK_WOOD, Material.STRIPPED_OAK_WOOD);
		addRecipe(Material.BIRCH_WOOD, Material.STRIPPED_BIRCH_WOOD);
		addRecipe(Material.SPRUCE_WOOD, Material.STRIPPED_SPRUCE_WOOD);
		addRecipe(Material.JUNGLE_WOOD, Material.STRIPPED_JUNGLE_WOOD);
		addRecipe(Material.ACACIA_WOOD, Material.STRIPPED_ACACIA_WOOD);
		addRecipe(Material.DARK_OAK_WOOD, Material.STRIPPED_DARK_OAK_WOOD);
	}
	
	/**
	 * Adds a recipe to craft stripped logs
	 * @param log
	 * @param strippedLog
	 */
	private void addRecipe(Material log, Material strippedLog) {
		for (int i = 0; i < AXES.length; i++) {
			ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(NoBarkStripping.INSTANCE, strippedLog.name() + i), new ItemStack(strippedLog));
			recipe.addIngredient(log);
			recipe.addIngredient(AXES[i]);
			recipe.setGroup("logStrippingRecipe");
			NoBarkStripping.INSTANCE.getServer().addRecipe(recipe);
		}
	}
	
	@EventHandler
	public void onStrip(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (event.getItem() == null || event.getClickedBlock() == null) return;
		if (!isAxe(event.getItem().getType())) return; //They aren't holding an axe
		if (!Arrays.asList(LOGS).contains(event.getClickedBlock().getType())) return; //They didn't click a log
		
		event.setCancelled(true);
		
		if (clickers.contains(event.getPlayer())) {
			event.getPlayer().sendMessage(Config.getMessage());
			return;
		} else {
			clickers.add(event.getPlayer());
			new BukkitRunnable() { //Set an alert to the player if they keep doing it for over 2s
				@Override
				public void run() {
					if (clickers.contains(event.getPlayer())) {
						clickers.remove(event.getPlayer());
					}
				}
			}.runTaskLater(NoBarkStripping.INSTANCE, (long) (20 * 5)); //5 second cooldown
		}
	}
	
	@EventHandler
	public void onCraft(CraftItemEvent event) {
		if (event.getRecipe() instanceof ShapelessRecipe && ((ShapelessRecipe)event.getRecipe()).getGroup().equals("logStrippingRecipe")) {
			saveTheAxe(event);
		}
	}
	
	private boolean isAxe(Material material) {
		return Arrays.asList(AXES).contains(material);
	}

	/**
	 * This method makes the axe remain in the crafting grid when used to craft the stripped logs. In vanilla,
	 * items that are not consumed in recipes are defined in the items class file and not in the recipe themselves,
	 * so therefore we have to replace the axe once it is consumed 1 tick later.
	 * @param event The craft item event
	 */
	private void saveTheAxe(CraftItemEvent event) {
		//events.add(event);
		
		int axe = -1;
		ItemStack axeStack = new ItemStack(Material.BLAZE_ROD);
		for (int slot = 0; slot < event.getInventory().getSize(); slot++) {
			if (event.getInventory().getItem(slot) != null && isAxe(event.getInventory().getItem(slot).getType())) {
				axe = slot;
				axeStack = event.getInventory().getItem(slot);
				break;
			}
		}
		
		if (axe == -1) {
			NoBarkStripping.INSTANCE.getLogger().warning("Failed to find axe in recipe!");
			return;
		}
		
		final int axeSlot = axe;
		final ItemStack axeStackF = axeStack.clone();

		if (event.isShiftClick()) { //If we don't set it to 64, the axe will be consumed, then there will be 0 axes
			axeStack.setAmount(64); //there in the same tick, so therefore the shift click will only give 1x result
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if (event.getInventory() == null) { //If by some chance, it gets closed in the same tick (plugin or something)
					event.getWhoClicked().getInventory().addItem(axeStackF);
					return;
				}
				event.getInventory().setItem(axeSlot, axeStackF);
			}
		}.runTaskLater(NoBarkStripping.INSTANCE, 1); //Run next tick
	}
}
