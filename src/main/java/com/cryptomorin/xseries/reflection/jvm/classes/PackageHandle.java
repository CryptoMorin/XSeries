package com.cryptomorin.xseries.reflection.jvm.classes;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;

/**
 * A handle that can provide base package names or translate names based on current mappings.
 */
public interface PackageHandle {
    @Language("RegExp")
    @ApiStatus.Internal
    String JAVA_PACKAGE_PATTERN = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    @Language("RegExp")
    @ApiStatus.Internal
    String JAVA_IDENTIFIER_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    String packageId();

    String getBasePackageName();

    String getPackage(String packageName);
}
