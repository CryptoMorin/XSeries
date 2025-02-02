/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.cryptomorin.xseries.profiles.exceptions;

import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.jetbrains.annotations.NotNull;

/**
 * If a given {@link Profileable} has incorrect value (more general than {@link UnknownPlayerException})
 * which mostly happens when a string value is passed to be handled by {@link com.cryptomorin.xseries.profiles.objects.ProfileInputType}.
 * E.g. invalid Base64, texture URLs, username, UUID, etc.
 * Though most of these invalid values are just recognized as an unknown string instead
 * of being invalid values of a specific input type.
 * <p>
 * Note: Unknown usernames and UUIDs are handled by {@link UnknownPlayerException} instead.
 */
public class InvalidProfileException extends ProfileException {
    private final String value;

    public InvalidProfileException(String value, String message) {
        super(message);
        this.value = value;
    }

    public InvalidProfileException(String value, String message, Throwable cause) {
        super(message, cause);
        this.value = value;
    }

    /**
     * The invalid value.
     */
    @NotNull
    public String getValue() {
        return value;
    }
}