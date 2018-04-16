package wiresegal.fabled.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import wiresegal.fabled.TraitManager;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author WireSegal
 * Created at 7:59 PM on 4/15/18.
 */
public class ModConfig {
    public static Predicate<Item> canReceiveTraits = ModConfig::canItemHaveTraits;

    public static boolean traitsFromLootOnly;

    private static File configFile;

    public static void injectConfigFile(File recommended) {
        String oldName = recommended.getName();
        String newName = oldName.substring(oldName.lastIndexOf('.')) + ".json";
        configFile = new File(recommended.getParentFile(), newName);
    }

    public static void init() {
        if (configFile.mkdirs()) {
            JsonConfig config = new JsonConfig(configFile);
            config.comment("Config for Fabled Works");
            generalSection(config.category("general"));
            whitelistSection(config.category("whitelist"));
            traitSection(config.category("traits"));
            config.save(configFile);
        }
    }


    private static boolean canItemHaveTraits(Item item) {
        return EnumEnchantmentType.ALL.canEnchantItem(item);
    }

    private static void generalSection(JsonConfig category) {
        category.comment("general");

        traitsFromLootOnly = category.get("Traits only come from loot", true);
    }

    private static void whitelistSection(JsonConfig category) {
        category.comment("whitelist",
                "You can explicitly define the whitelisted items by turning off `Allow Any`.",
                "Otherwise, the default behavior will be used, only applying to items that can receive enchantments.");

        JsonArray defaultWhitelist = new JsonArray();
        for (Item item : Item.REGISTRY) {
            if (canItemHaveTraits(item)) {
                ResourceLocation name = Item.REGISTRY.getNameForObject(item);
                if (name != null)
                    defaultWhitelist.add(name.toString());
            }
        }

        boolean allowAny = category.get("Allow Any", true);
        JsonArray whitelistedItems = category.get("Whitelist", defaultWhitelist);

        if (allowAny)
            canReceiveTraits = ModConfig::canItemHaveTraits;
        else {
            Set<Item> allowed = Sets.newHashSet();
            for (JsonElement el : whitelistedItems) {
                if (el.isJsonPrimitive()) {
                    ResourceLocation location = new ResourceLocation(el.getAsString());
                    Item item = Item.REGISTRY.getObject(location);
                    if (item != null)
                        allowed.add(item);
                }
            }

            canReceiveTraits = ImmutableSet.copyOf(allowed)::contains;
        }
    }


    private static void traitSection(JsonConfig category) {
        category.comment("traits",
                "Custom traits can be added as you want.",
                "Examples of all usable properties are provided below.");

        JsonArray defaultTraits = new JsonParser().parse(new InputStreamReader(
                ModConfig.class.getResourceAsStream("/assets/fabledworks/default_traits.json"))).getAsJsonArray();

        JsonArray traits = category.get("Traits", defaultTraits);

        TraitManager.purgeAllTraits();

        for (JsonElement traitEl : traits) {
            if (traitEl.isJsonObject()) {
                Trait trait = Trait.loadFromJson(traitEl.getAsJsonObject());
                if (trait != null)
                    TraitManager.registerTrait(trait);
            }
        }
    }
}
