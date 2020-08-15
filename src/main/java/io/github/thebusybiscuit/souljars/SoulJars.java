package io.github.thebusybiscuit.souljars;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.bstats.bukkit.Metrics;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.GitHubBuildsUpdater;
import me.mrCookieSlime.Slimefun.cscorelib2.updater.Updater;

public class SoulJars extends JavaPlugin implements Listener, SlimefunAddon {

    private static final String JAR_TEXTURE = "bd1c777ee166c47cae698ae6b769da4e2b67f468855330ad7bddd751c5293f";
    private final Map<EntityType, Integer> mobs = new EnumMap<>(EntityType.class);

    private Config cfg;
    private Category category;
    private RecipeType recipeType;
    private SlimefunItemStack emptyJar;

    @Override
    public void onEnable() {
        cfg = new Config(this);

        // Setting up bStats
        new Metrics(this, 5581);

        if (getDescription().getVersion().startsWith("DEV - ")) {
            Updater updater = new GitHubBuildsUpdater(this, getFile(), "TheBusyBiscuit/SoulJars/master");

            // Only run the Updater if it has not been disabled
            if (cfg.getBoolean("options.auto-update")) {
                updater.start();
            }
        }

        emptyJar = new SlimefunItemStack("SOUL_JAR", JAR_TEXTURE, "&bSoul Jar &7(Empty)", "", "&rKill a Mob while having this", "&rItem in your Inventory to bind", "&rtheir Soul to this Jar");
        category = new Category(new NamespacedKey(this, "soul_jars"), new CustomItem(emptyJar, "&bSoul Jars", "", "&a> Click to open"));
        recipeType = new RecipeType(new NamespacedKey(this, "mob_killing"), new CustomItem(Material.DIAMOND_SWORD, "&cKill the specified Mob", "&cwhile having an empty Soul Jar", "&cin your Inventory"));

        new SlimefunItem(category, emptyJar, RecipeType.ANCIENT_ALTAR, new ItemStack[] { SlimefunItems.EARTH_RUNE, new ItemStack(Material.SOUL_SAND), SlimefunItems.WATER_RUNE, new ItemStack(Material.SOUL_SAND), SlimefunItems.NECROTIC_SKULL, new ItemStack(Material.SOUL_SAND), SlimefunItems.AIR_RUNE, new ItemStack(Material.SOUL_SAND), SlimefunItems.FIRE_RUNE }, new CustomItem(emptyJar, 3)).register(this);
        new JarsListener(this);

        for (String mob : cfg.getStringList("mobs")) {
            try {
                EntityType type = EntityType.valueOf(mob);
                registerSoul(type);
            }
            catch (Exception x) {
                getLogger().log(Level.SEVERE, "{0}: Possibly invalid mob type: {1}", new Object[] { x.getClass().getSimpleName(), mob });
            }
        }

        cfg.save();
    }

    private void registerSoul(EntityType type) {
        String name = ChatUtils.humanize(type.name());

        int souls = cfg.getOrSetDefault("souls-required." + type.toString(), 128);
        mobs.put(type, souls);

        Material mobEgg = Material.getMaterial(type.toString() + "_SPAWN_EGG");

        if (mobEgg == null) {
            mobEgg = Material.ZOMBIE_SPAWN_EGG;
        }

        SlimefunItemStack jarItem = new SlimefunItemStack(type.name() + "_SOUL_JAR", JAR_TEXTURE, "&cSoul Jar &7(" + name + ")", "", "&7Infused Souls: &e1");
        SlimefunItem jar = new UnplaceableBlock(category, jarItem, recipeType, new ItemStack[] { null, null, null, emptyJar, null, new CustomItem(mobEgg, "&rKill " + souls + "x " + name), null, null, null });
        jar.register(this);

        SlimefunItemStack filledJarItem = new SlimefunItemStack("FILLED_" + type.name() + "_SOUL_JAR", JAR_TEXTURE, "&cFilled Soul Jar &7(" + name + ")", "", "&7Infused Souls: &e" + souls);
        SlimefunItem filledJar = new FilledJar(category, filledJarItem, recipeType, new ItemStack[] { null, null, null, emptyJar, null, new CustomItem(mobEgg, "&rKill " + souls + "x " + name), null, null, null });
        filledJar.register(this);

        SlimefunItemStack spawnerItem = new SlimefunItemStack(type.toString() + "_BROKEN_SPAWNER", Material.SPAWNER, "&cBroken Spawner", "&7Type: &b" + name, "", "&cFractured, must be repaired in an Ancient Altar");
        new SlimefunItem(category, spawnerItem, RecipeType.ANCIENT_ALTAR, new ItemStack[] { new ItemStack(Material.IRON_BARS), SlimefunItems.EARTH_RUNE, new ItemStack(Material.IRON_BARS), SlimefunItems.EARTH_RUNE, filledJarItem, SlimefunItems.EARTH_RUNE, new ItemStack(Material.IRON_BARS), SlimefunItems.EARTH_RUNE, new ItemStack(Material.IRON_BARS) }, new SlimefunItemStack("BROKEN_SPAWNER", Material.SPAWNER, "&cBroken Spawner", "&7Type: &b" + name, "", "&cFractured, must be repaired in an Ancient Altar")).register(this);
    }

    public Map<EntityType, Integer> getRequiredSouls() {
        return mobs;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/TheBusyBiscuit/SoulJars/issues";
    }

}
