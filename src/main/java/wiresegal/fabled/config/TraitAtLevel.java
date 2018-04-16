package wiresegal.fabled.config;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.server.MinecraftServer;
import wiresegal.fabled.wrappers.CommandExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author WireSegal
 * Created at 3:45 PM on 4/15/18.
 */
public class TraitAtLevel {
    private final double unbreakingChance;
    private final double penetrationMultiplier;
    private final double knockbackMultiplier;

    @Nonnull
    private final Map<EntityEquipmentSlot, Multimap<String, AttributeModifier>> attributes;

    @Nullable
    private final String command;

    public TraitAtLevel(double unbreakingChance, double penetrationMultiplier, double knockbackMultiplier, @Nonnull Map<EntityEquipmentSlot, Multimap<String, AttributeModifier>> attributes, @Nullable String command) {
        this.unbreakingChance = unbreakingChance;
        this.penetrationMultiplier = penetrationMultiplier;
        this.knockbackMultiplier = knockbackMultiplier;
        this.attributes = attributes;
        this.command = command;
    }

    public void applyAttributes(@Nonnull Multimap<String, AttributeModifier> modifiers, @Nonnull EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> mine = attributes.get(slot);
        if (mine != null)
            modifiers.putAll(attributes.get(slot));
    }

    public double getKnockbackMultiplier(double current) {
        return knockbackMultiplier * current;
    }

    public double modifyWithPenetrationMultiplier(double current) {
        if (current == -penetrationMultiplier)
            return current < 0 ? current : -current;

        if (Math.abs(penetrationMultiplier) > Math.abs(current))
            return penetrationMultiplier;

        return current;
    }

    public double chanceToBreak(double current) {
        return (1 - unbreakingChance) * current;
    }

    public void hitEntity(@Nonnull EntityLivingBase attacker) {
        if (!attacker.world.isRemote && command != null) {
            MinecraftServer server = attacker.getServer();
            if (server != null)
                server.getCommandManager().executeCommand(new CommandExecutor(attacker), command);
        }
    }
}
