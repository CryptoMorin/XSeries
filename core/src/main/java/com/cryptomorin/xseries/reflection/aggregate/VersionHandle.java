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
    private int major, minor, patch;
    private T handle;
    // private RuntimeException errors;

    private String stringVersion() {
        return major + "." + minor + "." + patch;
    }

    /**
     * Use {@link XReflection#v(int, int, Object)} instead.
     */
    @ApiStatus.Internal
    public VersionHandle(int major, int minor, int patch, T handle) {
        if (XReflection.supports(major, minor, patch)) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.handle = handle;
        }
    }

    /**
     * Use {@link XReflection#v(int, int, Callable)} instead.
     */
    @ApiStatus.Internal
    public VersionHandle(int major, int minor, int patch, Callable<T> handle) {
        if (XReflection.supports(major, minor, patch)) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;

            try {
                this.handle = handle.call();
            } catch (Exception ignored) {
            }
        }
    }

    private boolean checkVersion(int major, int minor, int patch) {
        if (major == this.major && minor == this.minor && patch == this.patch)
            throw new IllegalArgumentException("Cannot have duplicate version handles for version: " + stringVersion());

        // We call supports() first to validate versioning system breaking change.
        return XReflection.supports(major, minor, patch) && major > this.major && minor > this.minor && patch >= this.patch;
    }

    public VersionHandle<T> v(int major, int minor, int patch, Callable<T> handle) {
        if (!checkVersion(major, minor, patch)) return this;

        try {
            this.handle = handle.call();
        } catch (Exception ignored) {
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        return this;
    }

    public VersionHandle<T> v(int major, int minor, Callable<T> handle) {
        return v(major, minor, 0, handle);
    }

    public VersionHandle<T> v(int major, int minor, int patch, T handle) {
        if (checkVersion(major, minor, patch)) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.handle = handle;
        }
        return this;
    }

    public VersionHandle<T> v(int major, int minor, T handle) {
        return v(major, minor, 0, handle);
    }

    /**
     * If none of the previous version checks matched, it'll return this object.
     *
     * @see #orElse(Callable)
     */
    public T orElse(T handle) {
        return this.major == 0 ? handle : this.handle;
    }

    /**
     * If none of the previous version checks matched, it'll return this object.
     *
     * @see #orElse(Object)
     */
    public T orElse(Callable<T> handle) {
        if (this.major == 0) {
            try {
                return handle.call();
            } catch (Exception e) {
                throw new IllegalArgumentException("The last handle also failed", e);
            }
        }
        return this.handle;
    }
}
