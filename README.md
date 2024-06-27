# XSeries

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-dark_green.svg)](https://shields.io/)
[![Java](https://img.shields.io/badge/Java-8-dark_green.svg)](https://shields.io/)
[![Build Status](https://api.travis-ci.com/CryptoMorin/XSeries.svg?branch=master)](https://app.travis-ci.com/github/CryptoMorin/XSeries)
![maven-central](https://img.shields.io/maven-central/v/com.github.cryptomorin/XSeries)
[![CodeFactor](https://www.codefactor.io/repository/github/cryptomorin/xseries/badge)](https://www.codefactor.io/repository/github/cryptomorin/xseries)

Library mainly designed to provide cross-version support for Minecraft Bukkit plugins,
but it also includes numerous extra methods to help developers design their plugins easier and efficiently.
Some utilities are completely unrelated to cross-version support such as NoteBlockMusic.

Don't forget to add `api-version: "1.13"` to your `plugin.yml`.
This will keep the plugin working even if the server is not 1.13

This project aims to provide quality utilities with high performance using the latest, yet efficient techniques.
Although support for old versions (like 1.8) will still remain for future updates, I highly encourage all developers
drop support for anything below 1.12

### Links

This project was mainly posted in [SpigotMC](https://www.spigotmc.org/threads/378136/)\
Most of the updates and news will be announced there.

### Getting Started

When compiling your plugin you should be using the latest version that your plugin is going to support.\
Which means, at least you have to use 1.13 (for cross-version support utilities only)
You can clone the project using: `git clone https://github.com/CryptoMorin/XSeries.git`

All the methods are explained in the JavaDocs. Please read them before using a method.
It's quite common to miss the whole purpose of cross-version support and the efficiency
of the utility by using the wrong methods.

You can use most of these utilities individually or use the maven dependency.
Most of the utilities are intended to be independent. However, some
utilities such as [XParticle](src/main/java/com/cryptomorin/xseries/particles/XParticle.java) are intended to use
another class ([ParticleDisplay](src/main/java/com/cryptomorin/xseries/particles/ParticleDisplay.java))

#### Maven ![maven-central](https://img.shields.io/maven-central/v/com.github.cryptomorin/XSeries)

```xml

<dependency>
    <groupId>com.github.cryptomorin</groupId>
    <artifactId>XSeries</artifactId>
    <version>version</version>
</dependency>

<!-- If you want to use XSkull and you use 'spigot-api' dependency you need the following: -->
<repository>
    <id>minecraft-libraries</id>
    <name>Minecraft Libraries</name>
    <url>https://libraries.minecraft.net/</url>
</repository>
<dependency>
<groupId>com.mojang</groupId>
    <artifactId>authlib</artifactId>
    <version>6.0.54</version>
    <scope>provided</scope>
</dependency>
```

Gradle

```kotlin
repositories {
    mavenCentral()
}
dependencies {
    implementation("com.github.cryptomorin:XSeries:version")
}
```

You shouldn't worry if the reflection or other classes are going to use your memory with heavy useless static cache.
As long as you don't use them anywhere in your code, they won't initialize.
The memory usage of these utilities are extremely enhanced.

# Shading

DO NOT extract the JAR into your project if you're using maven. You have to shade the library,
otherwise your plugin or other plugins will break due to version mismatch.
To shade the library, add the following under your maven plugins:

### [Apache Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <relocations>
            <relocation>
                <pattern>com.cryptomorin.xseries</pattern>
                <!-- Be sure to change the package below -->
                <shadedPattern>my.plugin.utils</shadedPattern>
            </relocation>
        </relocations>
        <!-- Here you can remove the classes you don't use. -->
        <!-- These are some examples. -->
        <!-- The "unused" package and SkullCacheListener are excluded by default. -->
        <!-- Some utilities such a XItemStack depend on more than 3 other classes, so watch out. -->
        <filters>
            <filter>
                <artifact>*:*</artifact>
                <excludes>
                    <exclude>com/cryptomorin/xseries/XBiome*</exclude>
                    <exclude>com/cryptomorin/xseries/NMSExtras*</exclude>
                    <exclude>com/cryptomorin/xseries/NoteBlockMusic*</exclude>
                    <exclude>com/cryptomorin/xseries/SkullCacheListener*</exclude>
                </excludes>
            </filter>
        </filters>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### [Gradle Shadow plugin](https://imperceptiblethoughts.com/shadow/)

Gradle (Kotlin DSL)

```kotlin
plugins {
    java
    id("com.github.johnrengelman.shadow") version ("8.1.1")
}

tasks {
    shadowJar {
        relocate("com.cryptomorin.xseries", "my.plugin.utils")
    }
}
```

Gradle (Groovy)

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
    id 'java'
}

tasks {
    shadowJar {
        relocate 'com.cryptomorin.xseries', 'my.plugin.utils'
    }
}
```

### Contributing

There's always room for improvement. If you know better ways of doing things,
I really appreciate it if you can share it with me,
but please make sure you know what you're doing and tested the project on different versions.
Any new ideas are welcome as long as they're useful; not just for you, but for everyone else.\
Please refer to [contributing guidelines](CONTRIBUTING.md) for more info.
