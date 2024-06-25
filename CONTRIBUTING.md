I'm really thankful for all the bug fixes and performance improvements.\
Even if you changed a single line that has a good impact on the performance, it's welcomed.

Some changes may need a discussion about quality and usage.
Make sure to explain your changes clearly when creating a pull request.
All the pull requests are merged directly into the master branch.

This project imports the full Spigot JAR from an unofficial repo as SkullUtils uses `com.mojang.authlib`
It's also used for JavaX nullability annotations.

Do not make any PRs/issues regarding adding support for new Minecraft versions. I'll be usually finishing the update
within the first week of Paper unstable release.
Having multiple developers work on the same issue will be just a waste of time and resources.
You should also try not to make any PRs before support is added when a new Minecraft version comes out even
if it's unrelated to adding support for that version since your changes are likely to conflict with the update.

### Usage

This project uses Maven. So you'll have to use `mvn` commands.
In IntelliJ, you can press `Ctrl` twice for the command window to popup.
To compile the library into `target` folder, you can use this Maven command:

```maven
mvn package
```

To test the library using the latest Spigot server, you can use:

```maven
mvn clean package -Ptester,latest
```

> [!NOTE]
> The server files will be generated inside `target/tests` folder.\
> The common server settings used between tests are in `src/test/resources`

### Rules

* One of the main principles of XSeries is that each utility should be independent except the ones that cannot be
  independent. Functions such as the common ISFLAT boolean check should not depend on XMaterial's isNewVersion() except
  XBlock which is intended, since it already uses XMaterial for materials. Same for Particles and ParticleDisplay.
* Only Java 8 should be used. All the functions in the latest version of Java 8 can be used.
* Make sure the utility works on different Minecraft server versions.
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
* @datatags: [ParticleDisplay revamp](https://github.com/CryptoMorin/XSeries/pull/265) and for responding to various issues and reporting them.

I hope that I didn't forget anyone ;)