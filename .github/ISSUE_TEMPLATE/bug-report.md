---
name: Bug Report
about: Create a bug report.
title: "[Utility Class Name] Short Issue Description"
labels: 'bug'
assignees: CryptoMorin
---

Example of a good bug report: https://github.com/CryptoMorin/XSeries/issues/376

## Description

A clear and concise description of what the bug is.

**Note:** Issues caused by plugins such ProtocolSupport or ViaVersion
should be reported to the plugin developers, not here.

## Code

* A minimal code to reproduce this issue.
* It should not contain any code from your plugin's classes or external libraries.
* You should write a code with pure Java + Bukkit API + XSeries.

```java
// Code here (Kotlin, Scala or Groovy is fine too)
```

## Error

If you get any errors, put them in a spoiler tag below:

<details>
  <summary>Error stacktrace</summary>

  ```hs
  Error goes here 
  
  (Make sure its properly indented by selecting all
  the error text and pressing TAB or Shift+TAB) 
  ```

</details>

* Do not use pasting websites such as [Pastebin](https://pastebin.com/).
* Try not to include any logs (that are not stacktraces) which are unrelated to the library.

## Version

* **Server:** Your server version information from `/ver` command. It must be using the latest version of that major
  update.
  E.g. 1.14, 1.14.1, 1.14.2 and 1.14.3 are not supported, only 1.14.4 is supported.
* **XSeries:** Specify the version (or commit if you're using unreleased JitPack builds) Make sure you're using the
  latest utility/maven version.

**Do not** make requests regarding support for newly released Minecraft versions
(read [contributing guidelines](/CONTRIBUTING.md) for more info).