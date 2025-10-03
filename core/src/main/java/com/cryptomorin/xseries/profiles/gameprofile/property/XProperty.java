package com.cryptomorin.xseries.profiles.gameprofile.property;

import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;

public class XProperty {
    private XProperty() {}

    private static final boolean USE_RECORDS = XReflection.isRecord(Property.class);
    private static final MethodHandle PropertyMap$ctor =
            XReflection.of(PropertyMap.class).constructor().reflectOrNull();

    @NotNull
    public static MojangProperty of(Property property) {
        Objects.requireNonNull(property, "Property is null");
        if (USE_RECORDS) return new NewProperty(property);
        else return new OldProperty(property);
    }


    public static MojangProperty create(String name, String value) {
        return of(new Property(name, value));
    }

    public static MojangProperty create(String name, String value, String signature) {
        return of(new Property(name, value, signature));
    }

    public static PropertyMap createPropertyMap(Multimap<String, Property> map) {
        if (PropertyMap$ctor == null) return new PropertyMap(map);

        try {
            PropertyMap propertyMap = (PropertyMap) PropertyMap$ctor.invokeExact();
            propertyMap.putAll(map);
            return propertyMap;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
