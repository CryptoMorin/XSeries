/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Crypto Morin
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

package com.cryptomorin.xseries.reflection.proxy.generator;

import com.cryptomorin.xseries.reflection.XAccessFlag;
import com.cryptomorin.xseries.reflection.jvm.objects.ReflectedObject;
import com.cryptomorin.xseries.reflection.proxy.ReflectiveProxyObject;
import com.cryptomorin.xseries.reflection.proxy.annotations.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is used for scanning other compiled classes using reflection in order to generate template
 * interfaces classes for {@link com.cryptomorin.xseries.reflection.XReflection#proxify(Class)}.
 * <p>
 * Turn this into an IntelliJ plugin?
 * TODO Add a way to determine which referenced classes also require proxy classes
 *      and rename those, and add a method for generating a string for them as well.
 *
 * @since 14.0.0
 */
@SuppressWarnings("StringBufferField")
@ApiStatus.Internal
public final class XProxifier {
    private static final String MEMBER_SPACES = "    ";

    private final StringBuilder writer = new StringBuilder(1000);
    private final Set<String> imports = new HashSet<>(20);

    private final String proxifiedClassName;
    private final Class<?> clazz;

    /// / Settings //// TODO implement this
    private final boolean generateIntelliJAnnotations = true;
    private final boolean generateInaccessibleMembers = true;
    private final boolean copyAnnotations = true;
    private final boolean writeComments = true;
    private final boolean writeInfoAnnotationsAsComments = true; // like doing /* private static final */ before the member
    private boolean disableIDEFormatting;
    private Function<Class<?>, String> remapper; // useful for mapping classes to a hypothetical proxy class

    public XProxifier(Class<?> clazz) {
        this.clazz = clazz;
        this.proxifiedClassName = clazz.getSimpleName() + "Proxified";
        proxify();
    }

    private static Class<?> unwrapArrayType(Class<?> clazz) {
        while (clazz.isArray()) clazz = clazz.getComponentType();
        return clazz;
    }

    private void imports(Class<?> clazz) {
        clazz = unwrapArrayType(clazz);
        if (!clazz.isPrimitive() && !clazz.getPackage().getName().equals("java.lang"))
            imports.add(clazz.getName().replace('$', '.'));
    }

    private void writeComments(String... comments) {
        boolean multiLine = comments.length > 1;
        if (!multiLine) {
            writer.append("// ").append(comments[0]).append('\n');
        }

        writer.append("/**\n");

        for (String comment : comments) {
            writer.append(" * ");
            writer.append(comment);
            writer.append('\n');
        }

        writer.append(" */\n");
    }

    private void writeThrownExceptions(Class<?>[] exceptionTypes) {
        if (exceptionTypes == null || exceptionTypes.length == 0) return;
        writer.append(" throws ");
        StringJoiner exs = new StringJoiner(", ");
        for (Class<?> ex : exceptionTypes) {
            imports(ex);
            exs.add(ex.getSimpleName());
        }
        writer.append(exs);
    }

    private void writeMember(ReflectedObject jvm) {
        writeMember(jvm, false);
    }

    private void writeMember(ReflectedObject jvm, boolean generateGetterField) {
        writer.append(annotationsToString(true, true, jvm));

        Set<XAccessFlag> accessFlags = jvm.accessFlags();
        if (accessFlags.contains(XAccessFlag.PRIVATE)) writeAnnotation(Private.class);
        if (accessFlags.contains(XAccessFlag.PROTECTED)) writeAnnotation(Protected.class);
        if (accessFlags.contains(XAccessFlag.STATIC)) writeAnnotation(Static.class);
        if (accessFlags.contains(XAccessFlag.FINAL)) writeAnnotation(Final.class);

        switch (jvm.type()) {
            case CONSTRUCTOR:
                writeAnnotation(com.cryptomorin.xseries.reflection.proxy.annotations.Constructor.class);
                writeAnnotation("NotNull");

                Constructor<?> ctor = (Constructor<?>) jvm.unreflect();
                String contractParams = Arrays.stream(ctor.getParameterTypes()).map(x -> "_").collect(Collectors.joining(", "));
                writeAnnotation("Contract",
                        "value = \"" + contractParams + " -> new\"",
                        "pure = true"
                );

                break;
            case FIELD:
                writeAnnotation(com.cryptomorin.xseries.reflection.proxy.annotations.Field.class);
                if (generateGetterField) {
                    writeAnnotation("Contract", "pure = true");
                } else {
                    writeAnnotation("Contract", "mutates = \"this\"");
                }
                break;
        }

        StringJoiner parameters = new StringJoiner(", ", "(", ")");
        Class<?>[] exceptionTypes = null;
        writer.append(MEMBER_SPACES);
        switch (jvm.type()) {
            case CONSTRUCTOR:
                Constructor<?> constructor = (Constructor<?>) jvm.unreflect();
                exceptionTypes = constructor.getExceptionTypes();
                writer.append(proxifiedClassName).append(' ').append("construct");
                writeParameters(parameters, constructor.getParameters());
                break;
            case FIELD:
                Field field = (Field) jvm.unreflect();
                imports(field.getType());

                if (generateGetterField) {
                    writer.append(field.getType().getSimpleName());
                } else {
                    writer.append("void");
                    parameters.add(field.getType().getSimpleName() + " value");
                }

                writer.append(' ');
                writer.append(jvm.name());
                break;
            case METHOD:
                Method method = (Method) jvm.unreflect();
                exceptionTypes = method.getExceptionTypes();

                imports(method.getReturnType());

                writer.append(method.getReturnType().getSimpleName());
                writer.append(' ');
                writer.append(jvm.name());
                writeParameters(parameters, method.getParameters());
                break;
        }

        writer.append(parameters);
        writeThrownExceptions(exceptionTypes);
        writer.append(";\n\n");
    }

    /**
     * Boxes primitive types into an object because a primitive array like int[] cannot be cast to Object[]
     */
    private static Object[] getArray(Object val) {
        if (val instanceof Object[]) return (Object[]) val;
        int arrlength = Array.getLength(val);
        Object[] outputArray = new Object[arrlength];
        for (int i = 0; i < arrlength; ++i) {
            outputArray[i] = Array.get(val, i);
        }
        return outputArray;
    }

    private String constantToString(Object obj) {
        if (obj instanceof String) return '"' + obj.toString() + '"';
        if (obj instanceof Class) {
            Class<?> clazz = (Class<?>) obj;
            imports(clazz);
            return clazz.getSimpleName() + ".class";
        }
        if (obj instanceof Annotation) {
            Annotation annotation = (Annotation) obj;
            return annotationToString(annotation);
        }
        if (obj.getClass().isEnum()) {
            imports(obj.getClass());
            return obj.getClass().getSimpleName() + '.' + ((Enum<?>) obj).name();
        }
        if (obj.getClass().isArray()) {
            // Multidimensional arrays aren't allowed in annotations.
            Object[] array = getArray(obj);
            StringJoiner builder;
            if (array.length == 0) return "{}";
            if (array.length == 1) builder = new StringJoiner(", ");
            else builder = new StringJoiner(", ", "{", "}");

            for (Object element : array) {
                builder.add(constantToString(element));
            }

            return builder.toString();
        }

        // Numbers and booleans
        return obj.toString();
    }

    private String annotationsToString(boolean member, boolean newLine, AnnotatedElement annotatable) {
        StringJoiner builder = new StringJoiner(
                (newLine ? '\n' : "") + (member ? MEMBER_SPACES : ""),
                (member ? MEMBER_SPACES : ""),
                (newLine ? "\n" : "")
        ).setEmptyValue("");

        for (Annotation annotation : annotatable.getAnnotations()) {
            Annotation[] unwrapped = unwrapRepeatElement(annotation);
            if (unwrapped != null) {
                for (Annotation inner : unwrapped) {
                    builder.add(annotationToString(inner));
                }
            } else {
                builder.add(annotationToString(annotation));
            }
        }

        return builder.toString();
    }

    private static Annotation[] unwrapRepeatElement(Annotation annotation) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod("value");
            if (method.getReturnType().isArray()) {
                // Multidimensional arrays aren't allowed, but we will use this just in case.
                Class<?> rawReturn = unwrapArrayType(method.getReturnType());
                if (rawReturn.isAnnotation()) {
                    Repeatable repeatable = rawReturn.getAnnotation(Repeatable.class);
                    if (repeatable != null && repeatable.value() == annotation.annotationType()) {
                        try {
                            return (Annotation[]) method.invoke(annotation);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    private String annotationToString(Annotation annotation) {
        List<String> builder = new ArrayList<>();
        boolean visitedValue = false;

        for (Method entry : annotation.annotationType().getDeclaredMethods()) {
            try {
                entry.setAccessible(true);
                String key = entry.getName();
                Object value = entry.invoke(annotation);
                try {
                    @Nullable Object defaultValue = entry.getDefaultValue();

                    // The default value isn't directly passed, they're not actually identical.
                    if (defaultValue != null) {
                        if (defaultValue.getClass().isArray()) {
                            if (Arrays.equals(getArray(defaultValue), getArray(value))) continue;
                        } else {
                            if (value.equals(defaultValue)) continue;
                        }
                    }
                } catch (TypeNotPresentException ignored) {
                    // If it's not an annotation.
                }

                if (key.equals("value")) visitedValue = true;
                builder.add(key + " = " + constantToString(value));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to get annotation value " + entry, e);
            }
        }

        imports(annotation.annotationType());
        String annotationValues;
        if (builder.isEmpty()) annotationValues = "";
        else if (builder.size() == 1 && visitedValue) {
            annotationValues = builder.get(0);
            int equalsSign = annotationValues.indexOf('=');
            annotationValues = '(' + annotationValues.substring(equalsSign + 2) + ')';
        } else {
            annotationValues = '(' + String.join(", ", builder) + ')';
        }

        return '@' + annotation.annotationType().getSimpleName() + annotationValues;
    }

    private StringJoiner writeParameters(StringJoiner joiner, Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            imports(parameter.getType());

            String type;
            if (parameter.isVarArgs()) {
                type = parameter.getType().getSimpleName() + "... ";
            } else {
                type = parameter.getType().getSimpleName();
            }

            String annotations = annotationsToString(false, false, parameter);
            joiner.add(annotations + (annotations.isEmpty() ? "" : " ") + type + ' ' + parameter.getName());
        }
        return joiner;
    }

    private void writeAnnotation(Class<?> annotation, String... values) {
        writeAnnotation(true, annotation, values);
    }

    private void writeAnnotation(boolean member, Class<?> annotation, String... values) {
        imports(annotation);
        writeAnnotation(member, annotation.getSimpleName(), values);
    }

    private void writeAnnotation(String annotation, String... values) {
        writeAnnotation(true, annotation, values);
    }

    private void writeAnnotation(boolean member, String annotation, String... values) {
        if (member) writer.append(MEMBER_SPACES);
        writer.append('@').append(annotation);
        if (values.length != 0) {
            StringJoiner valueJoiner = new StringJoiner(", ", "(", ")");
            for (String value : values) valueJoiner.add(value);
            writer.append(valueJoiner);
        }
        writer.append('\n');
    }

    private void proxify() {
        if (disableIDEFormatting) {
            // This is intentionally written like this because IntelliJ will even recognize this
            // text sequence even if it's not written as a comment.
            writer.append("// ").append("@formatter:").append("OFF").append('\n');
        }

        if (writeComments) {
            writeComments(
                    "This is a generated proxified class for " + clazz.getSimpleName() + ". However, you might",
                    "want to review each member and correct its annotations when needed.",
                    "<p>",
                    "It's also recommended to use your IDE's code formatter to adjust",
                    "imports and spaces according to your settings.",
                    "In IntelliJ, this can be done by with Ctrl+Alt+L",
                    "<p>",
                    "Full Target Class Path:",
                    clazz.getName()
            );
        }

        writer.append(annotationsToString(false, true, clazz));

        writeAnnotation(
                false,
                Proxify.class,
                "target = " + clazz.getSimpleName() + ".class"
        );
        if (!XAccessFlag.PUBLIC.isSet(clazz.getModifiers())) {
            writeAnnotation(false, Private.class);
        }
        if (XAccessFlag.FINAL.isSet(clazz.getModifiers())) {
            writeAnnotation(false, Final.class);
            writeAnnotation(false, "ApiStatus.NonExtendable");
        }
        writer
                .append("public interface ")
                .append(proxifiedClassName)
                .append(" extends ")
                .append(ReflectiveProxyObject.class.getSimpleName())
                .append(" {\n");

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isSynthetic()) continue;
            if (!XAccessFlag.FINAL.isSet(field.getModifiers())) {
                writeMember(ReflectedObject.of(field), false);
            }
            writeMember(ReflectedObject.of(field), true);
        }
        if (declaredFields.length != 0) writer.append('\n');

        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : declaredConstructors) {
            if (constructor.isSynthetic()) continue;
            writeMember(ReflectedObject.of(constructor));
        }
        if (declaredConstructors.length != 0) writer.append('\n');

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getDeclaringClass() == Object.class) continue;
            if (method.isSynthetic()) continue;
            if (method.isBridge()) continue;
            writeMember(ReflectedObject.of(method));
        }

        writer.append('\n');
        writeAnnotation(Ignore.class);
        writeAnnotation("NotNull");
        writeAnnotation("ApiStatus.OverrideOnly");
        writeAnnotation("Contract",
                "value = \"_ -> new\"",
                "pure = true"
        );
        writer.append(MEMBER_SPACES).append(proxifiedClassName).append(" bindTo(@NotNull Object instance);\n");

        writer.append("}\n");
        finalizeString();
    }

    /**
     * After gathering all analysis data (currently only imports), construct the final string.
     */
    private void finalizeString() {
        StringBuilder whole = new StringBuilder(writer.length() + (imports.size() * 100));
        whole.append("import org.jetbrains.annotations.*;\n");
        // whole.append("import ").append(com.cryptomorin.xseries.reflection.proxy.annotations.Field.class.getPackage().getName()).append(".*;\n");

        List<String> sortedImports = new ArrayList<>(imports);
        sortedImports.sort(Comparator.naturalOrder());

        for (String anImport : sortedImports) {
            whole.append("import ").append(anImport).append(";\n");
        }

        whole.append('\n');
        this.writer.insert(0, whole);
        imports(ReflectiveProxyObject.class);
    }


    public String getString() {
        if (writer.length() == 0) proxify();
        return writer.toString();
    }

    public void writeTo(Path path) {
        if (Files.isDirectory(path)) {
            path = path.resolve(proxifiedClassName + ".java");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(getString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
