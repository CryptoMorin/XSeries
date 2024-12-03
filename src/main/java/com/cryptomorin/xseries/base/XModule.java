package com.cryptomorin.xseries.base;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Do not use this class directly.
 * <p>
 * All XModules should implement the following static methods:
 * <pre>{@code
 *     public static XForm of(@NotNull BukkitForm bukkit) {
 *         return REGISTRY.getByBukkitForm(bukkit);
 *     }
 *
 *     public static Optional<XForm> of(@NotNull String bukkit) {
 *         return REGISTRY.getByName(bukkit);
 *     }
 *
 *     @NotNull
 *     public static XForm[] values() {
 *         return REGISTRY.values();
 *     }
 * }</pre>
 *
 * @param <XForm>      the class type associated with the Bukkit type defined by XSeries.
 * @param <BukkitForm> the Bukkit class type associated with the XForm.
 */
public abstract class XModule<XForm extends XModule<XForm, BukkitForm>, BukkitForm> {
    private final BukkitForm bukkitForm;
    private final String[] names;

    protected XModule(BukkitForm bukkitForm, String[] names) {
        this.bukkitForm = bukkitForm;
        this.names = names;
        // this.names = new String[names.length + 1];
        // System.arraycopy(names, 0, names, 1, names.length);
    }

    /**
     * Should be used for saving data.
     */
    @NotNull
    public final String name() {
        return names[0];
    }

    @ApiStatus.Experimental
    protected void setEnumName(XRegistry<XForm, BukkitForm> registry, String enumName) {
        if (names[0] != null)
            throw new IllegalStateException("Enum name already set " + enumName + " -> " + Arrays.toString(names));
        names[0] = enumName;

        BukkitForm newForm = registry.getBukkit(names);
        if (bukkitForm != newForm) {
            // noinspection unchecked
            registry.std((XForm) this);
        }
    }

    @ApiStatus.Internal
    public String[] getNames() {
        return names;
    }

    /**
     * In most cases you should be using {@link #name()} instead.
     *
     * @return a friendly readable string name.
     */
    public String friendlyName() {
        return Arrays.stream(name().split("_"))
                .map(t -> t.charAt(0) + t.substring(1).toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(" "));
    }

    @Nullable
    public final BukkitForm get() {
        return bukkitForm;
    }

    /**
     * Checks if this sound is supported in the current Minecraft version.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #get()} != null
     * </blockquote>
     *
     * @return true if the current version has this sound, otherwise false.
     * @since 1.0.0
     */
    public final boolean isSupported() {
        return get() != null;
    }

    /**
     * Checks if this form is supported in the current version and
     * returns itself if yes.
     * <p>
     * In the other case, the alternate form will get returned,
     * no matter if it is supported or not.
     *
     * @param other the other form to get if this one is not supported.
     * @return this form or the {@code other} if not supported.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public final XForm or(XForm other) {
        return this.isSupported() ? (XForm) this : other;
    }

    @Override
    public final String toString() {
        return (isSupported() ? "" : "!") + getClass().getSimpleName() + '(' + name() + ')';
    }

    /**
     * Identity hash code.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Identity comparison. Should use {@code ==} instead.
     */
    @Override
    @Deprecated
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }
}
