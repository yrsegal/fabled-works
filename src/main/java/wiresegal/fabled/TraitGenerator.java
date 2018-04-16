package wiresegal.fabled;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import wiresegal.fabled.config.ModConfig;
import wiresegal.fabled.config.Trait;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * @author WireSegal
 * Created at 12:43 PM on 4/16/18.
 */
public class TraitGenerator {
    public static void rollTraits(ItemStack stack, Random random, boolean fromLoot) {
        if ((fromLoot || !ModConfig.traitsFromLootOnly) && ModConfig.canReceiveTraits.test(stack.getItem())) {
            List<TraitRoll> rolls = getTraitPool();
            int totalWeight = WeightedRandom.getTotalWeight(rolls);
            if (fromLoot)
                totalWeight /= 2;
            TraitRoll rolled = WeightedRandom.getRandomItem(random, rolls, totalWeight);
            rolled.apply(stack);
        }
    }


    public static void invalidateTraitPool() {
        traitPool = null;
    }

    private static List<TraitRoll> traitPool = null;

    @Nonnull
    private static List<TraitRoll> getTraitPool() {
        if (traitPool == null)
            return traitPool = createTraitPool();
        return traitPool;
    }

    @Nonnull
    private static List<TraitRoll> createTraitPool() {
        Set<TraitRoll> traits = Sets.newHashSet();

        for (Trait trait : TraitManager.getAllTraits())
            for (EnumTraitLevel level : EnumTraitLevel.LEVELS)
                addAllForPrimary(traits, new LeveledTrait(trait, level));

        traits.add(new TraitRoll(null, null, null, null));

        return Lists.newArrayList(traits);
    }

    private static void addAllForPrimary(Set<TraitRoll> rolls, LeveledTrait trait) {
        Trait primary = trait.trait;

        EnumTraitLevel secondaryLevel = trait.level.getSecondary();
        EnumTraitLevel tertiaryFirstLevel = trait.level.getTertiaryFirst();
        EnumTraitLevel tertiarySecondLevel = trait.level.getTertiarySecond();


        permuteTraitRolls(rolls, trait,
                secondaryLevel, tertiaryFirstLevel, tertiarySecondLevel,
                null, null, null);

        if (tertiaryFirstLevel == EnumTraitLevel.NULL) {
            for (Trait secondary : TraitManager.getAllTraits())
                if (TraitRoll.canApply(primary, secondary, null, null))
                    rolls.add(new TraitRoll(trait, of(secondary, secondaryLevel), null, null));
        } else { // The first two levels can be optimized away
            for (Trait secondary : TraitManager.getAllTraits()) {
                if (secondary.canBeAppliedWith(primary)) {
                    for (Trait tertiary : TraitManager.getAllWithNullEntry()) {
                        if (tertiary != null && TraitRoll.canApply(primary, secondary, tertiary, null)) {
                            for (Trait tertiarySecond : TraitManager.getAllWithNullEntry())
                                    permuteTraitRolls(rolls, trait,
                                            secondaryLevel, tertiaryFirstLevel, tertiarySecondLevel,
                                            secondary, tertiary, tertiarySecond);
                        } else if (tertiary == null) {
                            permuteTraitRolls(rolls, trait,
                                    secondaryLevel, tertiaryFirstLevel, tertiarySecondLevel,
                                    secondary, null, null);
                        }
                    }
                }
            }
        }
    }

    private static void permuteTraitRolls(Set<TraitRoll> rolls, LeveledTrait trait, EnumTraitLevel secondaryLevel, EnumTraitLevel tertiaryFirstLevel, EnumTraitLevel tertiarySecondLevel,
                                          @Nullable Trait secondary, @Nullable Trait tertiaryFirst, @Nullable Trait tertiarySecond) {

        if (!TraitRoll.canApply(trait.trait, secondary, tertiaryFirst, tertiarySecond))
            return;

        ImmutableSet.Builder<EnumTraitLevel> traitLevels = ImmutableSet.<EnumTraitLevel>builder().add(secondaryLevel);
        if (tertiaryFirstLevel != EnumTraitLevel.NULL)
            traitLevels.add(tertiaryFirstLevel);
        if (tertiarySecondLevel != EnumTraitLevel.NULL)
            traitLevels.add(tertiarySecondLevel);

        ImmutableSet<EnumTraitLevel> levels = traitLevels.build();

        ImmutableSet<EnumTraitLevel> nl = ImmutableSet.<EnumTraitLevel>builder().add(EnumTraitLevel.NULL).build();

        for (EnumTraitLevel levelOfSecondary : levels) {
            for (EnumTraitLevel levelOfTertiaryFirst : tertiaryFirst == null ? nl : levels) {
                if (levelOfTertiaryFirst.ordinal() > levelOfSecondary.ordinal())
                    continue;

                for (EnumTraitLevel levelOfTertiarySecond : tertiarySecond == null ? nl : levels) {
                    if (levelOfTertiarySecond.ordinal() > levelOfSecondary.ordinal())
                        continue;

                    rolls.add(new TraitRoll(trait,
                            of(secondary, levelOfSecondary),
                            of(tertiaryFirst, levelOfTertiaryFirst),
                            of(tertiarySecond, levelOfTertiarySecond)));
                }
            }
        }
    }

    @Nullable
    private static LeveledTrait of(@Nullable Trait trait, @Nonnull EnumTraitLevel level) {
        if (trait == null || level == EnumTraitLevel.NULL)
            return null;
        return new LeveledTrait(trait, level);
    }

    private static class TraitRoll extends WeightedRandom.Item {

        @Nullable
        private final LeveledTrait primary;
        @Nullable
        private final LeveledTrait secondary;
        @Nullable
        private final LeveledTrait tertiaryFirst;
        @Nullable
        private final LeveledTrait tertiarySecond;

        public TraitRoll(@Nullable LeveledTrait primary,
                         @Nullable LeveledTrait secondary,
                         @Nullable LeveledTrait tertiaryFirst,
                         @Nullable LeveledTrait tertiarySecond) {
            super(calculateWeight(primary, secondary, tertiaryFirst, tertiarySecond));
            this.primary = primary;
            this.secondary = secondary;
            this.tertiaryFirst = tertiaryFirst;
            this.tertiarySecond = tertiarySecond;
        }

        public void apply(ItemStack stack) {
            if (primary != null)
                TraitManager.setLevel(stack, primary.trait, primary.level);
            if (secondary != null)
                TraitManager.setLevel(stack, secondary.trait, secondary.level);
            if (tertiaryFirst != null)
                TraitManager.setLevel(stack, tertiaryFirst.trait, tertiaryFirst.level);
            if (tertiarySecond != null)
                TraitManager.setLevel(stack, tertiarySecond.trait, tertiarySecond.level);

            if (primary == null && secondary == null && tertiaryFirst == null && tertiarySecond == null)
                TraitManager.updateTraitData(stack, TraitManager.getTraitData(stack));
        }

        private static boolean canApply(@Nonnull Trait one, @Nullable Trait two, @Nullable Trait three, @Nullable Trait four) {
            return (two == null || one.canBeAppliedWith(two)) &&
                    (three == null || (one.canBeAppliedWith(three) &&
                            (two == null || two.canBeAppliedWith(three)))) &&
                    (four == null || (one.canBeAppliedWith(four) &&
                            (two == null || two.canBeAppliedWith(four)) &&
                            (three == null || three.canBeAppliedWith(four))));
        }

        private static int calculateWeight(@Nullable LeveledTrait primary,
                                           @Nullable LeveledTrait secondary,
                                           @Nullable LeveledTrait tertiaryFirst,
                                           @Nullable LeveledTrait tertiarySecond) {
            int defaultWeight = ModConfig.weightOfUncommon * 100 * 4;

            double weightPercentage = 1;

            if (primary != null)
                weightPercentage *= primary.weightCost();
            if (secondary != null)
                weightPercentage *= secondary.weightCost();
            if (tertiaryFirst != null)
                weightPercentage *= tertiaryFirst.weightCost();
            if (tertiarySecond != null)
                weightPercentage *= tertiarySecond.weightCost();

            if (weightPercentage == 1)
                return (int) (defaultWeight * ModConfig.weightOfNone / (1 - ModConfig.weightOfNone));

            return Math.max((int) (defaultWeight * weightPercentage), 1);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TraitRoll traitRoll = (TraitRoll) o;
            return Objects.equals(primary, traitRoll.primary) &&
                    Objects.equals(secondary, traitRoll.secondary) &&
                    Objects.equals(tertiaryFirst, traitRoll.tertiaryFirst) &&
                    Objects.equals(tertiarySecond, traitRoll.tertiarySecond);
        }

        @Override
        public int hashCode() {
            return Objects.hash(primary, secondary, tertiaryFirst, tertiarySecond);
        }
    }

    private static class LeveledTrait {

        private final Trait trait;
        private final EnumTraitLevel level;

        public LeveledTrait(Trait trait, EnumTraitLevel level) {
            this.trait = trait;
            this.level = level;
        }

        public Trait getTrait() {
            return trait;
        }

        public EnumTraitLevel getLevel() {
            return level;
        }

        public double weightCost() {
            double forLevel = 0;
            switch (level) {
                case UNCOMMON:
                    forLevel = ModConfig.weightOfUncommon;
                    break;
                case RARE:
                    forLevel = ModConfig.weightOfRare;
                    break;
                case EPIC:
                    forLevel = ModConfig.weightOfEpic;
                    break;
                case LEGENDARY:
                    forLevel = ModConfig.weightOfLegendary;
                    break;
            }

            return (forLevel * trait.getWeight()) / (ModConfig.weightOfUncommon * 100);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LeveledTrait that = (LeveledTrait) o;
            return Objects.equals(trait, that.trait) &&
                    level == that.level;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trait, level);
        }
    }
}
