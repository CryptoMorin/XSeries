/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Crypto Morin
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
 * If a specific player-identifying {@link Profileable} is not found.
 * This can be an unknown username or UUID. (Invalid usernames and UUIDs are handled by {@link InvalidProfileException}).
 */
public final class UnknownPlayerException extends InvalidProfileException {
    private final Object unknownObject;

    public UnknownPlayerException(Object unknownObject, String message) {
        super(unknownObject.toString(), message);
        this.unknownObject = unknownObject;
    }

    /**
     * An invalid or unknown username ({@link String}) or an unknown {@link java.util.UUID}.
     */
    @NotNull
    public Object getUnknownObject() {
        return unknownObject;
    }
}