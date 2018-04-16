package wiresegal.fabled.config;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.common.FMLLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * @author WireSegal
 * Created at 8:22 PM on 4/15/18.
 */
public class JsonConfig {
    private final JsonObject configData;

    private static JsonObject loadFromFile(File file) {
        JsonElement data = new JsonObject();

        if (!file.exists())
            try {
                if (!file.createNewFile())
                    return new JsonObject();
            } catch (Throwable e) {
                // NO-OP
            }
        else {
            try {
                data = new JsonParser().parse(new FileReader(file));
            } catch (IOException io) {
                // Couldn't read, it's fine, just pass over

            } catch (Throwable e) {
                File fileBak = new File(file.getAbsolutePath() + "_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored");
                FMLLog.log.fatal("An exception occurred while loading config file {}. This file will be renamed to {}" +
                        " and a new config file will be generated.", file.getName(), fileBak.getName(), e);

                if (file.renameTo(fileBak))
                    data = loadFromFile(file);
            }
        }

        if (!data.isJsonObject())
            data = new JsonObject();

        return data.getAsJsonObject();
    }

    public JsonConfig(JsonObject configData) {
        this.configData = configData;
    }

    public JsonConfig(File file) {
        this(loadFromFile(file));
    }

    private void comment(@Nonnull JsonElement setTo) {
        configData.add("_comment", setTo);
    }

    public void comment(@Nonnull String title, @Nonnull String... comments) {
        List<String> lines = Lists.newArrayList();
        lines.add("======== " + title.toUpperCase(Locale.ROOT) + " ========");
        lines.addAll(Arrays.asList(comments));
        comment(lines);
    }

    public void commentNoTitle(@Nonnull String... comments) {
        comment(Arrays.asList(comments));
    }

    public void comment(@Nonnull Iterable<String> comments) {
        JsonArray arr = new JsonArray();
        for (String string : comments)
            arr.add(string);

        if (arr.size() == 1)
            comment(arr.get(0));
        else if (arr.size() > 1)
            comment(arr);
    }

    /* Getter methods start here! */

    public JsonElement get(@Nonnull String key, JsonElement defaultValue, @Nonnull Predicate<JsonElement> checkType) {
        String[] hierarchy = key.split("\\.");
        JsonObject currentObject = configData;
        for (int i = 0; i < hierarchy.length - 1; i++) {
            String k = hierarchy[i];
            if (!currentObject.has(k) || !currentObject.get(k).isJsonObject()) {
                JsonObject previous = currentObject;
                currentObject = new JsonObject();
                previous.add(k, currentObject);
            } else
                currentObject = currentObject.getAsJsonObject(k);
        }

        String finalKey = hierarchy[hierarchy.length - 1];

        JsonElement valuePresent = currentObject.get(finalKey);

        if (valuePresent == null || !checkType.test(valuePresent)) {
            currentObject.add(finalKey, defaultValue);
            return defaultValue;
        }

        return valuePresent;
    }

    @Nonnull
    public JsonConfig category(@Nonnull String key) {
        return new JsonConfig(get(key, new JsonObject()));
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public String get(@Nonnull String key, @Nonnull String defaultValue) {
        return get(key, defaultValue, false);
    }

    @Nullable
    public String get(@Nonnull String key, String defaultValue, boolean allowNull) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue), (element) -> element.isJsonNull() || element.isJsonPrimitive());
        if (el.isJsonNull())
            return allowNull ? null : defaultValue;
        return el.getAsString();
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public JsonObject get(@Nonnull String key, @Nonnull JsonObject defaultValue) {
        return get(key, defaultValue, false);
    }

    @Nullable
    public JsonObject get(@Nonnull String key, JsonObject defaultValue, boolean allowNull) {
        JsonElement el = get(key, defaultValue, (element) -> (element.isJsonNull() && allowNull) || element.isJsonObject());
        if (el.isJsonNull())
            return allowNull ? null : defaultValue;
        return el.getAsJsonObject();
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public JsonArray get(@Nonnull String key, @Nonnull JsonArray defaultValue) {
        return get(key, defaultValue, false);
    }

    @Nullable
    public JsonArray get(@Nonnull String key, @Nullable JsonArray defaultValue, boolean allowNull) {
        JsonElement el = get(key, defaultValue, (element) -> (element.isJsonNull() && allowNull) || element.isJsonArray());
        if (el.isJsonNull())
            return allowNull ? null : defaultValue;
        return el.getAsJsonArray();
    }

    @Nonnull
    public BigDecimal get(@Nonnull String key, @Nonnull BigDecimal defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsBigDecimal();
    }

    @Nonnull
    public BigInteger get(@Nonnull String key, @Nonnull BigInteger defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsBigInteger();
    }

    public long get(@Nonnull String key, long defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsLong();
    }

    public int get(@Nonnull String key, int defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsInt();
    }

    public short get(@Nonnull String key, short defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsShort();
    }

    public byte get(@Nonnull String key, byte defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsByte();
    }

    public boolean get(@Nonnull String key, boolean defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean());
        return el.getAsBoolean();
    }

    public float get(@Nonnull String key, float defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsFloat();
    }

    public double get(@Nonnull String key, double defaultValue) {
        JsonElement el = get(key, new JsonPrimitive(defaultValue),
                (element) -> element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber());
        return el.getAsDouble();
    }

    public void save(File file) {
        try {
            JsonWriter jsonWriter = new JsonWriter(new FileWriter(file));
            jsonWriter.setSerializeNulls(true);
            jsonWriter.setIndent("\t");
            Streams.write(configData, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            // NO-OP
        }
    }
}
