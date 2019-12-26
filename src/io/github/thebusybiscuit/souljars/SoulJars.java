package io.github.thebusybiscuit.souljars;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.bstats.bukkit.Metrics;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.BukkitUpdater;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.GitHubBuildsUpdater;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.Updater;

public class SoulJars extends JavaPlugin implements Listener {

	private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19";
	private final Map<EntityType, Integer> mobs = new EnumMap<>(EntityType.class);
	
	private Config cfg;
	private Category category;
	private RecipeType recipeType;
	private SlimefunItemStack jar;

	@Override
	public void onEnable() {
		cfg = new Config(this);
		
		// Setting up bStats
		new Metrics(this);

		// Setting up the Auto-Updater
		Updater updater;

		if (!getDescription().getVersion().startsWith("DEV - ")) {
			// We are using an official build, use the BukkitDev Updater
			updater = new BukkitUpdater(this, getFile(), 101706);
		}
		else {
			// If we are using a development build, we want to switch to our custom 
			updater = new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/SoulJars/master");
		}

		// Only run the Updater if it has not been disabled
		if (cfg.getBoolean("options.auto-update")) updater.start();

		jar = new SlimefunItemStack("SOUL_JAR", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19", "&bSoul Jar &7(Empty)", "", "&rKill a Mob while having this", "&rItem in your Inventory to bind", "&rtheir Soul to this Jar");
		category = new Category(new CustomItem(jar, "&bSoul Jars", "", "&a> Click to open"));
		recipeType = new RecipeType(new CustomItem(Material.DIAMOND_SWORD, "&cKill the specified Mob", "&cwhile having an empty Soul Jar", "&cin your Inventory"));
		
		new SlimefunItem(category, jar, RecipeType.ANCIENT_ALTAR, new ItemStack[] {
				SlimefunItems.RUNE_EARTH, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_WATER, 
				new ItemStack(Material.SOUL_SAND), SlimefunItems.NECROTIC_SKULL, new ItemStack(Material.SOUL_SAND), 
				SlimefunItems.RUNE_AIR, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_FIRE
		}, new CustomItem(jar, 3))
		.register();

		getServer().getPluginManager().registerEvents(this, this);

		for (String mob : cfg.getStringList("mobs")) {
			try {
				EntityType type = EntityType.valueOf(mob);
				registerSoul(mob, type);
			} catch(Exception x) {
				getLogger().log(Level.SEVERE, "An Exception was thrown for the (maybe invalid) Mob Type: " + mob, x);
			}
		}
	}

	private void registerSoul(String mob, EntityType type) {
		int souls = cfg.getOrSetDefault("souls-required." + type.toString(), 128);
		mobs.put(type, souls);
		
		Material m = Material.getMaterial(type.toString() + "_SPAWN_EGG");
		if (m == null) m = Material.ZOMBIE_SPAWN_EGG;
		
		new SlimefunItem(category, new SlimefunItemStack(mob + "_SOUL_JAR", TEXTURE, "&cSoul Jar &7(" + format(mob) + ")", "", "&7Infused Souls: &e1"), mob + "_SOUL_JAR", recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill " + souls + "x " + format(mob)), null, null, null}, true)
		.register();
		
		new FilledJar(category, new SlimefunItemStack("FILLED_" + mob + "_SOUL_JAR", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19", "&cFilled Soul Jar &7(" + format(mob) + ")", "", "&7Infused Souls: &e" + souls), recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill " + souls + "x " + format(mob)), null, null, null})
		.register();

		new SlimefunItem(category, new SlimefunItemStack(mob + "_BROKEN_SPAWNER", Material.SPAWNER, "&cBroken Spawner", "&7Type: &b" + format(mob), "", "&cFractured, must be repaired in an Ancient Altar"), RecipeType.ANCIENT_ALTAR,
		new ItemStack[] {new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, SlimefunItem.getItem("FILLED_" + mob + "_SOUL_JAR"), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS)})
		.register();
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (!mobs.containsKey(e.getEntityType()))
			return;

		Player killer = e.getEntity().getKiller();
		
		if (killer == null)
			return;

		String jarType = e.getEntity().getType().toString() + "_SOUL_JAR";
		
		for (int slot = 0; slot < killer.getInventory().getSize(); slot++) {
			ItemStack stack = killer.getInventory().getItem(slot);
			
			if (stack != null && SlimefunManager.isItemSimilar(stack, SlimefunItem.getItem(jarType), false)) {
				List<String> lore = stack.getItemMeta().getLore();
				int souls = Integer.parseInt(ChatColor.stripColor(lore.get(1)).split(": ")[1]) + 1, requiredSouls = mobs.get(e.getEntityType());

				if (souls >= requiredSouls) {
					if (stack.getAmount() > 1) {
						stack.setAmount(stack.getAmount() - 1);
						killer.getInventory().addItem(SlimefunItem.getItem("FILLED_" + jarType));
					} 
					else {
						killer.getInventory().setItem(slot, SlimefunItem.getItem("FILLED_" + jarType));
					}
				} 
				else {
					lore.set(1, ChatColor.translateAlternateColorCodes('&', lore.get(1).split(": ")[0] + ": &e" + souls));
					
					if (stack.getAmount() > 1) {
						stack.setAmount(stack.getAmount() - 1);
						stack = stack.clone();
						stack.setAmount(1);
						ItemMeta im = stack.getItemMeta();
						im.setLore(lore);
						stack.setItemMeta(im);
						killer.getInventory().addItem(stack);
					} 
					else {
						ItemMeta im = stack.getItemMeta();
						im.setLore(lore);
						stack.setItemMeta(im);
					}
				}

				return;
			}
		}

		for (int slot = 0; slot < killer.getInventory().getSize(); slot++) {
			ItemStack stack = killer.getInventory().getItem(slot);
			
			if (stack != null && SlimefunManager.isItemSimilar(stack, jar, false)) {
				stack.setAmount(stack.getAmount() - 1);
				killer.getWorld().dropItemNaturally(e.getEntity().getLocation(), SlimefunItem.getItem(jarType));
				return;
			}
		}
	}
	
	private static String format(String string) {
		string = string.toLowerCase();
		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		for (String s : string.split("_")) {
			if (i == 0) builder.append(Character.toUpperCase(s.charAt(0)) + s.substring(1));
			else builder.append(" " + Character.toUpperCase(s.charAt(0)) + s.substring(1));
			i++;
		}
		return builder.toString();
	}

}
