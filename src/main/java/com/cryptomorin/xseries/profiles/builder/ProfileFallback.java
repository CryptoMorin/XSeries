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

package com.cryptomorin.xseries.profiles.builder;

import com.cryptomorin.xseries.profiles.exceptions.ProfileChangeException;
import com.cryptomorin.xseries.profiles.objects.ProfileContainer;

/**
 * An object that has all the information about failed attempts.
 *
 * @param <T> the object that has its profile set. See {@link ProfileContainer} for a list.
 */
public final class ProfileFallback<T> {
    private final ProfileInstruction<T> instruction;
    private T object;
    private final ProfileChangeException error;

    public ProfileFallback(ProfileInstruction<T> instruction, T object, ProfileChangeException error) {
        this.instruction = instruction;
        this.object = object;
        this.error = error;
    }

    public T getObject() {
        return object;
    }

    public ProfileInstruction<T> getInstruction() {
        return instruction;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public ProfileChangeException getError() {
        return error;
    }
}
