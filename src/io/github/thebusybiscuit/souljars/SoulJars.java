package io.github.thebusybiscuit.souljars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;
import me.mrCookieSlime.Slimefun.bstats.bukkit.Metrics;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.BukkitUpdater;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.GitHubBuildsUpdater;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.Updater;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;

public class SoulJars extends JavaPlugin implements Listener {

	private final Map<EntityType, Integer> mobs = new HashMap<>();
	
	private Config cfg;
	private Category category;
	private RecipeType recipeType;
	private ItemStack jar;

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

		try {
			category = new Category(new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&bSoul Jars", "", "&a> Click to open"));
			recipeType = new RecipeType(new CustomItem(Material.DIAMOND_SWORD, "&cKill the specified Mob", "&cwhile having an empty Soul Jar", "&cin your Inventory"));
			jar = new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&bSoul Jar &7(Empty)", "", "&rKill a Mob while having this", "&rItem in your Inventory to bind", "&rhis Soul to this Jar");
		} catch (Exception e) {
			e.printStackTrace();
		}

		new SlimefunItem(category, jar, "SOUL_JAR", RecipeType.ANCIENT_ALTAR,
		new ItemStack[] {SlimefunItems.RUNE_EARTH, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_WATER, new ItemStack(Material.SOUL_SAND), SlimefunItems.NECROTIC_SKULL, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_AIR, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_FIRE},
		new CustomItem(jar, 3))
		.register();

		getServer().getPluginManager().registerEvents(this, this);

		for (String mob: cfg.getStringList("mobs")) {
			try {
				EntityType type = EntityType.valueOf(mob);
				registerSoul(mob, type);
			} catch(Exception x) {
				System.err.println("[SoulJars] " + x.getClass().getName() + " for (maybe invalid) Mob Type: " + mob);
			}
		}
	}

	private void registerSoul(String mob, EntityType type) throws Exception {
		int souls = cfg.getOrSetDefault("souls-required." + type.toString(), 128);
		mobs.put(type, souls);
		
		Material m = Material.getMaterial(type.toString() + "_SPAWN_EGG");
		if (m == null) m = Material.ZOMBIE_SPAWN_EGG;

		new SlimefunItem(category, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&cSoul Jar &7(" + StringUtils.format(mob) + ")", "", "&7Infused Souls: &e1"), mob + "_SOUL_JAR", recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill " + souls + "x " + StringUtils.format(mob)), null, null, null}, true)
		.register();

		new SlimefunItem(category, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&cFilled Soul Jar &7(" + StringUtils.format(mob) + ")", "", "&7Infused Souls: &e" + souls), "FILLED_" + mob + "_SOUL_JAR", recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill " + souls + "x " + StringUtils.format(mob)), null, null, null})
		.register();

		new SlimefunItem(category, new CustomItem(Material.SPAWNER, "&cBroken Spawner", "&7Type: &b" + StringUtils.format(mob), "", "&cFractured, must be repaired in an Ancient Altar"), mob + "_BROKEN_SPAWNER", RecipeType.ANCIENT_ALTAR,
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
			if (SlimefunManager.isItemSimiliar(stack, SlimefunItem.getItem(jarType), false)) {
				List<String> lore = stack.getItemMeta().getLore();
				int souls = Integer.parseInt(ChatColor.stripColor(lore.get(1)).split(": ")[1]) + 1, requiredSouls = mobs.get(e.getEntityType());

				if (souls >= requiredSouls) {
					if (stack.getAmount() > 1) {
						stack.setAmount(stack.getAmount() - 1);
						killer.getInventory().addItem(SlimefunItem.getItem("FILLED_" + jarType));
					} else {
						killer.getInventory().setItem(slot, SlimefunItem.getItem("FILLED_" + jarType));
					}
				} else {
					lore.set(1, ChatColor.translateAlternateColorCodes('&', lore.get(1).split(": ")[0] + ": &e" + souls));
					if (stack.getAmount() > 1) {
						stack.setAmount(stack.getAmount() - 1);
						stack = stack.clone();
						stack.setAmount(1);
						ItemMeta im = stack.getItemMeta();
						im.setLore(lore);
						stack.setItemMeta(im);
						killer.getInventory().addItem(stack);
					} else {
						ItemMeta im = stack.getItemMeta();
						im.setLore(lore);
						stack.setItemMeta(im);
					}
				}

				return;
			}
		}

		if (killer.getInventory().containsAtLeast(jar, 1)) {
			killer.getInventory().removeItem(new CustomItem(jar, 1));
			killer.getWorld().dropItemNaturally(e.getEntity().getLocation(), SlimefunItem.getItem(jarType));
		}
	}

}
