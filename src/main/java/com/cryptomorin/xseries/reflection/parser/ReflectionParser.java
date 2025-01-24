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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
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
 * supports more simplified syntax (for example not requiring semicolons) which even works
 * much better if you're using IntelliJ because of the string highlighting.
 * <h2>Performance & Caching</h2>
 * All XReflection APIs that use this feature, use heavy RegEx patterns which sacrifices
 * a lot of performance for readability. Please read {@link XReflection}'s <b>Performance & Caching</b>
 * section for more information about how to properly cache this.
 * <p>
 * TODO Add better support for inner classes. Read {@link #includeInnerClassOf} for more info.
 *
 * @see ReflectiveNamespace#classHandle(String)
 * @see ReflectiveNamespace#ofMinecraft(String)
 * @see ClassHandle#constructor(String)
 * @see ClassHandle#method(String)
 * @see ClassHandle#field(String)
 * @see MemberHandle#signature(String)
 */
@ApiStatus.Internal
public final class ReflectionParser {
    private static final String[] DEFAULT_CHECKED_PACKAGES = {"java.util", "java.util.function", "java.lang", "java.io"};

    private final String declaration;
    private Pattern pattern;
    private Matcher matcher;
    private ReflectiveNamespace namespace;
    private Map<String, Class<?>> cachedImports;
    private String[] checkedPackages = DEFAULT_CHECKED_PACKAGES;
    private final Set<Flag> flags = EnumSet.noneOf(Flag.class);
    private static final PackageHandle[] PACKAGE_HANDLES = MinecraftPackage.values();

    public ReflectionParser(@Language("Java") String declaration) {
        this.declaration = declaration;
    }

    public ReflectionParser checkedPackages(@org.intellij.lang.annotations.Pattern(PackageHandle.JAVA_PACKAGE_PATTERN) String... checkedPackages) {
        this.checkedPackages = checkedPackages;
        return this;
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

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    @Language("RegExp")
    private static final String
            GENERIC = "(?:\\s*<\\s*[.\\w<>\\[\\], ]+\\s*>)?",
            ARRAY = "(?:(?:\\[])*)",
            PACKAGE_REGEX = "(?:package\\s+(?<package>" + PackageHandle.JAVA_PACKAGE_PATTERN + ")\\s*;\\s*)?",
            CLASS_TYPES = "(?<classType>class|interface|enum|record)",
            PARAMETERS = "\\s*\\(\\s*(?<parameters>[\\w$_,.<?>\\[\\] ]+)?\\s*\\)",
            THROWS = "(?:\\s*throws\\s+(?<throws>(?:" + type(null).array(false) + ")(?:\\s*,\\s*" + type(null).array(false) + ")*))?",
            END_DECL = "\\s*;?\\s*";

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    private static final Pattern
            CLASS = Pattern.compile(PACKAGE_REGEX + Flag.FLAGS_REGEX + CLASS_TYPES + "\\s+" + type("className")
            + "(?:\\(\\))?" + // for record classes, the fields should not be specified
            "(?:\\s+extends\\s+" + id("superclasses") + ")?" +
            "(?:\\s+implements\\s+(?<interfaces>(?:" + type(null).array(false) + ")(?:\\s*,\\s*" + type(null).array(false) + ")*))?" +
            "(?:\\s*\\{\\s*})?\\s*");
    private static final Pattern METHOD = Pattern.compile(Flag.FLAGS_REGEX + type("methodReturnType") + "\\s+"
            + id("methodName") + PARAMETERS + THROWS + END_DECL);
    private static final Pattern CONSTRUCTOR = Pattern.compile(Flag.FLAGS_REGEX + "\\s+"
            + id("className") + PARAMETERS + END_DECL);
    private static final Pattern FIELD = Pattern.compile(Flag.FLAGS_REGEX + type("fieldType") + "\\s+"
            + id("fieldName") + END_DECL);

    private static IDHandler id(@NotNull @Language("RegExp") String groupName) {
        return new IDHandler(groupName, false);
    }

    private static IDHandler type(@Language("RegExp") String groupName) {
        return new IDHandler(groupName, true).generic(true).array(true);
    }

    private static final class IDHandler {
        private boolean generic, array;
        private final String groupName;
        private final boolean isFullyQualified;

        private IDHandler(String groupName, boolean isFullyQualified) {
            this.groupName = groupName;
            this.isFullyQualified = isFullyQualified;
        }

        public IDHandler generic(boolean generic) {
            this.generic = generic;
            return this;
        }

        public IDHandler array(boolean array) {
            this.array = array;
            return this;
        }

        @Override
        public String toString() {
            String type = (isFullyQualified ? PackageHandle.JAVA_PACKAGE_PATTERN : PackageHandle.JAVA_IDENTIFIER_PATTERN)
                    + (generic ? GENERIC : "")
                    + (array ? ARRAY : "");

            if (groupName == null) return "(?:" + type + ')';
            return "(?<" + groupName + '>' + type + ')';
        }
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
                // Primitives
                byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class, void.class,

                // Primitives (Boxxed)
                // This is included in java.lang yes, but we still put them here.
                Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class, Void.class,

                // java.lang
                Object.class, String.class, CharSequence.class, StringBuilder.class, StringBuffer.class, UUID.class, Optional.class,
                Map.class, HashMap.class,

                // Time API (java.util + java.time)
                Date.class, Calendar.class, Duration.class, TimeUnit.class,

                // java.nio.file
                Path.class, Files.class,

                // java.util.concurrent
                ConcurrentHashMap.class, Callable.class, Future.class, CompletableFuture.class,

                // Exceptions
                Throwable.class, Error.class, Exception.class, IllegalArgumentException.class, IllegalStateException.class
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

        Class<?> clazz = stringToClass(typeName);

        // if (clazz == null) error("Unknown type '" + firstTypeName + "' -> '" + typeName + '\'');
        if (clazz == null) return new UnknownClassHandle(getOrCreateNamespace(), firstTypeName + " -> " + typeName);
        if (arrayDimension != 0) {
            clazz = XReflection.of(clazz).asArray(arrayDimension).unreflect();
        }
        return new StaticClassHandle(getOrCreateNamespace(), clazz);
    }

    @Nullable
    private Class<?> stringToClass(String typeName) {
        Class<?> clazz = null;
        if (!typeName.contains(".")) {
            // Override predefined types
            if (cachedImports != null) clazz = this.cachedImports.get(typeName);
            if (clazz == null) clazz = PREDEFINED_TYPES.get(typeName);
            // if (clazz == null) {
            if (clazz == null && checkedPackages != null) {
                for (String checkedPackage : checkedPackages) {
                    boolean inner = checkedPackage.endsWith("$");
                    clazz = classNamed(checkedPackage + (inner ? "" : '.') + typeName);
                    if (clazz != null) break;
                }
            }
        }

        if (clazz == null) clazz = classNamed(typeName);
        return clazz;
    }

    private static Class<?> classNamed(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
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
                    if (packageName.indexOf('.') == -1) {
                        classHandle.inPackage(pkgHandle);
                    } else {
                        classHandle.inPackage(pkgHandle, packageName.substring(targetPackageName.length() + 1)); // + 1 for the dot
                    }
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

    private void includeInnerClassOf(MemberHandle handle) {
        Class<?> clazz = handle.getClassHandle().reflectOrNull();
        if (clazz == null) return;

        int len = DEFAULT_CHECKED_PACKAGES.length + 2;
        this.checkedPackages = Arrays.copyOf(DEFAULT_CHECKED_PACKAGES, len);
        this.checkedPackages[len - 1] = clazz.getName() + '$';
        this.checkedPackages[len - 2] = clazz.getPackage().getName();

        // The second one is also useful in case the name of the class is only relatively qualified.
        // As in using "MethodHandles.Lookup" instead of just "Lookup", however this is currently not
        // reliable as inner classes must be separated with $ instead of . for Class.forName() to work
        // and we don't know where the package ends and where the class names begin. We can't just assume there'll be only inner
        // classes to replace the dots, fully qualified inner classes (e.g. java.lang.invoke.MethodHandles.Lookup)
        // may be used as well, and we cannot test every combination of dots and dollar signs to see which one matches.
    }

    public <T extends ConstructorMemberHandle> T parseConstructor(T ctorHandle) {
        includeInnerClassOf(ctorHandle);
        pattern(CONSTRUCTOR, ctorHandle);

        if (has("className") && !ctorHandle.getClassHandle().getPossibleNames().contains(group("className"))) {
            error("Wrong class name associated to constructor, possible names: " + ctorHandle.getClassHandle().getPossibleNames());
        }
        if (has("parameters")) ctorHandle.parameters(parseTypes(group("parameters").split(",")));
        return ctorHandle;
    }

    public <T extends MethodMemberHandle> T parseMethod(T methodHandle) {
        includeInnerClassOf(methodHandle);
        pattern(METHOD, methodHandle);

        // String classGeneric = parser.group("generic");
        methodHandle.named(group("methodName").split("\\$"));
        methodHandle.returns(parseType(group("methodReturnType")));
        if (has("parameters")) methodHandle.parameters(parseTypes(group("parameters").split(",")));

        return methodHandle;
    }

    public <T extends FieldMemberHandle> T parseField(T fieldHandle) {
        includeInnerClassOf(fieldHandle);
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
            if (handle instanceof FieldMemberHandle && flags.contains(Flag.FINAL)) {
                ((FieldMemberHandle) handle).asFinal();
            }
            if (handle instanceof FlaggedNamedMemberHandle && flags.contains(Flag.STATIC)) {
                ((FlaggedNamedMemberHandle) handle).asStatic();
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
        throw new ReflectionParserException(message + " in: " + declaration + " (RegEx: " + pattern.pattern() + "), (Imports: " + cachedImports + ')');
    }

    public static final class ReflectionParserException extends RuntimeException {
        public ReflectionParserException(String message) {
            super(message);
        }
    }
}
