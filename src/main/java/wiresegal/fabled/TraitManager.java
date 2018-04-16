package wiresegal.fabled;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import wiresegal.fabled.config.Trait;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author WireSegal
 * Created at 4:23 PM on 4/15/18.
 */
public final class TraitManager {
    private static Map<String, Trait> ALL_TRAITS = new HashMap<>();
    private static Set<Trait> TRAIT_SET = Sets.newHashSet();
    private static Set<Trait> TRAIT_SET_WITH_NULL = Sets.newHashSet((Trait) null);

    private static final String NBT_KEY = FabledWorks.FABLED + ":traits";

    public static void registerTrait(Trait trait) {
        ALL_TRAITS.put(trait.getName(), trait);
        TRAIT_SET.add(trait);
        TRAIT_SET_WITH_NULL.add(trait);
    }

    public static Set<Trait> getAllTraits() {
        return TRAIT_SET;
    }

    public static Set<Trait> getAllWithNullEntry() {
        return TRAIT_SET_WITH_NULL;
    }

    public static void purgeAllTraits() {
        ALL_TRAITS.clear();
        TRAIT_SET.clear();
        TRAIT_SET_WITH_NULL.clear();
        TRAIT_SET_WITH_NULL.add(null);
    }

    @Nullable
    private static NBTTagCompound traitDataOnStack(@Nonnull ItemStack stack) {
        return stack.getSubCompound(NBT_KEY);
    }

    public static boolean hasTraitData(@Nonnull ItemStack stack) {
        return stack.getSubCompound(NBT_KEY) != null;
    }

    @Nonnull
    public static NBTTagCompound getTraitData(@Nonnull ItemStack stack) {
        NBTTagCompound subCompound = traitDataOnStack(stack);
        if (subCompound == null) subCompound = new NBTTagCompound();

        return subCompound;
    }

    public static void updateTraitData(@Nonnull ItemStack stack, @Nonnull NBTTagCompound traitData) {
        stack.setTagInfo(NBT_KEY, traitData);
    }

    @Nullable
    public static Trait lookupTrait(@Nonnull String traitName) {
        return ALL_TRAITS.get(traitName);
    }

    @Nonnull
    public static EnumTraitLevel getLevel(@Nonnull ItemStack stack, @Nonnull String traitName) {
        Trait trait = lookupTrait(traitName);
        return trait == null ? EnumTraitLevel.NULL : getLevel(stack, trait);
    }

    @Nonnull
    public static EnumTraitLevel getLevel(@Nonnull ItemStack stack, @Nonnull Trait trait) {
        NBTTagCompound traitData = getTraitData(stack);
        return trait.getLevelFromCompound(traitData);
    }

    public static void setLevel(@Nonnull ItemStack stack, @Nonnull String traitName, @Nonnull EnumTraitLevel level) {
        Trait trait = lookupTrait(traitName);
        if (trait != null)
            setLevel(stack, trait, level);
    }

    public static void setLevel(@Nonnull ItemStack stack, @Nonnull Trait trait, @Nonnull EnumTraitLevel level) {
        NBTTagCompound traitData = getTraitData(stack);
        trait.setLevelOnCompound(traitData, level);
        updateTraitData(stack, traitData);
    }

    @Nonnull
    public static List<Trait> allTraitsOnStack(@Nonnull ItemStack stack) {
        List<Trait> traits = Lists.newArrayList();
        NBTTagCompound data = traitDataOnStack(stack);
        if (data == null)
            return traits;

        for (String key : data.getKeySet()) {
            Trait trait = lookupTrait(key);
            if (trait != null)
                traits.add(trait);
        }

        return traits;
    }
}
