> [!NOTE]
> All feature related contributions to XReflection and its subclasses
> are paused until XReflection is moved to its new separate project.

I'm really thankful for all the bug fixes and performance improvements.\
Even if you changed a single line that has a good impact on the performance, it's welcomed.

Some changes may need a discussion about quality and usage.
Make sure to explain your changes clearly when creating a pull request.
All the pull requests are merged directly into the master branch.

This project imports the full Spigot JAR from an unofficial repo as `XSkull` uses `com.mojang.authlib`
It's also used for JavaX nullability annotations and for easier access to NMS/internal code.

Do not make any PRs/issues regarding adding support for new Minecraft versions. I'll be usually finishing the update
within the first week of Paper unstable release.
Having multiple developers work on the same issue will be just a waste of time and resources.
You should also try not to make any PRs before support is added when a new Minecraft version comes out even
if it's unrelated to adding support for that version since your changes are likely to conflict with the update.

### Usage

This project uses a Maven aggregator project. So you'll have to use **mvn** commands.
In IntelliJ, you can press `Ctrl` twice for the command window to popup.
To compile the library into `target` folder, you can use this Maven command:

```maven
mvn clean package -pl core -am -DskipTests=true -Dmaven.test.skip=true
```

> [!NOTE]
> Since this is a Maven aggregator project, there are a few more command arguments:
> - **-pl core** also can be used as **--projects core**, means that we only want to `clean package` the `core`
    subproject, not all subproject.
> - **-am** (also named **--also-make**) Builds all other subprojects, and any of their dependencies which are required
    for the `core` subproject.
>
> For more information about Maven aggregator projects,
> visit [Apache Maven's Guide](https://maven.apache.org/guides/mini/guide-multiple-subprojects-4.html).

To test the library using the latest Spigot server, you can use:

```maven
mvn clean test -Ptester,latest -pl core -am
```

If you want to test older versions, you could for example use:
```maven
mvn clean exec:exec@compile exec:exec@test -DtestVer=21 --projects core -am
```

> [!NOTE]
> The server files will be generated inside `target/tests` folder.\
> The common server settings used between tests are in `src/test/resources`

### Rules

* ~~One of the main principles of XSeries is that each utility should be independent except the ones that cannot be
  independent. Functions such as the common ISFLAT boolean check should not depend on XMaterial's isNewVersion() except
  XBlock which is intended, since it already uses XMaterial for materials. Same for Particles and ParticleDisplay.~~
  This is no longer the case because of Minecraft's new registry system. It's going to make a lot of boilerplate code
  if we decide to stick to this principle.
* Only Java 8 should be used. All the functions in the latest version of Java 8 can be used.
* Make sure the utility works on different Minecraft server versions. Usually outdated patches should not be supported.
  For example, in the `1.21` series, only `1.21.5` should be supported not `1.21.4` or `1.21.1` there are of course some
  exceptions to this rule like enum values.
* Use method and variable names that make sense and are related to the context.
* Don't use Optional everywhere that can return null.
* Using Google's Guava is a plus, but not always. Make sure what you're using is supported in
  older versions of Bukkit's libraries. Don't use other libraries included in Bukkit, specially Apache Commons
  since it was removed/replaced.
* Add JavaDocs with proper formatting. It's also preferred to explain how the complex parts of a method work
  inside the method. Use simple English when possible. Do not add comments to things that are self-explanatory,
  for example do not add a `@return` tag to a function `public void isSupported()` if all you're going to write
  is going to be something like "returns true if this is supported, otherwise false" sometimes functions don't
  even need the `@return` tag because their return value is pretty much obvious from the main description.
* All the functions used in the utilities should be compatible with Bukkit, Spigot and Paper.
  Using extra methods from Spigot is a plus as long as it supports Bukkit, but do not use any methods that are included
  in any forks of Spigot.
* Change the class version properly. If you're not sure how versioning works, don't change it.
* Do not attempt to support versions older than 1.8 even if it can be fixed with a single line.
* Do not use one-liner if statements if it doesn't fit the screen.
* Try to avoid streams. Mostly for frequently used methods.

# Special Thanks

I'd like to express my profound gratitude to the following people for really helping this project to grow:

* @Condordito: [SkullUtils/XSkull revamp](https://github.com/CryptoMorin/XSeries/issues/254)
* @HSGamer: [XParticle/ParticleDisplay issues](https://github.com/CryptoMorin/XSeries/commits?author=HSGamer)
* @DeadSilenceIV: [XItemStack issues](https://github.com/CryptoMorin/XSeries/commits?author=DeadSilenceIV)
* @AV3RG: [XTag](https://github.com/CryptoMorin/XSeries/commit/988fee3a0fc80697f99804ca7c13108976f26acd)
* @SirLeezus: [XItemStack issues](https://github.com/CryptoMorin/XSeries/commits?author=SirLeezus)
* @datatags: [ParticleDisplay revamp](https://github.com/CryptoMorin/XSeries/pull/265) and for responding to various
  issues and reporting them.

I hope that I didn't forget anyone ;)