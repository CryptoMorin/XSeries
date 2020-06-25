package com.cryptomorin.xseries.unused;

import com.cryptomorin.xseries.ReflectionUtils;
import com.google.common.base.Enums;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <b>BossBar</b>
 * This class will not be updated use {@link org.bukkit.boss.BossBar} instead.
 *
 * @author Crypto Morin
 * @version 1.1.0
 */
public class BossBar implements Cloneable {
    /**
     * The maximum number of BossBars a player can have.
     */
    public static final int MAX_BOSSBARS = 4;

    // Methods
    private static final MethodHandle PACKET;
    private static final MethodHandle SERIALIZER;

    // Enums
    private static final Object[] COLORS = ReflectionUtils.getNMSClass("BossBattle$BarColor").getEnumConstants();
    private static final Object[] STYLES = ReflectionUtils.getNMSClass("BossBattle$BarStyle").getEnumConstants();
    private static final Object[] ACTIONS = ReflectionUtils.getNMSClass("PacketPlayOutBoss$Action").getEnumConstants();

    // Fields
    private static final MethodHandle ID;
    private static final MethodHandle ACTION;
    private static final MethodHandle MESSAGE;
    private static final MethodHandle PROGRESS;
    private static final MethodHandle COLOR;
    private static final MethodHandle STYLE;
    private static final MethodHandle DARKEN_SKY;
    private static final MethodHandle PLAY_MUSIC;
    private static final MethodHandle CREATE_FOG;

    static {
        Class<?> packetClass = ReflectionUtils.getNMSClass("PacketPlayOutBoss");
        Class<?> baseComponent = ReflectionUtils.getNMSClass("IChatBaseComponent");
        Class<?> chatSerializer = ReflectionUtils.getNMSClass("IChatBaseComponent$ChatSerializer");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle packet = null;
        MethodHandle serializer = null;

        MethodHandle id = null;
        MethodHandle action = null;
        MethodHandle message = null;
        MethodHandle progress = null;
        MethodHandle color = null;
        MethodHandle style = null;
        MethodHandle darkenSky = null;
        MethodHandle playMusic = null;
        MethodHandle createFog = null;

        try {
            packet = lookup.findConstructor(packetClass, MethodType.methodType(void.class));
            serializer = lookup.findStatic(chatSerializer, "a", MethodType.methodType(baseComponent, String.class));

            Field field = packetClass.getDeclaredField("a");
            field.setAccessible(true);
            id = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("b");
            field.setAccessible(true);
            action = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("c");
            field.setAccessible(true);
            message = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("d");
            field.setAccessible(true);
            progress = lookup.unreflectSetter(field);


            field = packetClass.getDeclaredField("e");
            field.setAccessible(true);
            color = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("f");
            field.setAccessible(true);
            style = lookup.unreflectSetter(field);


            field = packetClass.getDeclaredField("g");
            field.setAccessible(true);
            darkenSky = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("h");
            field.setAccessible(true);
            playMusic = lookup.unreflectSetter(field);

            field = packetClass.getDeclaredField("i");
            field.setAccessible(true);
            createFog = lookup.unreflectSetter(field);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        PACKET = packet;
        SERIALIZER = serializer;

        ID = id;
        ACTION = action;
        MESSAGE = message;
        PROGRESS = progress;
        COLOR = color;
        STYLE = style;
        DARKEN_SKY = darkenSky;
        PLAY_MUSIC = playMusic;
        CREATE_FOG = createFog;
    }

    private final UUID id;
    private final Set<Property> properties = EnumSet.noneOf(Property.class);
    private final Object packet;
    private List<UUID> receivers = new ArrayList<>();
    private float progress;
    private String title;
    private Color color;
    private Style style;
    private boolean visible;

    public BossBar(String title, Color color, Style style, float progress, Property... properties) {
        this.id = UUID.randomUUID();

        this.color = color != null ? color : Color.PURPLE;
        this.style = style != null ? style : Style.PROGRESS;
        this.title = title;
        this.progress = progress;
        this.properties.addAll(Arrays.asList(properties));

        Object packet = null;
        try {
            packet = PACKET.invoke();
            ID.invoke(packet, this.id);
            MESSAGE.invoke(packet, SERIALIZER.invoke(this.title));
            PROGRESS.invoke(packet, this.progress);
            COLOR.invoke(packet, COLORS[this.color.ordinal()]);
            STYLE.invoke(packet, STYLES[this.style.ordinal()]);
            DARKEN_SKY.invoke(packet, hasProperty(Property.DARKEN_SKY));
            PLAY_MUSIC.invoke(packet, hasProperty(Property.PLAY_MUSIC));
            CREATE_FOG.invoke(packet, hasProperty(Property.CREATE_FOG));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        this.packet = packet;
    }

    public static BossBar fromConfig(ConfigurationSection section) {
        return new BossBar(ChatColor.translateAlternateColorCodes('&', section.getString("message")),
                Enums.getIfPresent(Color.class, section.getString("color")).or(Color.PURPLE),
                Enums.getIfPresent(Style.class, section.getString("style")).or(Style.PROGRESS),
                (float) section.getDouble("progress"));
    }

    public static void removeBossBar(List<Player> players) {
        BossBar bossBar = new BossBar(null, null, null, 0);
        bossBar.setPlayers(players);
        bossBar.update(Action.REMOVE);
    }

    public boolean hasProperty(Property property) {
        return properties.contains(property);
    }

    public BossBar setStyle(Style style) {
        Objects.requireNonNull(color, "BossBar style cannot be null");
        if (style != this.style) {
            this.style = style;
            try {
                STYLE.invoke(packet, STYLES[this.style.ordinal()]);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            update(Action.UPDATE_STYLE);
        }
        return this;
    }

    /**
     * Sets the BossBar properties without updating it.
     *
     * @param property the property to set.
     * @param flag     the flag of this property.
     * @since 1.0.0
     */
    public void setPropertySilent(Property property, boolean flag) {
        Objects.requireNonNull(color, "Cannot set null property of BossBar");
        boolean hasProp = hasProperty(property);
        if (hasProp == flag) return;
        if (hasProp) properties.remove(property);
        else properties.add(property);

        switch (property) {
            case DARKEN_SKY:
                try {
                    DARKEN_SKY.invoke(packet, flag);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            case PLAY_MUSIC:
                try {
                    PLAY_MUSIC.invoke(packet, flag);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            case CREATE_FOG:
                try {
                    CREATE_FOG.invoke(packet, flag);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                break;
            default:
                break;
        }

        update(Action.UPDATE_PROPERTIES);
    }

    public void setProperty(Property property, boolean flag) {
        setPropertySilent(property, flag);
        update(Action.UPDATE_PROPERTIES);
    }

    /**
     * Sends the updated BossBar packet to the receivers.
     *
     * @param action the update state.
     * @since 1.0.0
     */
    public void update(Action action) {
        try {
            ACTION.invoke(packet, ACTIONS[action.ordinal()]);

            for (UUID receiver : receivers) {
                Player player = Bukkit.getPlayer(receiver);
                if (player == null) continue;
                ReflectionUtils.sendPacket(player, this.packet);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Sends the BossBar with the same properties to
     * the receivers.
     *
     * @since 1.0.0
     */
    public void send() {
        update(Action.ADD);
    }

    public BossBar setPlayers(List<Player> players) {
        List<UUID> receivers = new ArrayList<>();
        for (Player player : players) {
            receivers.add(player.getUniqueId());
        }
        this.receivers = receivers;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public BossBar setVisible(boolean flag) {
        if (flag != this.visible) {
            this.visible = flag;
            update(flag ? Action.ADD : Action.REMOVE);
        }
        return this;
    }

    public Color getColor() {
        return color;
    }

    public BossBar setColor(Color color) {
        Objects.requireNonNull(color, "BossBar color cannot be null");
        if (color != this.color) {
            this.color = color;
            try {
                COLOR.invoke(packet, COLORS[this.color.ordinal()]);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            update(Action.UPDATE_STYLE);
        }
        return this;
    }

    public float getProgress() {
        return progress;
    }

    public BossBar setProgress(float progress) {
        if (progress != this.progress) {
            if (progress > 1) progress = progress / 100f;
            this.progress = progress;
            try {
                PROGRESS.invoke(packet, this.progress);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            update(Action.UPDATE_PCT);
        }
        return this;
    }

    public List<UUID> getReceivers() {
        return receivers;
    }

    public BossBar setReceivers(List<UUID> receivers) {
        this.receivers = receivers;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public BossBar setTitle(String title) {
        Objects.requireNonNull(title, "Cannot set null as the BossBar message");
        if (!title.startsWith("{") || !title.endsWith("}")) return setMessage(new TextComponent(title));
        if (!title.equals(this.title)) {
            this.title = title;
            try {
                MESSAGE.invoke(packet, SERIALIZER.invoke(this.title));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            update(Action.UPDATE_NAME);
        }
        return this;
    }

    public BossBar setMessage(BaseComponent component) {
        Objects.requireNonNull(component, "Cannot translate null BaseComponent for BossBar message");
        this.title = ComponentSerializer.toString(component);
        try {
            MESSAGE.invoke(packet, SERIALIZER.invoke(this.title));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        update(Action.UPDATE_NAME);
        return this;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public void setProperties(Property... properties) {
        List<Property> prop = Arrays.asList(properties);
        for (Property property : Property.values()) setPropertySilent(property, prop.contains(property));
        update(Action.UPDATE_PROPERTIES);
    }

    public UUID getId() {
        return id;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public BossBar clone() {
        return new BossBar(title, color, style, progress, properties.toArray(new Property[0]));
    }

    /**
     * The color of the BossBar.
     *
     * @since 1.0.0
     */
    public enum Color {
        PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE;
    }

    /**
     * The style of the BossBar.
     * The notched (also known as segmented) styles will divide the bar
     * with small gray indicators.
     * The parts numbers are the specified number after them.
     *
     * @since 1.0.0
     */
    public enum Style {
        PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20;
    }

    /**
     * The packet actions sent the player.
     * These are just used to define the update state of the packet.
     *
     * @since 1.0.0
     */
    private enum Action {
        ADD,
        REMOVE,
        UPDATE_PCT, // Progress
        UPDATE_NAME,
        UPDATE_STYLE, // And Color
        UPDATE_PROPERTIES;
    }

    /**
     * The properties of a BossBar.
     *
     * @since 1.0.0
     */
    public enum Property {
        DARKEN_SKY, PLAY_MUSIC, CREATE_FOG;
    }
}