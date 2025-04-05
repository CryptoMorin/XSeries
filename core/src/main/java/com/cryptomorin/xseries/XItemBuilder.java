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
    private static final Map<Class<? extends Property>, Supplier<Property>> PROPERTIES_REGISTRY = new IdentityHashMap<>();
    private final Map<Class<? extends Property>, Property> properties = new IdentityHashMap<>();
    private final XMaterial material;

    static {
        PROPERTIES_REGISTRY.put(Amount.class, Amount::new); //TODO Add private empty constructor to every Property
        PROPERTIES_REGISTRY.put(DisplayName.class, DisplayName::new);
        PROPERTIES_REGISTRY.put(Durability.class, Durability::new);
    }

    public XItemBuilder(final XMaterial material) {
        this.material = material;
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

    public static XItemBuilder from(ItemStack item) {
        XMaterial material = XMaterial.matchXMaterial(item);
        XItemBuilder builder = new XItemBuilder(material);

        ItemMeta meta = item.getItemMeta();
        for (Map.Entry<Class<? extends Property>, Supplier<Property>> entry : PROPERTIES_REGISTRY.entrySet()) {
            Property currentProperty = entry.getValue().get();
            currentProperty.from(item, meta);
            builder.property(currentProperty); //TODO Only add if currentProperty was set (maybe add boolean return to Property#from())
        }

        return builder;
    }

    public ItemStack build() {
        ItemStack item = material.parseItem();
        to(item);
        return item;
    }

    public <T extends Property> Optional<T> get(Class<T> propertyType) {
        return Optional.ofNullable((T) properties.get(propertyType));
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

    //TODO should we use ths? Would make simple properties much smaller.
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

    //TODO should we use ths? Would make simple properties much smaller.
    public abstract static class LambdaProperty<T> implements SimpleProperty {
        private final BiConsumer<ItemStack, T> toLambda;
        private final Function<ItemStack, T> fromLambda;
        private T value;

        protected LambdaProperty(
                final T value,
                final BiConsumer<ItemStack, T> toLambda,
                final Function<ItemStack, T> fromLambda
        ) {
            this.toLambda = toLambda;
            this.fromLambda = fromLambda;
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
    }


    public static final class Amount implements SimpleProperty {
        private int amount;

        public Amount(int amount) {
            this.amount = amount;
        }

        @Override
        public void to(final ItemStack item) {
            item.setAmount(amount);
        }

        @Override
        public void from(final ItemStack item) {
            this.amount = item.getAmount();

        }
    }

    //TODO should we use ths? (Implementation of LambdaProperty)
    public static final class AmountAlternative extends LambdaProperty<Integer> {
        public AmountAlternative(final Integer amount) {
    public static final class Amount extends LambdaProperty<Integer> {
            super(amount, ItemStack::setAmount, ItemStack::getAmount);
        }
    }

    public static final class DisplayName implements ItemMetaProperty {
        private String displayName;

        public DisplayName(final String displayName) {
            this.displayName = displayName;
        }

        @Override
        public void to(final ItemMeta meta) {
            meta.setDisplayName(displayName);
        }

        @Override
        public void from(final ItemMeta meta) {
            this.displayName = meta.getDisplayName();
        }
    }

    @SuppressWarnings("deprecation")
    public static class Durability implements Property {
        private static final int NEW_DURABILITY_VERSION = 13;
        private int durability;

        public Durability(final int durability) {
            this.durability = durability;
        }


        @Override
        public void to(final ItemStack item, final ItemMeta meta) {
            if (supports(NEW_DURABILITY_VERSION)) {
                if (meta instanceof Damageable) {
                    if (durability > 0) ((Damageable) meta).setDamage(durability);
                }
            } else {
                if (durability > 0) item.setDurability((short) durability);
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
    }

    public static final class Lore implements ItemMetaProperty {
        private List<String> lore;

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
            this.lore = meta.getLore();
        }
    }

    public static final class LoreAlternative extends LambdaMetaProperty<ItemMeta, List<String>> {
        public LoreAlternative(final List<String> lore) {
            super(lore, ItemMeta.class, ItemMeta::setLore, ItemMeta::getLore);
        }
    }

    public static final class BookAuthor implements MetaProperty<BookMeta> {
        private String bookAuthor;

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
    }

}
