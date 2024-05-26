package com.cryptomorin.xseries.skull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.regex.Pattern;

public enum InputType {

    TEXTURE_HASH(Pattern.compile("[0-9a-z]{55,70}")),
    TEXTURE_URL(Pattern.compile(Pattern.quote(XSkull.TEXTURES) + TEXTURE_HASH.pattern)),
    BASE64(Pattern.compile("[-A-Za-z0-9+/]{100,}={0,3}")),
    UUID(Pattern.compile("[A-F\\d]{8}-[A-F\\d]{4}-4[A-F\\d]{3}-([89AB])[A-F\\d]{3}-[A-F\\d]{12}", Pattern.CASE_INSENSITIVE)),
    USERNAME(Pattern.compile("[A-Za-z0-9_]{1,16}"));

    private final Pattern pattern;

    InputType(Pattern compile) {
        this.pattern = compile;
    }

    @Nullable
    public static InputType get(@Nonnull String identifier) {
        return Arrays.stream(InputType.values())
                .filter(value -> value.pattern.matcher(identifier).matches())
                .findFirst().orElse(null);
    }
}