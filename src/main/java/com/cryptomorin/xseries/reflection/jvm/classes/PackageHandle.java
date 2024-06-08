package com.cryptomorin.xseries.reflection.jvm.classes;

import org.intellij.lang.annotations.Language;

public interface PackageHandle {
    @Language("RegExp")
    String JAVA_PACKAGE_PATTERN = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    @Language("RegExp")
    String JAVA_IDENTIFIER_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    String packageId();

    String getBasePackageName();

    String getPackage(String packageName);
}
