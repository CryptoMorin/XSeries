/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Crypto Morin
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

package com.cryptomorin.xseries.reflection.aggregate;

import com.cryptomorin.xseries.reflection.XReflection;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Callable;

/**
 * Provides objects based on the current version of the server ({@link XReflection#supports(int, int, int)}),
 * it's mostly useful for primitive values, for reflection you should use the specialized
 * {@link XReflection} class instead.
 *
 * @param <T> the type of object to check.
 */
public final class VersionHandle<T> {
    private int version, patch;
    private T handle;
    // private RuntimeException errors;

    /**
     * Use {@link XReflection#v(int, Object)} instead.
     */
    @ApiStatus.Internal
    public VersionHandle(int version, T handle) {
        this(version, 0, handle);
    }

    /**
     * Use {@link XReflection#v(int, int, Object)} instead.
     */
    @ApiStatus.Internal
    public VersionHandle(int version, int patch, T handle) {
        if (XReflection.supports(version, patch)) {
            this.version = version;
            this.patch = patch;
            this.handle = handle;
        }
    }

    /**
     * Use {@link XReflection#v(int, int, Callable)} instead.
     */
    @ApiStatus.Internal
    public VersionHandle(int version, int patch, Callable<T> handle) {
        if (XReflection.supports(version, patch)) {
            this.version = version;
            this.patch = patch;

            try {
                this.handle = handle.call();
            } catch (Exception ignored) {
            }
        }
    }

    @ApiStatus.Internal
    public VersionHandle(int version, Callable<T> handle) {
        this(version, 0, handle);
    }

    public VersionHandle<T> v(int version, T handle) {
        return v(version, 0, handle);
    }

    private boolean checkVersion(int version, int patch) {
        if (version == this.version && patch == this.patch)
            throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + version + '.' + patch);
        return version > this.version && patch >= this.patch && XReflection.supports(version, patch);
    }

    public VersionHandle<T> v(int version, int patch, Callable<T> handle) {
        if (!checkVersion(version, patch)) return this;

        try {
            this.handle = handle.call();
        } catch (Exception ignored) {
        }

        this.version = version;
        this.patch = patch;
        return this;
    }

    public VersionHandle<T> v(int version, int patch, T handle) {
        if (checkVersion(version, patch)) {
            this.version = version;
            this.patch = patch;
            this.handle = handle;
        }
        return this;
    }

    /**
     * If none of the previous version checks matched, it'll return this object.
     *
     * @see #orElse(Callable)
     */
    public T orElse(T handle) {
        return this.version == 0 ? handle : this.handle;
    }

    /**
     * If none of the previous version checks matched, it'll return this object.
     *
     * @see #orElse(Object)
     */
    public T orElse(Callable<T> handle) {
        if (this.version == 0) {
            try {
                return handle.call();
            } catch (Exception e) {
                throw new IllegalArgumentException("The last handle also failed", e);
            }
        }
        return this.handle;
    }
}
