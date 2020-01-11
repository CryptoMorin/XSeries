# XSeries

XSeries is a set of utility classes mainly designed to provide cross-version support for Minecraft Bukkit servers.
But it also includes numerous extra methods to help developers design their plugins easier.
Some utilities are completely unrelated to cross-version support
such as NoteBlockMusic.

This project aims to provide quality utilities with high performance using the latest yet efficient techniques.
Although support for old versions (like 1.8) will still remain for future updates, I highly encourage all server owners
to update your servers to 1.12 if you're using an older version.


### Links

This project was mainly posted in [SpigotMC](https://www.spigotmc.org/threads/378136/)\
Most of the updates and news will be announced there.


### Getting Started

When compiling your plugin you should be using the latest version that your plugin is going to support.\
Which means, at least you have to use 1.13 (for cross-version support utilities only)
You can clone the project using `git clone https://github.com/CryptoMorin/XSeries.git`
If you're using maven and have issues with nullability annotations, you can import the following dependency to your project:
```xml
<dependency>
<groupId>com.google.code.findbugs</groupId>
<artifactId>jsr305</artifactId>
<version>3.0.2</version>
</dependency>
```
Or you can completely remove if it from the file by using a quick Ctrl+R replace. These annotations are purely used for
documentation purposes.

All the methods are explained in the JavaDoc. Please read the JavaDoc before using a method.
It's quite common to miss the whole purpose of cross-version support by using the wrong methods.

### Contributing

There's always room for improvement. If you know better ways of doing things, I really appreciate it if you can share it with me.
But please make sure you know what you're doing and tested the project on different versions.
Any new ideas are welcome as long as it's useful not just for you but for everyone else.
