package wiresegal.fabled.asm;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import wiresegal.fabled.FabledWorks;

import java.util.Random;

/**
 * @author WireSegal
 * Created at 6:17 PM on 4/10/18.
 */
@SuppressWarnings("unused")
public class FabledAsmHooks {
    public static void applyExtraAttributes(
            Multimap<String, AttributeModifier> map,
            ItemStack stack,
            EntityEquipmentSlot slot) {
        FabledWorks.applyAllModifiers(stack, slot, map);
    }

    public static boolean shouldNotDamage(ItemStack stack, int index, Random rand) {
        return FabledWorks.preventDamage(stack, index == 0, rand);
    }
}
