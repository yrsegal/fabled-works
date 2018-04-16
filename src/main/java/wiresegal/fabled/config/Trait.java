package wiresegal.fabled.config;

import com.google.common.collect.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.fabled.EnumTraitLevel;
import wiresegal.fabled.TraitManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static wiresegal.fabled.FabledWorks.FABLED;

/**
 * @author WireSegal
 * Created at 4:20 PM on 4/15/18.
 */
public class Trait {
    @Nonnull
    private final List<String> conflicts;

    @Nonnull
    private final String location;

    @Nonnull
    private final String defaultName;

    @Nonnull
    private final Map<EnumTraitLevel, TraitAtLevel> levels; // new EnumMap<>(EnumTraitLevel.class)

    public Trait(@Nonnull List<String> conflicts, @Nonnull String location, @Nonnull String defaultName, @Nonnull Map<EnumTraitLevel, TraitAtLevel> levels) {
        this.conflicts = conflicts;
        this.location = location;
        this.defaultName = defaultName;
        this.levels = levels;
    }

    private static JsonObject getSubObject(String key, JsonObject obj) {
        if (!obj.has(key) || !obj.get(key).isJsonObject())
            return new JsonObject();

        return obj.getAsJsonObject(key);
    }

    private static JsonObject getSubObject(JsonObject obj, String... keys) {
        JsonObject current = obj;
        for (String key : keys)
            current = getSubObject(key, obj);
        return current;
    }

    private static final ImmutableMap<String, EntityEquipmentSlot> SLOTS =
            ImmutableMap.<String, EntityEquipmentSlot>builder()
                    .put("Main Hand", EntityEquipmentSlot.MAINHAND)
                    .put("Off Hand", EntityEquipmentSlot.OFFHAND)
                    .put("Head", EntityEquipmentSlot.HEAD)
                    .put("Body", EntityEquipmentSlot.CHEST)
                    .put("Legs", EntityEquipmentSlot.LEGS)
                    .put("Feet", EntityEquipmentSlot.FEET).build();

    @Nullable
    private static JsonElement resolveUpwards(JsonObject object, EnumTraitLevel traitLevel) {
        for (int i = traitLevel.ordinal(); i > 0; i--) {
            EnumTraitLevel trait = EnumTraitLevel.values()[i];
            if (object.has(trait.getJsonKey()))
                return object.get(trait.getJsonKey());
        }

        return null;
    }

    @Nullable
    public static Trait loadFromJson(JsonObject trait) {
        try {
            String name = trait.get("Name").getAsString();
            String defaultText = trait.get("Default Text").getAsString();
            List<String> conflicts = Lists.newArrayList();
            if (trait.has("Conflicts"))
                for (JsonElement el : trait.getAsJsonArray("Conflicts"))
                    conflicts.add(el.getAsString());

            List<String> attributesToApply = Lists.newArrayList();
            JsonObject attributeObject = getSubObject(trait, "Attributes");
            for (Map.Entry<String, JsonElement> element : attributeObject.entrySet())
                if (!element.getKey().startsWith("_"))
                    for (Map.Entry<String, JsonElement> location : trait.getAsJsonObject(element.getKey()).entrySet()) {
                        if (!location.getKey().startsWith("_")) {
                            attributesToApply.add(location.getKey());
                        }
                    }

            EnumMap<EnumTraitLevel, TraitAtLevel> atLevels = new EnumMap<>(EnumTraitLevel.class);

            int attId = 0;

            for (EnumTraitLevel level : EnumTraitLevel.LEVELS) {
                Map<EntityEquipmentSlot, Multimap<String, AttributeModifier>> allAttributes = Maps.newHashMap();
                double knockback = 1.0;
                double durability = 0.0;
                double penetration = 0.0;
                String command = null;

                for (Map.Entry<String, EntityEquipmentSlot> slot : SLOTS.entrySet()) {
                    Multimap<String, AttributeModifier> attributes = HashMultimap.create();
                    for (String attribute : attributesToApply) {
                        JsonElement attributeSet = resolveUpwards(getSubObject(trait, "Attributes", attribute, slot.getKey()), level);
                        if (attributeSet != null && attributeSet.isJsonObject()) {
                            JsonObject att = attributeSet.getAsJsonObject();
                            if (att.has("Add"))
                                attributes.put(attribute, new AttributeModifier(name + attId++, att.get("Add").getAsDouble(), 0));
                            if (att.has("Multiply"))
                                attributes.put(attribute, new AttributeModifier(name + attId++, att.get("Multiply").getAsDouble(), 0));
                            if (att.has("Exponential"))
                                attributes.put(attribute, new AttributeModifier(name + attId++, att.get("Exponential").getAsDouble(), 0));
                        }
                    }
                    if (!attributes.isEmpty())
                        allAttributes.put(slot.getValue(), attributes);
                }

                JsonElement knockbackElement = resolveUpwards(getSubObject(trait, "Knockback"), level);
                if (knockbackElement != null)
                    knockback = knockbackElement.getAsDouble();

                JsonElement durabilityElement = resolveUpwards(getSubObject(trait, "Durability"), level);
                if (durabilityElement != null)
                    durability = durabilityElement.getAsDouble();

                JsonElement penetrationElement = resolveUpwards(getSubObject(trait, "Penetration"), level);
                if (penetrationElement != null)
                    penetration = penetrationElement.getAsDouble();

                JsonElement commandElement = resolveUpwards(getSubObject(trait, "Command"), level);
                if (commandElement != null)
                    command = commandElement.getAsString();

                atLevels.put(level, new TraitAtLevel(durability, penetration, knockback, allAttributes, command));
            }

            return new Trait(conflicts, name, defaultText, atLevels);

        } catch (Throwable e){
            // NO-OP
        }

        return null;
    }


    public boolean canBeAppliedWith(@Nonnull String other) {
        if (other.equals(location) || conflicts.contains(other))
            return false;
        Trait otherTrait = TraitManager.lookupTrait(other);
        return otherTrait != null && !otherTrait.conflicts.contains(getName());
    }

    @Nonnull
    public String getName() {
        return location;
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        String key = FABLED + "." + getName() + ".name";

        if (I18n.hasKey(key))
            return I18n.format(key);
        return defaultName;
    }

    @Nonnull
    public String getNBTKey() {
        return getName();
    }

    public boolean isInCompound(@Nonnull NBTTagCompound compound) {
        return compound.hasKey(getNBTKey());
    }

    @Nonnull
    public EnumTraitLevel getLevelFromCompound(@Nonnull NBTTagCompound compound) {
        return isInCompound(compound) ? EnumTraitLevel.fromName(compound.getString(getNBTKey())) : EnumTraitLevel.NULL;
    }

    @Nullable
    private TraitAtLevel levelOn(@Nonnull ItemStack stack) {
        return traitAtLevel(TraitManager.getLevel(stack, this));
    }

    @Nullable
    private TraitAtLevel traitAtLevel(@Nonnull EnumTraitLevel level) {
        if (level == EnumTraitLevel.NULL) return null;

        return levels.get(level);
    }

    public void setLevelOnCompound(@Nonnull NBTTagCompound compound, @Nonnull EnumTraitLevel level) {
        boolean in = isInCompound(compound);
        boolean willRemove = level == EnumTraitLevel.NULL;

        if (in && willRemove)
            compound.removeTag(getNBTKey());
        else if (!willRemove)
            compound.setString(getNBTKey(), level.getName());
    }




    public void applyAttributes(@Nonnull ItemStack stack, @Nonnull Multimap<String, AttributeModifier> modifiers, @Nonnull EntityEquipmentSlot slot) {
        TraitAtLevel atLevel = levelOn(stack);
        if (atLevel != null)
            atLevel.applyAttributes(modifiers, slot);
    }

    public double getKnockbackMultiplier(@Nonnull ItemStack stack, double current) {
        TraitAtLevel atLevel = levelOn(stack);
        if (atLevel != null)
            return atLevel.getKnockbackMultiplier(current);
        return current;
    }

    public double modifyWithPenetrationMultiplier(@Nonnull ItemStack stack, double current) {
        TraitAtLevel atLevel = levelOn(stack);
        if (atLevel != null)
            return atLevel.modifyWithPenetrationMultiplier(current);
        return current;
    }

    public double chanceToBreak(@Nonnull ItemStack stack, double current) {
        TraitAtLevel atLevel = levelOn(stack);
        if (atLevel != null)
            return atLevel.chanceToBreak(current);
        return current;
    }

    public void hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase attacker) {
        TraitAtLevel atLevel = levelOn(stack);
        if (atLevel != null)
            atLevel.hitEntity(attacker);
    }
}
