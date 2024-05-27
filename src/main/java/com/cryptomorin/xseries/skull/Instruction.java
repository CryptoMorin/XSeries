package com.cryptomorin.xseries.skull;

import com.mojang.authlib.GameProfile;

import java.util.function.Function;

/**
 * The {@code Instruction} class represents an instruction that sets a property of a {@link GameProfile}.
 * It uses a {@link Function} to define how to set the property.
 *
 * @param <T> The type of the result produced by the setter function.
 */
public class Instruction<T> {

    /**
     * A function that takes a {@link GameProfile} and produces a result of type {@code T}.
     */
    final protected Function<GameProfile, T> setter;

    /**
     * The input value to be used in the profile setting operation.
     */
    protected String input = null;

    /**
     * The type of the input value.
     */
    protected InputType type = null;

    /**
     * Constructs an {@code Instruction} with the specified setter function.
     *
     * @param setter A function that sets a property of a {@link GameProfile} and returns a result of type {@code T}.
     */
    Instruction(Function<GameProfile, T> setter) {
        this.setter = setter;
    }

    /**
     * Sets the input value to be used in the profile setting operation and returns a new {@link Action} instance.
     *
     * @param input The input value to be used in the profile setting operation.
     * @return A new {@link Action} instance configured with this {@code Instruction}.
     */
    public Action<T> profile(String input) {
        this.input = input;
        return new Action<>(this);
    }

    /**
     * Sets the input type and value to be used in the profile setting operation and returns a new {@link Action} instance.
     *
     * @param type The type of the input value.
     * @param input The input value to be used in the profile setting operation.
     * @return A new {@link Action} instance configured with this {@code Instruction}.
     */
    public Action<T> profile(InputType type, String input) {
        this.type = type;
        this.input = input;
        return new Action<>(this);
    }
}
