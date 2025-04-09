package com.cryptomorin.xseries;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.cryptomorin.xseries.XMaterial.supports;

public class XItemBuilder {
    private static final Map<Class<? extends Property>, Supplier<? extends Property>> PROPERTIES_REGISTRY = new IdentityHashMap<>();
    private final Map<Class<? extends Property>, Property> properties = new IdentityHashMap<>();

    static {
        register(Material::new);
        register(Amount::new);
        register(DisplayName::new);
        register(Durability::new);
        register(Lore::new);
        register(BookAuthor::new);
    }

    private static <T extends Property> void register(Supplier<T> creator) {
        PROPERTIES_REGISTRY.put(creator.get().getClass(), creator);
    }

    public XItemBuilder() {
    }

    public XItemBuilder(final XMaterial material) {
        property(new Material(material));
    }

    public void to(ItemStack item) {
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

    public XItemBuilder remove(Class<Property> propertyType) {
        properties.remove(propertyType);
        return this;
    }

    private XItemBuilder property(Property property) {
        properties.put(property.getClass(), property);
        return this;
    }

    public XItemBuilder withAmount(int amount) {
        return property(new Amount(amount));
    }

    public XItemBuilder withDisplayName(String name) {
        return property(new DisplayName(name));
    }

    public XItemBuilder withDurability(int durability) {
        return property(new Durability(durability));
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

    public interface ItemMetaProperty extends MetaProperty<ItemMeta> {

        @Override
        default Class<ItemMeta> getMetaClass() {
            return ItemMeta.class;
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

    public abstract static class LambdaMetaProperty<META extends ItemMeta, T> implements MetaProperty<META> {
        private final BiConsumer<META, T> toLambda;
        private final Function<META, T> fromLambda;
        private final Class<META> metaClass;
        private T value;

        protected LambdaMetaProperty(
                final T value,
                final Class<META> metaClass,
                final BiConsumer<META, T> toLambda,
                final Function<META, T> fromLambda
        ) {
            this.toLambda = toLambda;
            this.fromLambda = fromLambda;
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
            return metaClass;
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
            return value == defaultValue;

        }
    }

    public static final class Material extends LambdaProperty<XMaterial> {
        public Material(final XMaterial material) {
            super(material, null, (a, b) -> {
            }, XMaterial::matchXMaterial);
        }

        public Material() {
            this(null);
        }

        public XMaterial getMaterial() {
            return super.value;
        }
    }

    public static final class Amount extends LambdaProperty<Integer> {
        private static final int DEFAULT_VALUE = 1;

        public Amount(final Integer amount) {
            super(amount, DEFAULT_VALUE, ItemStack::setAmount, ItemStack::getAmount);
        }

        public Amount() {
            this(DEFAULT_VALUE);
        }
    }

    public static final class DisplayName implements ItemMetaProperty {
        private String displayName;

        public DisplayName() {
        }

        public DisplayName(final String displayName) {
            this.displayName = displayName;
        }

        @Override
        public void to(final ItemMeta meta) {
            meta.setDisplayName(displayName);
        }

        @Override
        public void from(final ItemMeta meta) {
            if (!meta.hasDisplayName()) return;
            this.displayName = meta.getDisplayName();
        }

        @Override
        public boolean isDefault() {
            return displayName == null;
        }
    }

    @SuppressWarnings("deprecation")
    public static class Durability implements Property {
        private static final int NEW_DURABILITY_VERSION = 13;
        private int durability;

        public Durability() {
        }

        public Durability(final int durability) {
            this.durability = durability;
        }


        @Override
        public void to(final ItemStack item, final ItemMeta meta) {
            if (supports(NEW_DURABILITY_VERSION)) {
                if (meta instanceof Damageable) {
                    ((Damageable) meta).setDamage(durability);
                }
            } else {
                item.setDurability((short) durability);
            }
        }

        @Override
        public void from(final ItemStack item, final ItemMeta meta) {
            if (supports(NEW_DURABILITY_VERSION)) {
                if (meta instanceof Damageable) {
                    durability = ((Damageable) meta).getDamage();
                }
            } else {
                durability = item.getDurability();
            }
        }

        @Override
        public boolean affectsMeta() {
            return supports(NEW_DURABILITY_VERSION);
        }

        @Override
        public boolean isDefault() {
            return durability == 0;
        }
    }

    public static final class Lore implements ItemMetaProperty {
        private List<String> lore;

        public Lore() {
        }

        public Lore(final List<String> lore) {
            this.lore = lore;
        }
        //TODO translate lore spaces

        @Override
        public void to(final ItemMeta meta) {
            meta.setLore(lore);
        }

        @Override
        public void from(final ItemMeta meta) {
            if (!meta.hasLore()) return;
            this.lore = meta.getLore();
        }

        @Override
        public boolean isDefault() {
            return lore == null;
        }
    }

    public static final class BookAuthor implements MetaProperty<BookMeta> {
        private String bookAuthor;

        public BookAuthor() {
        }

        public BookAuthor(String bookAuthor) {
            this.bookAuthor = bookAuthor;
        }

        @Override
        public Class<BookMeta> getMetaClass() {
            return BookMeta.class;
        }

        @Override
        public void to(final BookMeta meta) {
            meta.setAuthor(bookAuthor);
        }

        @Override
        public void from(final BookMeta meta) {
            if (!meta.hasAuthor()) return;
            bookAuthor = meta.getAuthor();
        }

        @Override
        public boolean isDefault() {
            return bookAuthor == null;
        }
    }

}
