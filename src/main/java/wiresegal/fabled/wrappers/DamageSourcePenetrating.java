package wiresegal.fabled.wrappers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 7:42 PM on 4/15/18.
 */
public class DamageSourcePenetrating extends DamageSource {

    @Nonnull
    private final DamageSource parent; // Treated as unmodifiable

    public DamageSourcePenetrating(@Nonnull DamageSource parent) {
        super(parent.getDamageType());
        this.parent = parent;
    }

    @Override
    public boolean isUnblockable() {
        return true;
    }

    @Override
    public float getHungerDamage() {
        return 0;
    }


    // Wrapper methods

    @Nonnull
    @Override
    public String getDamageType() {
        return parent.getDamageType();
    }

    @Override
    public boolean isProjectile() {
        return parent.isProjectile();
    }

    @Override
    public boolean isExplosion() {
        return parent.isExplosion();
    }

    @Override
    public boolean canHarmInCreative() {
        return parent.canHarmInCreative();
    }

    @Override
    public boolean isDamageAbsolute() {
        return parent.isDamageAbsolute();
    }

    @Nullable
    @Override
    public Entity getImmediateSource() {
        return parent.getImmediateSource();
    }

    @Nullable
    @Override
    public Entity getTrueSource() {
        return parent.getTrueSource();
    }

    @Nonnull
    @Override
    public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        return parent.getDeathMessage(entityLivingBaseIn);
    }

    @Override
    public boolean isFireDamage() {
        return parent.isFireDamage();
    }

    @Override
    public boolean isDifficultyScaled() {
        return parent.isDifficultyScaled();
    }

    @Override
    public boolean isMagicDamage() {
        return parent.isMagicDamage();
    }

    @Override
    public boolean isCreativePlayer() {
        return parent.isCreativePlayer();
    }

    @Nullable
    @Override
    public Vec3d getDamageLocation() {
        return parent.getDamageLocation();
    }
}
