package com.cryptomorin.xseries.skull;

import com.mojang.authlib.GameProfile;

import java.util.function.Function;
import java.util.function.Supplier;

public class Instruction<T> {
    final protected Supplier<GameProfile> getter;
    final protected Function<GameProfile, T> setter;

    protected String input = null;
    protected InputType type = null;

    Instruction(
        Supplier<GameProfile> getter,
        Function<GameProfile, T> setter
    ) {
        this.getter = getter;
        this.setter = setter;
    }

    public GameProfile profile() {
        return getter.get();
    }

    public Action<T> profile(String input) {
        this.input = input;
        return new Action<>(this);
    }
    public Action<T> profile(InputType type, String input) {
        this.type = type;
        this.input = input;
        return new Action<>(this);
    }}
