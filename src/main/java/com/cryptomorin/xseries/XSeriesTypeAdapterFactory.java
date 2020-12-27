package com.cryptomorin.xseries;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link TypeAdapterFactory} designed to provide Gson support to XSeries enums:
 * <ul>
 *     <li>{@link XSound}</li>
 *     <li>{@link XMaterial}</li>
 *     <li>{@link XBiome}</li>
 *     <li>{@link XEnchantment}</li>
 *     <li>{@link XPotion}</li>
 * </ul>
 * <p>
 * This factory will create a {@link TypeAdapter} for each of the above types, by
 * using the conventional <code>XSomething#matchXSomething</code>, and throwing
 * concise {@link IllegalArgumentException} on invalid values with messages
 * like "Invalid sound: boop"
 * <p>
 * Values are case-insensitive.
 * <p>
 * Register using {@link com.google.gson.GsonBuilder#registerTypeAdapterFactory(TypeAdapterFactory)}.
 * <p>
 * Singleton instance can be accessed through {@link XSeriesTypeAdapterFactory#getInstance()}.
 */
public class XSeriesTypeAdapterFactory implements TypeAdapterFactory {

    private static final ImmutableMap<Class<?>, Function<String, Optional<?>>> MAPPING = ImmutableMap.<Class<?>, Function<String, Optional<?>>>builder()
            .put(XSound.class, XSound::matchXSound)
            .put(XMaterial.class, XMaterial::matchXMaterial)
            .put(XBiome.class, XBiome::matchXBiome)
            .put(XEnchantment.class, XEnchantment::matchXEnchantment)
            .put(XPotion.class, XPotion::matchXPotion)
            .build();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> token) {
        Class<?> type = token.getRawType();
        if (!type.isEnum()) return null;
        Function<String, Optional<?>> matchFunction = MAPPING.get(type);
        if (matchFunction == null) return null;
        String name = type.getSimpleName().substring(1).toLowerCase();
        return new TypeAdapter<T>() {
            @Override public void write(JsonWriter out, T value) throws IOException {
                if (value == null) out.nullValue();
                else out.value(name(value));
            }

            @Override public T read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL)
                    return null;
                String value = in.nextString();
                return (T) matchFunction.apply(value.toUpperCase())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid " + name + ": " + value));
            }
        };
    }

    private static final XSeriesTypeAdapterFactory INSTANCE = new XSeriesTypeAdapterFactory();

    /**
     * Returns the singleton instance of this factory.
     *
     * @return Singleton instance
     */
    public static XSeriesTypeAdapterFactory getInstance() {
        return INSTANCE;
    }

    private static String name(@Nonnull Object value) {
        return ((Enum<?>) value).name().toLowerCase();
    }
}
