package wiresegal.fabled;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import wiresegal.fabled.config.Trait;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static wiresegal.fabled.FabledWorks.FABLED;

/**
 * @author WireSegal
 * Created at 4:13 PM on 4/15/18.
 */
public enum EnumTraitLevel {
    NULL("no_trait", "", TextFormatting.BLACK),
    UNCOMMON("uncommon", "Uncommon", TextFormatting.WHITE),
    RARE("rare", "Rare", TextFormatting.BLUE),
    EPIC("epic", "Epic", TextFormatting.LIGHT_PURPLE),
    LEGENDARY("legendary", "Legendary", TextFormatting.GOLD);

    private static final Map<String, EnumTraitLevel> REVERSE_LOOKUP;

    public static final EnumTraitLevel[] LEVELS = Arrays.copyOfRange(values(), 1, values().length);
    public static final EnumTraitLevel[] LEVELS_DOWN = Arrays.copyOfRange(values(), 1, values().length);


    static {
        REVERSE_LOOKUP = new HashMap<>();
        for (EnumTraitLevel level : LEVELS)
            REVERSE_LOOKUP.put(level.getName(), level);

        ArrayUtils.reverse(LEVELS_DOWN);
    }

    @Nonnull
    public static EnumTraitLevel fromName(String name) {
        return REVERSE_LOOKUP.getOrDefault(name, NULL);
    }

    EnumTraitLevel(@Nonnull String name,
                   @Nonnull String jsonKey,
                   @Nonnull TextFormatting color) {
        this.name = name;
        this.jsonKey = jsonKey;
        this.color = color;
    }

    @Nonnull
    private final String name;

    @Nonnull
    private final String jsonKey;

    @Nonnull
    private final TextFormatting color;

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getJsonKey() {
        return jsonKey;
    }

    @Nonnull
    public TextFormatting getColor() {
        return color;
    }

    @Nonnull
    public EnumTraitLevel getSecondary() {
        return values()[Math.max(0, ordinal() - 1)];
    }

    @Nonnull
    public EnumTraitLevel getTertiaryFirst() {
        return values()[Math.max(0, ordinal() - 2)];
    }

    @Nonnull
    public EnumTraitLevel getTertiarySecond() {
        return values()[Math.max(0, ordinal() - 2)];
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedText(String formatParameter) {
        return getColor() + I18n.format(FABLED + ".level." + getName(), formatParameter);
    }

    @SideOnly(Side.CLIENT)
    public String getLocalizedText(Trait trait, boolean first) {
        return I18n.format(FABLED + (first ? ".with" : ".and"), getLocalizedText(trait.getLocalizedName()));
    }
}
