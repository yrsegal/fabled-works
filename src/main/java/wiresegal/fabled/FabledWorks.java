package wiresegal.fabled;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.fabled.config.ModConfig;
import wiresegal.fabled.config.Trait;
import wiresegal.fabled.wrappers.DamageSourcePenetrating;

import java.util.List;
import java.util.Random;

import static wiresegal.fabled.FabledWorks.FABLED;

/**
 * @author WireSegal
 * Created at 4:31 PM on 4/15/18.
 */
@Mod(modid = FABLED, name = "Fabled Works", version = "GRADLE:VERSION")
@Mod.EventBusSubscriber
public class FabledWorks {
    public static final String FABLED = "fabledworks";

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void editTooltips(ItemTooltipEvent event) {
        List<String> tooltip = event.getToolTip();
        ItemStack stack = event.getItemStack();

        List<String> inject = Lists.newArrayList();
        List<Trait> traits = TraitManager.allTraitsOnStack(stack);
        EnumTraitLevel traitLevel = EnumTraitLevel.NULL;

        for (Trait trait : traits) {
            EnumTraitLevel level = TraitManager.getLevel(stack, trait);
            if (level != EnumTraitLevel.NULL)
                inject.add(level.getLocalizedText(trait));
            if (level.compareTo(traitLevel) > 0)
                traitLevel = level;
        }

        if (traitLevel != EnumTraitLevel.NULL) {
            tooltip.set(0, traitLevel.getLocalizedText(tooltip.get(0)));

            if (tooltip.size() > 1)
                inject.add("\n");
            tooltip.addAll(1, inject);
        }
    }

    private static EntityLivingBase attacking;
    private static DamageSource attackingWith;
    private static float subtract;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void penetrationDamage(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (source.getTrueSource() instanceof EntityLivingBase && !source.isUnblockable()) {
            EntityLivingBase target = event.getEntityLiving();
            EntityLivingBase aggressor = (EntityLivingBase) source.getTrueSource();
            ItemStack stack = aggressor.getHeldItemMainhand();

            if (stack.isEmpty()) return;

            List<Trait> traits = TraitManager.allTraitsOnStack(stack);

            if (traits.isEmpty()) return;

            double penetratingAmount = 0f;

            for (Trait trait : traits)
                penetratingAmount = trait.modifyWithPenetrationMultiplier(stack, penetratingAmount);

            if (penetratingAmount > 0) {
                DamageSource bypass = new DamageSourcePenetrating(source);

                attacking = target;
                attackingWith = source;
                subtract = (float) penetratingAmount;
                int resistanceTime = target.hurtResistantTime;
                target.attackEntityFrom(bypass, event.getAmount() * subtract);
                target.hurtResistantTime = resistanceTime;
            }

            if (!aggressor.world.isRemote) for (Trait trait : traits)
                trait.hitEntity(stack, aggressor);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void decreaseDamageForPenetration(LivingHurtEvent event) {
        if (event.getEntityLiving() == attacking && event.getSource() == attackingWith) {
            event.setAmount(event.getAmount() * (1 - subtract));

            attacking = null;
            attackingWith = null;
            subtract = 0f;
        }
    }

    @SubscribeEvent
    public static void knockbackMultiplier(LivingKnockBackEvent event) {
        Entity attacker = event.getAttacker();
        if (attacker instanceof EntityLivingBase) {
            ItemStack stack = ((EntityLivingBase) attacker).getHeldItemMainhand();

            if (stack.isEmpty()) return;

            List<Trait> traits = TraitManager.allTraitsOnStack(stack);

            if (traits.isEmpty()) return;

            double knockbackMultiplier = 1;

            for (Trait trait : traits)
                knockbackMultiplier = trait.getKnockbackMultiplier(stack, knockbackMultiplier);

            event.setStrength(event.getStrength() * (float) knockbackMultiplier);
        }
    }

    @SubscribeEvent
    public static void configReload(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(FABLED))
            ModConfig.init();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.injectConfigFile(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModConfig.init();
    }

    private static double chance = 0;

    public static boolean preventDamage(ItemStack stack, boolean newIndex, Random random) {
        if (newIndex) {
            List<Trait> traits = TraitManager.allTraitsOnStack(stack);

            if (traits.isEmpty()) {
                chance = 0;
                return false;
            }

            double newChance = 1f;

            for (Trait trait : traits)
                newChance = trait.chanceToBreak(stack, newChance);

            chance = newChance;
        }

        return chance != 0 && random.nextDouble() > chance;
    }


    public static void applyAllModifiers(ItemStack stack, EntityEquipmentSlot slot, Multimap<String, AttributeModifier> attributes) {
        List<Trait> traits = TraitManager.allTraitsOnStack(stack);

        for (Trait trait : traits)
            trait.applyAttributes(stack, attributes, slot);
    }
}
