package me.mrCookieSlime.SoulJars;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Player.PlayerInventory;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;

public class SoulJars extends JavaPlugin implements Listener {

	private Set<EntityType> mobs = new HashSet<EntityType>();
	private Category category;
	private RecipeType recipeType;
	private ItemStack jar;
	private int required_souls;

	@Override
	public void onEnable() {
		PluginUtils utils = new PluginUtils(this);
		utils.setupConfig();
		
		Config cfg = utils.getConfig();

		required_souls = cfg.getInt("required-souls");

		try {
			category = new Category(new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&bSoul Jars", "", "&a> Click to open"));
			recipeType = new RecipeType(new CustomItem(Material.PLAYER_HEAD, "&cKill the specified Mob", "&cwhile having an empty Soul Jar", "&cin your Inventory"));
			jar = new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&bSoul Jar &7(Empty)", "", "&rKill a Mob while having this", "&rItem in your Inventory to bind", "&rit's Soul to this Jar");
		} catch (Exception e) {
			e.printStackTrace();
		}

		new SlimefunItem(category, jar, "SOUL_JAR", RecipeType.ANCIENT_ALTAR,
		new ItemStack[] {SlimefunItems.RUNE_EARTH, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_WATER, new ItemStack(Material.SOUL_SAND), SlimefunItems.NECROTIC_SKULL, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_AIR, new ItemStack(Material.SOUL_SAND), SlimefunItems.RUNE_FIRE},
		new CustomItem(jar, 3))
		.register();

		getServer().getPluginManager().registerEvents(this, this);

		for (String mob : cfg.getStringList("mobs")) {
			try {
				EntityType type = EntityType.valueOf(mob);
				registerSoul(mob, type);
			} catch(Exception x) {
				System.err.println("[SoulJars] " + x.getClass().getName() + " for (maybe invalid) Mob Type: " + mob);
			}
		}
	}

	private void registerSoul(String mob, EntityType type) throws Exception {
		mobs.add(type);

		Material m = Material.getMaterial(type.toString() + "_SPAWN_EGG");
		if (m == null) m = Material.ZOMBIE_SPAWN_EGG;
		
		new SlimefunItem(category, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&bSoul Jar &7(" + StringUtils.format(mob) + ")", "", "&7Infused Souls: &e0"), mob + "_SOUL_JAR", recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill 1x " + StringUtils.format(mob)), null, null, null})
		.register();

		new SlimefunItem(category, new CustomItem(CustomSkull.getItem("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQxYzc3N2VlMTY2YzQ3Y2FlNjk4YWU2Yjc2OWRhNGUyYjY3ZjQ2ODg1NTMzMGFkN2JkZGQ3NTFjNTI5M2YifX19"), "&cSoul Jar &7(" + StringUtils.format(mob) + ")", "", "&7Infused Souls: &e" + required_souls), mob + "_SOUL_JAR_2", recipeType,
		new ItemStack[] {null, null, null, jar, null, new CustomItem(m, "&rKill " + required_souls + "x " + StringUtils.format(mob)), null, null, null})
		.register();

		new SlimefunItem(category, new CustomItem(Material.SPAWNER, "&cBroken Spawner", "&7Type: &b" + StringUtils.format(mob), "", "&cFractured, must be repaired in an Ancient Altar"), mob + "_BROKEN_SPAWNER", RecipeType.ANCIENT_ALTAR,
		new ItemStack[] {new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, SlimefunItem.getItem(mob + "_SOUL_JAR_2"), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS), SlimefunItems.RUNE_EARTH, new ItemStack(Material.IRON_BARS)})
		.register();
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if (!mobs.contains(e.getEntity().getType()))
			return;

		Player killer = e.getEntity().getKiller();
		if (killer instanceof Player) {
			for (int slot = 0; slot < killer.getInventory().getSize(); slot++) {
				ItemStack stack = killer.getInventory().getItem(slot);
				if (SlimefunManager.isItemSimiliar(stack, SlimefunItem.getItem(e.getEntity().getType().toString() + "_SOUL_JAR"), false)) {
					List<String> lore = stack.getItemMeta().getLore();
					int souls = Integer.parseInt(ChatColor.stripColor(lore.get(1)).split(": ")[1]);
					souls++;
					
					if (souls >= required_souls) {
						killer.getInventory().setItem(slot, SlimefunItem.getItem(e.getEntity().getType().toString() + "_SOUL_JAR_2"));
					}
					else {
						lore.set(1, ChatColor.translateAlternateColorCodes('&', lore.get(1).split(": ")[0] + ": &e" + souls));
						ItemMeta im = stack.getItemMeta();
						im.setLore(lore);
						stack.setItemMeta(im);
						killer.getInventory().setItem(slot, stack);
					}
					PlayerInventory.update(killer);
					return;
				}
			}

			if (killer.getInventory().containsAtLeast(jar, 1)) {
				killer.getInventory().removeItem(new CustomItem(jar, 1));
				ItemStack stack = SlimefunItem.getItem(e.getEntity().getType().toString() + "_SOUL_JAR");
				List<String> lore = stack.getItemMeta().getLore();
				lore.set(1, ChatColor.translateAlternateColorCodes('&', lore.get(1).split(": ")[0] + ": &e" + 1));
				ItemMeta im = stack.getItemMeta();
				im.setLore(lore);
				stack.setItemMeta(im);
				killer.getWorld().dropItemNaturally(killer.getEyeLocation(), stack);
			}
		}
	}

}
