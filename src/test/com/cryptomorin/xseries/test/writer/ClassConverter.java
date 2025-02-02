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

package com.cryptomorin.xseries.test.writer;

import org.intellij.lang.annotations.Language;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Used for converting an enum system to a
 * {@link com.cryptomorin.xseries.base.XRegistry}/{@link com.cryptomorin.xseries.base.XModule} system.
 * <p>
 * TODO Support annotations for @XInfo and @XChange
 */
public final class ClassConverter {
    @SuppressWarnings({"unused", "RegExpRedundantEscape", "RegExpRepeatedSpace"})
    private static final Pattern ENUM_REPLACER = Pattern.compile(
            "(?<comments>    \\/\\*\\*\\s+(?:\\*[\\w\\. ]+\\s+)+\\*\\/\\n)?    (?<name>[A-Z_0-9]+)(?:\\((?<params>.+)\\))?[,;]");
    private static final Pattern PARAM_ENUMERATOR = Pattern.compile(
            "(?<before>[^\"]+)?(?<after>\".+)?");


    public static String replaceEnums(String txt, String xForm) {
        Matcher matcher = ENUM_REPLACER.matcher(txt);

        StringBuilder builder = new StringBuilder(1000);
        StringJoiner declr = null;

        while (matcher.find()) {
            String comments = matcher.group("comments");
            String name = matcher.group("name");
            String params = matcher.group("params");
            String indent = "    ";

            if (declr == null || comments != null) {
                if (declr != null) builder.append(declr);
                declr = new StringJoiner(",\n", indent + "public static final " + xForm + '\n', ";\n\n");
            }

            if (params == null || !params.toLowerCase(Locale.ENGLISH).contains('"' + name.toLowerCase(Locale.ENGLISH) + '"')) {
                if (params == null) params = '"' + name + '"';
                else {
                    // Make the field name the first name that is checked.
                    // $1 = everything before the name list.
                    // $2 = the name list.
                    Matcher mathcedParam = PARAM_ENUMERATOR.matcher(params);
                    if (mathcedParam.find()) {
                        String before = mathcedParam.group("before");
                        String after = mathcedParam.group("after");
                        params
                                = (before == null ? "" : before)
                                + (after == null ? ", " : "") // If there is more, then they put the comma for us
                                + ('"' + name + '"')
                                + (after == null ? "" : ", " + after);
                    }
                }
            }
            declr.add(indent + indent + name + " = std(" + params + ')');
            if (comments != null) {
                builder.append(comments);
                builder.append(declr);
                declr = null;
            }
        }
        if (declr != null) builder.append(declr);

        return builder.toString();
    }

    @SuppressWarnings("ALL")
    public static <T extends Enum<T>> void enumToRegistry(Path enumClass, Path writeTo) {
        // if (!enumClass.isEnum()) throw new IllegalArgumentException("Provided class is not an enum: " + enumClass);

        String xForm = enumClass.getFileName().toString().replaceFirst("[.][^.]+$", "");
        String bukkitForm = xForm.substring(1);

        try (BufferedWriter writer = Files.newBufferedWriter(
                writeTo.resolve(xForm + ".java"),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeTo(writer, "public final class XForm extends XModule<XForm, BForm> {}", xForm, bukkitForm, 1);
            writer.newLine();
            writeTo(writer, "    public static final XRegistry<XForm, BForm> REGISTRY = \n" +
                    "       new XRegistry<>(BForm.class, XForm.class, () -> Registry.UBForm, XForm::new, XForm[]::new);", xForm, bukkitForm);

            writer.newLine();
            writer.newLine();
            writer.write(replaceEnums(Files.readAllLines(enumClass).stream().collect(Collectors.joining("\n")), xForm));
            writer.newLine();
            writer.newLine();

            writeTo(writer, "    private XForm(BForm LBForm, String[] names) {\n" +
                    "        super(LBForm, names);\n" +
                    "    }", xForm, bukkitForm);
            writer.newLine();
            writer.newLine();

            writeTo(writer, "    @NotNull\n    public static XForm of(@NotNull BForm LBForm) {\n" +
                    "        return REGISTRY.getByBukkitForm(LBForm);\n" +
                    "    }\n" +
                    '\n' +
                    "    public static Optional<XForm> of(@NotNull String LBForm) {\n" +
                    "        return REGISTRY.getByName(LBForm);\n" +
                    "    }\n" +
                    '\n' +
                    "    @NotNull\n    public static XForm[] values() {\n" +
                    "        return REGISTRY.values();\n" +
                    "    }\n" +
                    '\n' +
                    "    @NotNull\n    private static XForm std(@NotNull String... names) {\n" +
                    "        return REGISTRY.std(names);\n" +
                    "    }", xForm, bukkitForm);

            writer.newLine();
            writer.write('}');
        } catch (IOException e) {
            throw new IllegalStateException("Failed write class conversion for " + enumClass + " -> " + writeTo, e);
        }
    }

    private static void writeTo(Writer writer, @Language("Java") String txt, String xForm, String bukkitForm) throws IOException {
        writeTo(writer, txt, xForm, bukkitForm, 0);
    }

    private static void writeTo(Writer writer, @Language("Java") String txt, String xForm, String bukkitForm, int deleteLast) throws IOException {
        writer.write(txt
                .substring(0, txt.length() - deleteLast)
                .replace("XForm", xForm)
                .replace("UBForm", bukkitForm.toUpperCase(Locale.ENGLISH))
                .replace("LBForm", bukkitForm.toLowerCase(Locale.ENGLISH))
                .replace("BForm", bukkitForm));
    }
}
