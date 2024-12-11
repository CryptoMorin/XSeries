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

package com.cryptomorin.xseries.reflection.parser;

import com.cryptomorin.xseries.reflection.ReflectiveHandle;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.*;
import com.cryptomorin.xseries.reflection.jvm.classes.*;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <b>Note:</b> Currently, if XSeries is included as a library, the @Language annotation doesn't work
 * even if you have the sources downloaded, this is unfortunately
 * <a href="https://youtrack.jetbrains.com/issue/IJPL-18247/LanguageInjection-does-not-work-if-annotation-is-in-project-from-dependency">a bug with IntelliJ</a>.
 * <p>
 * This class should not be used directly.
 * <p>
 * This class is designed to be able to parse Java declarations only in a loose way, as it also
 * supports more simplified syntax (for example not requiring semicolons) that doesn't work
 * as nicely if you're using IntelliJ because of the highlighting which is greater advantage.
 */
@ApiStatus.Internal
public final class ReflectionParser {
    private final String declaration;
    private Pattern pattern;
    private Matcher matcher;
    private ReflectiveNamespace namespace;
    private Map<String, Class<?>> cachedImports;
    private final Set<Flag> flags = EnumSet.noneOf(Flag.class);
    private static final PackageHandle[] PACKAGE_HANDLES = MinecraftPackage.values();

    public ReflectionParser(@Language("Java") String declaration) {
        this.declaration = declaration;
    }

    private enum Flag {
        PUBLIC, PROTECTED, PRIVATE, FINAL, TRANSIENT, ABSTRACT, STATIC, NATIVE, SYNCHRONIZED, STRICTFP, VOLATILE;

        @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
        private static final String FLAGS_REGEX = "(?<flags>(?:(?:" + Arrays.stream(Flag.values())
                .map(Enum::name)
                .map(x -> x.toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining("|"))
                + ")\\s*)+)?";
    }

    @Language("RegExp")
    private static final String
            GENERIC = "(?:\\s*<\\s*[.\\w<>\\[\\], ]+\\s*>)?",
            ARRAY = "((?:\\[])*)",
            PACKAGE_REGEX = "(?:package\\s+(?<package>" + PackageHandle.JAVA_PACKAGE_PATTERN + ")\\s*;\\s*)?",
            CLASS_TYPES = "(?<classType>class|interface|enum)",
            PARAMETERS = "\\s*\\(\\s*(?<parameters>[\\w$_,. ]+)?\\s*\\)",
            END_DECL = "\\s*;?\\s*";

    private static final Pattern
            CLASS = Pattern.compile(PACKAGE_REGEX + Flag.FLAGS_REGEX + CLASS_TYPES + "\\s+" + type("className") +
            "(?:\\s+extends\\s+" + id("superclasses") + ")?\\s+(implements\\s+" + id("interfaces") + ")?(?:\\s*\\{\\s*})?\\s*");
    private static final Pattern METHOD = Pattern.compile(Flag.FLAGS_REGEX + type("methodReturnType") + "\\s+"
            + id("methodName") + PARAMETERS + END_DECL);
    private static final Pattern CONSTRUCTOR = Pattern.compile(Flag.FLAGS_REGEX + "\\s+"
            + id("className") + PARAMETERS + END_DECL);
    private static final Pattern FIELD = Pattern.compile(Flag.FLAGS_REGEX + type("fieldType") + "\\s+"
            + id("fieldName") + END_DECL);

    @Language("RegExp")
    private static String id(@Language("RegExp") String groupName) {
        if (groupName == null) return PackageHandle.JAVA_IDENTIFIER_PATTERN;
        return "(?<" + groupName + '>' + PackageHandle.JAVA_IDENTIFIER_PATTERN + ')';
    }

    @SuppressWarnings("LanguageMismatch")
    @Language("RegExp")
    private static String type(@Language("RegExp") String groupName) {
        String type = PackageHandle.JAVA_PACKAGE_PATTERN + GENERIC + ARRAY;
        return "(?<" + groupName + '>' + type + ')';
    }

    private ClassHandle[] parseTypes(String[] typeNames) {
        ClassHandle[] classes = new ClassHandle[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            String typeName = typeNames[i];
            typeName = typeName.trim().substring(0, typeName.lastIndexOf(' ')).trim();
            classes[i] = parseType(typeName);
        }
        return classes;
    }

    private static final Map<String, Class<?>> PREDEFINED_TYPES = new HashMap<>();

    static {
        Arrays.asList(
                byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class, void.class,
                Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class, Void.class,
                Object.class, String.class, CharSequence.class, StringBuilder.class, StringBuffer.class, UUID.class, Optional.class,
                Map.class, HashMap.class, ConcurrentHashMap.class, LinkedHashMap.class, WeakHashMap.class,
                List.class, ArrayList.class, Set.class, HashSet.class, Deque.class, Queue.class, LinkedList.class,
                Date.class, Calendar.class, Duration.class
        ).forEach(x -> PREDEFINED_TYPES.put(x.getSimpleName(), x));
    }

    private ClassHandle parseType(String typeName) {
        if (this.cachedImports == null && this.namespace != null) {
            this.cachedImports = this.namespace.getImports();
        }

        String firstTypeName = typeName;
        typeName = typeName.replace(" ", "");
        int arrayDimension = 0;
        if (typeName.endsWith("[]")) { // Arrays
            String replaced = typeName.replace("[]", "");
            arrayDimension = (typeName.length() - replaced.length()) / 2;
            typeName = replaced;
        }
        if (typeName.endsWith(">")) { // Generic
            typeName = typeName.substring(0, typeName.indexOf('<'));
        }

        Class<?> clazz = null;
        if (!typeName.contains(".")) {
            // Override predefined types
            if (cachedImports != null) clazz = this.cachedImports.get(typeName);
            if (clazz == null) clazz = PREDEFINED_TYPES.get(typeName);
        }
        if (clazz == null) {
            try {
                clazz = Class.forName(typeName);
            } catch (ClassNotFoundException ignored) {
            }
        }

        // if (clazz == null) error("Unknown type '" + firstTypeName + "' -> '" + typeName + '\'');
        if (clazz == null) return new UnknownClassHandle(getOrCreateNamespace(), firstTypeName + " -> " + typeName);
        if (arrayDimension != 0) {
            clazz = XReflection.of(clazz).asArray(arrayDimension).unreflect();
        }
        return new StaticClassHandle(getOrCreateNamespace(), clazz);
    }

    private ReflectiveNamespace getOrCreateNamespace() {
        return namespace == null ? XReflection.namespaced() : namespace;
    }

    public ReflectionParser imports(ReflectiveNamespace namespace) {
        this.namespace = namespace;
        return this;
    }

    private void pattern(Pattern pattern, ReflectiveHandle<?> handle) {
        this.pattern = pattern;
        this.matcher = pattern.matcher(declaration);
        start(handle);
    }

    public <T extends DynamicClassHandle> T parseClass(T classHandle) {
        pattern(CLASS, classHandle);

        String packageName = group("package");
        if (packageName != null && !packageName.isEmpty()) {
            boolean found = false;
            for (PackageHandle pkgHandle : PACKAGE_HANDLES) {
                String targetPackageName = pkgHandle.packageId().toLowerCase(Locale.ENGLISH);
                if (packageName.startsWith(targetPackageName)) {
                    classHandle.inPackage(pkgHandle, packageName.substring(targetPackageName.length() + 1)); // + 1 for the dot
                    found = true;
                    break;
                }
            }
            if (!found) classHandle.inPackage(packageName);
        }

        // String classGeneric = parser.group("generic");
        String className = group("className");
        if (className.contains("<")) className = className.substring(0, className.indexOf('<'));
        classHandle.named(className);

        return classHandle;
    }

    public <T extends ConstructorMemberHandle> T parseConstructor(T ctorHandle) {
        pattern(CONSTRUCTOR, ctorHandle);
        if (has("className")) {
            if (!ctorHandle.getClassHandle().getPossibleNames().contains(group("className")))
                error("Wrong class name associated to constructor, possible names: " + ctorHandle.getClassHandle().getPossibleNames());
        }
        if (has("parameters")) ctorHandle.parameters(parseTypes(group("parameters").split(",")));
        return ctorHandle;
    }

    public <T extends MethodMemberHandle> T parseMethod(T methodHandle) {
        pattern(METHOD, methodHandle);

        // String classGeneric = parser.group("generic");
        methodHandle.named(group("methodName").split("\\$"));
        methodHandle.returns(parseType(group("methodReturnType")));
        if (has("parameters")) methodHandle.parameters(parseTypes(group("parameters").split(",")));

        return methodHandle;
    }

    public <T extends FieldMemberHandle> T parseField(T fieldHandle) {
        pattern(FIELD, fieldHandle);

        // String classGeneric = parser.group("generic");
        fieldHandle.named(group("fieldName").split("\\$"));
        fieldHandle.returns(parseType(group("fieldType")));

        return fieldHandle;
    }

    private String group(String groupName) {
        return this.matcher.group(groupName);
    }

    private boolean has(String groupName) {
        String group = group(groupName);
        return group != null && !group.isEmpty();
    }

    private void start(ReflectiveHandle<?> handle) {
        if (!matcher.matches()) error("Not a " + handle + " declaration");
        parseFlags();
        if (handle instanceof MemberHandle) {
            MemberHandle memberHandle = (MemberHandle) handle;
            if (!hasOneOf(flags, Flag.PUBLIC, Flag.PROTECTED, Flag.PRIVATE)) {
                // No access modifier is set.
                // Interface methods are public, no need to make it accessible.
                Class<?> clazz = memberHandle.getClassHandle().reflectOrNull();
                if (clazz != null && !clazz.isInterface()) memberHandle.makeAccessible(); // package-private
            } else if (hasOneOf(flags, Flag.PRIVATE, Flag.PROTECTED)) {
                memberHandle.makeAccessible();
            }
            if (handle instanceof FieldMemberHandle) {
                if (flags.contains(Flag.FINAL)) ((FieldMemberHandle) handle).asFinal();
            }
            if (handle instanceof FlaggedNamedMemberHandle) {
                if (flags.contains(Flag.STATIC)) ((FlaggedNamedMemberHandle) handle).asStatic();
            }
        }
    }

    private void parseFlags() {
        if (!has("flags")) return;
        String flagsStr = group("flags");

        for (String flag : flagsStr.split("\\s+")) {
            if (!flags.add(Flag.valueOf(flag.toUpperCase(Locale.ENGLISH)))) {
                error("Repeated flag: " + flag);
            }
        }

        if (containsDuplicates(flags, Flag.PUBLIC, Flag.PROTECTED, Flag.PRIVATE)) {
            error("Duplicate visibility flags");
        }
    }

    @SafeVarargs
    private static <T> boolean containsDuplicates(Collection<T> collection, T... values) {
        boolean contained = false;
        for (T value : values) {
            if (collection.contains(value)) {
                if (contained) return true;
                else contained = true;
            }
        }
        return false;
    }

    @SafeVarargs
    private static <T> boolean hasOneOf(Collection<T> collection, T... elements) {
        return Arrays.stream(elements).anyMatch(collection::contains);
    }

    private void error(String message) {
        throw new RuntimeException(message + " in: " + declaration + " (RegEx: " + pattern.pattern() + "), (Imports: " + cachedImports + ')');
    }
}
