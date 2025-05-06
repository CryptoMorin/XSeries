package com.cryptomorin.xseries;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.cryptomorin.xseries.XMaterial.supports;

public class XItemBuilder {
    private static final String META_PACKAGE = "org.bukkit.inventory.meta.";
    private static final Map<Class<? extends Property>, Supplier<? extends Property>> PROPERTIES_REGISTRY = new IdentityHashMap<>();
    private final Map<Class<? extends Property>, Property> properties = new IdentityHashMap<>();

    static {
        register(Material::new);
        register(Amount::new);
        register(DisplayName::new);
        register(Durability::new);
        register(Unbreakable::new);
        register(CustomModelData::new);
        register(Lore::new);
        register(BookAuthor::new);
    }

    private static <T extends Property> void register(Supplier<T> creator) {
        T property = creator.get();
        if (property.isSupported()) {
            PROPERTIES_REGISTRY.put(creator.get().getClass(), creator);
        }
    }

    private static final Set<String> AVAILABLE_CLASSES = new HashSet<>();

    private static boolean checkMetaAvailable(String metaName) {
        return checkClassAvailable(META_PACKAGE + metaName);
    }

    private static boolean checkClassAvailable(String requestedClass) {
        if (AVAILABLE_CLASSES.contains(requestedClass)) return true;
        try {
            Class.forName(requestedClass);
            AVAILABLE_CLASSES.add(requestedClass);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }


    public XItemBuilder() {
    }

    public XItemBuilder(final XMaterial material) {
        property(new Material(material));
    }

    public void to(ItemStack item) {
        to(item, false);
    }

    public void to(ItemStack item, boolean deleteBefore) {
        if (deleteBefore) deleteAll(item);

        boolean metaModified = false;
        ItemMeta meta = item.getItemMeta();

        for (Property prop : properties.values()) {
            prop.to(item, meta);
            if (!metaModified) metaModified = prop.affectsMeta();
        }

        if (metaModified) {
            item.setItemMeta(meta);
        }
    }

    public void from(ItemStack item, boolean override) {
        ItemMeta meta = item.getItemMeta();
        for (Map.Entry<Class<? extends Property>, Supplier<? extends Property>> entry : PROPERTIES_REGISTRY.entrySet()) {
            Property currentProperty = entry.getValue().get();
            currentProperty.from(item, meta);
            if (!currentProperty.isDefault() && (override || !properties.containsKey(currentProperty.getClass()))) {
                property(currentProperty);
            }
        }
    }

    public static XItemBuilder from(ItemStack item) {
        XItemBuilder builder = new XItemBuilder();
        builder.from(item, true);
        return builder;
    }

    public ItemStack build() {
        Optional<Material> material = get(Material.class);
        if (!material.isPresent() || material.get().isDefault()) {
            throw new IllegalStateException("No material specified for the ItemStack!");
        }

        ItemStack item = material.get().getMaterial().parseItem();
        to(item);

        return item;
    }

    public <T extends Property> Optional<T> get(Class<T> propertyType) {
        return Optional.ofNullable((T) properties.get(propertyType));
    }

    public XItemBuilder remove(Class<? extends Property> propertyType) {
        properties.remove(propertyType);
        return this;
    }

    public XItemBuilder delete(Class<? extends Property> property) {
        if (property.equals(Material.class)) {
            throw new IllegalArgumentException("Can't delete the material property!");
        }
        Supplier<? extends Property> propertyCtor = PROPERTIES_REGISTRY.get(property);
        if (propertyCtor != null) {
            properties.put(property, propertyCtor.get());
        }
        return this;
    }

    public static XItemBuilder createDeleteBuilder() {
        XItemBuilder deleteBuilder = new XItemBuilder();
        for (Map.Entry<Class<? extends Property>, Supplier<? extends Property>> prop : PROPERTIES_REGISTRY.entrySet()) {
            deleteBuilder.properties.put(prop.getKey(), prop.getValue().get());
        }
        return deleteBuilder;
    }

    public static void deleteAll(ItemStack item) {
        createDeleteBuilder().to(item, false);
    }


    private XItemBuilder property(Property property) {
        properties.put(property.getClass(), property);
        return this;
    }


    public interface Property {

        void to(ItemStack item, ItemMeta meta);

        void from(ItemStack item, ItemMeta meta);

        default boolean isSupported() {
            return true;
        }

        boolean affectsMeta();

        boolean isDefault();
    }

    public interface MetaProperty<T extends ItemMeta> extends Property {
        Class<T> getMetaClass();

        default void to(ItemStack item, ItemMeta meta) {
            if (!isSupported()) return;

            Class<T> metaClass = getMetaClass();
            if (!metaClass.isInstance(meta)) return;
            T specificMeta = metaClass.cast(meta);

            to(specificMeta);
        }

        void to(T meta);

        default void from(ItemStack item, ItemMeta meta) {
            if (!isSupported()) return;

            Class<T> metaClass = getMetaClass();
            if (!metaClass.isInstance(meta)) return;
            T specificMeta = metaClass.cast(meta);

            from(specificMeta);
        }

        void from(T meta);

        @Override
        default boolean affectsMeta() {
            return true;
        }
    }

    public interface SimpleProperty extends Property {
        @Override
        default void to(ItemStack item, ItemMeta meta) {
            to(item);
        }

        void to(ItemStack item);

        @Override
        default void from(ItemStack item, ItemMeta meta) {
            from(item);
        }

        void from(ItemStack item);

        @Override
        default boolean affectsMeta() {
            return false;
        }
    }

    private static <T, R> Function<T, R> conditional(Function<T, Boolean> condition, Function<T, R> action) {
        return conditional(condition, action, null);
    }

    private static <T, R> Function<T, R> conditional(Function<T, Boolean> condition, Function<T, R> action, R defaultVal) {
        return (T meta) -> {
            if (condition.apply(meta)) {
                return action.apply(meta);
            }
            return defaultVal;
        };
    }

    public abstract static class LambdaMetaProperty<META extends ItemMeta, T> implements MetaProperty<META> {
        private final BiConsumer<META, T> toLambda;
        private final Function<META, T> fromLambda;
        private final T defaultValue;
        private final Supplier<Class<META>> metaClass;
        private T value;

        protected LambdaMetaProperty(
                final T value,
                final T defaultValue,
                final Supplier<Class<META>> metaClass,
                final BiConsumer<META, T> toLambda,
                final Function<META, T> fromLambda
        ) {
            this.toLambda = toLambda;
            this.fromLambda = fromLambda;
            this.defaultValue = defaultValue;
            this.metaClass = metaClass;
            this.value = value;
        }

        @Override
        public void to(final META meta) {
            toLambda.accept(meta, value);
        }

        @Override
        public void from(final META meta) {
            value = fromLambda.apply(meta);
        }

        @Override
        public Class<META> getMetaClass() {
            return metaClass.get();
        }

        @Override
        public boolean isDefault() {
            return Objects.equals(value, defaultValue);
        }
    }

    public abstract static class LambdaProperty<T> implements SimpleProperty {
        private final BiConsumer<ItemStack, T> toLambda;
        private final Function<ItemStack, T> fromLambda;
        private final T defaultValue;
        private T value;

        protected LambdaProperty(
                final T value,
                final T defaultValue,
                final BiConsumer<ItemStack, T> toLambda,
                final Function<ItemStack, T> fromLambda
        ) {
            this.toLambda = toLambda;
            this.fromLambda = fromLambda;
            this.defaultValue = defaultValue;
            this.value = value;
        }

        @Override
        public void to(final ItemStack meta) {
            toLambda.accept(meta, value);
        }

        @Override
        public void from(final ItemStack meta) {
            value = fromLambda.apply(meta);
        }

        @Override
        public boolean isDefault() {
            return Objects.equals(value, defaultValue);
        }
    }


    // @formatter:off

    public XItemBuilder withAmount(int amount) { return property(new Amount(amount)); }
    public XItemBuilder withDisplayName(String name) { return property(new DisplayName(name)); }
    public XItemBuilder withDurability(int durability) { return property(new Durability(durability)); }
    public XItemBuilder withUnbreakable(boolean unbreakable) { return property(new Unbreakable(unbreakable)); }
    public XItemBuilder withCustomModelData(int customModelData) { return property(new CustomModelData(customModelData)); }
    public XItemBuilder withLore(List<String> lore) { return property(new Lore(lore)); }
    public XItemBuilder withBookAuthor(String bookAuthor) { return property(new BookAuthor(bookAuthor)); }


    public static final class Material extends LambdaProperty<XMaterial> {
        public Material(XMaterial material) { super(material, null, (a, b) -> {}, XMaterial::matchXMaterial); }
        public Material() { this(null); }

        public XMaterial getMaterial() { return super.value; }
    }

    public static final class Amount extends LambdaProperty<Integer> {
        private static final int DEFAULT_VALUE = 1;

        public Amount(Integer amount) { super(amount, DEFAULT_VALUE, ItemStack::setAmount, ItemStack::getAmount); }
        public Amount() { this(DEFAULT_VALUE); }
    }

    public static final class DisplayName extends LambdaMetaProperty<ItemMeta, String> {
        public DisplayName(String displayName) {
            super(displayName, null, () -> ItemMeta.class, ItemMeta::setDisplayName,
                    conditional(ItemMeta::hasDisplayName, ItemMeta::getDisplayName));
        }
        public DisplayName() { this(null); }
    }

    @SuppressWarnings("deprecation")
    public static class Durability implements Property {
        private static final boolean SUPPORTS_META = checkMetaAvailable("Damageable");
        private int durability;

        public Durability() {}
        public Durability(int durability) { this.durability = durability; }

        @Override public void to(ItemStack item, ItemMeta meta) {
            if (SUPPORTS_META) {
                if (meta instanceof Damageable) {
                    ((Damageable) meta).setDamage(durability);
                }
            } else {
                item.setDurability((short) durability);
            }
        }

        @Override public void from(ItemStack item, ItemMeta meta) {
            if (SUPPORTS_META) {
                if (meta instanceof Damageable) {
                    durability = ((Damageable) meta).getDamage();
                }
            } else {
                durability = item.getDurability();
            }
        }

        @Override public boolean affectsMeta() { return SUPPORTS_META; }
        @Override public boolean isDefault() { return durability == 0; }
    }

    public static final class Unbreakable extends LambdaMetaProperty<ItemMeta, Boolean> {
        public Unbreakable(final Boolean unbreakable) {
            super(unbreakable, false, () -> ItemMeta.class, ItemMeta::setUnbreakable, ItemMeta::isUnbreakable);
        }
        public Unbreakable() { this(false); }

        private static final boolean SUPPORTS_PROPERTY = supports(11);
        @Override public boolean isSupported() { return SUPPORTS_PROPERTY; }
    }

    public static final class CustomModelData extends LambdaMetaProperty<ItemMeta, Integer> {
        public CustomModelData(final Integer customModelData) {
            super(customModelData, 0, () -> ItemMeta.class, ItemMeta::setCustomModelData,
                    conditional(ItemMeta::hasCustomModelData, ItemMeta::getCustomModelData));
        }
        public CustomModelData() { this(0); }

        private static final boolean SUPPORTS_PROPERTY = supports(14);
        @Override public boolean isSupported() { return SUPPORTS_PROPERTY; }
    }

    public static final class Lore extends LambdaMetaProperty<ItemMeta, List<String>> {
        /**
         * In some versions, an empty string for a lore line is completely
         * ignored, so at least a space " " is needed to get empty lore lines.
         * <p>
         * This seems to be inconsistent between versions, so it's always enabled.
         */
        private static final boolean SPACE_EMPTY_LORE_LINES = true;

        public Lore(List<String> lore) {
            super(translateLoreSpaces(lore), null, () -> ItemMeta.class, ItemMeta::setLore,
                    conditional(ItemMeta::hasLore, ItemMeta::getLore));
        }
        public Lore() { this(null); }

        public static List<String> translateLoreSpaces(List<String> lore) {
            if (!SPACE_EMPTY_LORE_LINES) return lore;

            List<String> translatedLore = new ArrayList<>(lore.size());
            if (!lore.isEmpty()) {
                for (String loreLine : lore) {
                    if (loreLine.isEmpty()) translatedLore.add(" ");
                }
            }

            return translatedLore;
        }
    }

    public static final class BookAuthor extends LambdaMetaProperty<BookMeta, String> {
        public BookAuthor(String bookAuthor) {
            super(bookAuthor, null, () -> BookMeta.class, BookMeta::setAuthor,
                    conditional(BookMeta::hasAuthor, BookMeta::getAuthor));
        }
        public BookAuthor() { this(null); }

        private static final boolean SUPPORTS_PROPERTY = checkMetaAvailable("BookMeta");
        @Override public boolean isSupported() { return SUPPORTS_PROPERTY; }
    }

}
