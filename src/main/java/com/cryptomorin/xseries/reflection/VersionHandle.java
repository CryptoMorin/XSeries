package com.cryptomorin.xseries.reflection;

import java.util.concurrent.Callable;

/**
 * Provides objects based on the current version of the server ({@link XReflection#supports(int, int, int)}),
 * it's mostly useful for primitive values, for reflection you should use the specialized
 * {@link XReflection} class instead.
 * @param <T> the type of object to check.
 */
public final class VersionHandle<T> {
    private int version, patch;
    private T handle;
    // private RuntimeException errors;

    VersionHandle(int version, T handle) {
        this(version, 0, handle);
    }

    VersionHandle(int version, int patch, T handle) {
        if (XReflection.supports(version, patch)) {
            this.version = version;
            this.patch = patch;
            this.handle = handle;
        }
    }

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
     */
    public T orElse(T handle) {
        return this.version == 0 ? handle : this.handle;
    }

    public T orElse(Callable<T> handle) {
        if (this.version == 0) {
            try {
                return handle.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.handle;
    }
}
