This file simply highlighs things that need to be done in future releases or other issues and bugs that a proper
solution is yet to be found. It's also a simple list of planned decisions for the future of the project that other
developers can see and perhaps give suggestions about. Anyone is welcome to complete any of the listed issues.

* **[XMaterial]** We have to convert this into a XModule/XRegistry class soon. Wait and see what Spigot/Pape does first.

* **[Unit Tests]** Improve Maven's unit testing (Read #149 issue for more info.)

* **[XReflectASM/ReflectiveProxy]** Add more benchmarking for XReflection Stage III & IV with different method
  signatures, field and constructor tests.

* **[XReflectASM]** Clean XReflectASM class by perhaps splitting it? It's too crowded right now, or maybe that's just
  how it is because we're manipulating bytecode?

* **[XReflectASM]** Figure out a way to use **MagicAccessorImpl** for XReflectASM for more performance for
  private/protected members. you can look at the comments in `XReflectASM->MAGIC_ACCESSOR_IMPL` for more info about the
  current situation.

* **[XReflectASM]** Find a way to be able to use normal class fields and constructors instead of only interface methods
  with a solution that doesn't involve modifying the startup arguments like Java Agents for replacing classes that
  access ASM generated class fields/constructors.

* **[ReflectiveHandleProxyProcessor]** Finish this class for people who wish to use the XReflection's direct API instead
  of using the annotations. This is useful for situations where annotations will seem bulky or the data is more complex
  and must be calculated during runtime.

* **[ReflectiveProxyObject]** Add a way to ignore certain methods if they're not supported in a specific
  version instead of throwing an error (related to the issue below)

* **[ReflectiveProxyObject]** Add some sort of `boolean isSupported(String method, MethodType type)` to
  **ReflectiveProxyObject**. So we can test whether a method is supported in the current version or not.
  If we could make the method signature more simple, that'd be nice as well. For example, we could take the
  string approach and do `boolean isSupported(String signature)` to be used
  like `isSupported("private method(int a, String b, ...)")` that could work. A more strict approach using the
  already existing interface would've been nicer. For example, in JavaScript we could reference the method
  itself and add properties to it. I guess an ideal approach would be how you link methods in javadocs.

* **[ReflectiveProxyObject]** Add a simple enum class scanner that uses `@Proxify` and `@ReflectName` annotations to
  parse an enum class and map the correct values to field. How this value gets saved should be handled by
  an interface class like `interface A { void mapValue(Object) }` which our enum implements. Then we could
  also add a parent interface for **ReflectiveProxyObject** that enums also share, allowing us to use its
  `instance()` method. Which also means that we can use our enum class in other proxy methods.

* **[ReflectiveProxyObject]** Add interface inheritance support. (This might already be a thing, we just need to test)

* **[ReflectiveProxyObject]** Add support for inner classes.

* **[ReflectiveProxyObject]** Create a Maven and Gradle plugin for real class support that remaps fields, constructors
  and static member accesses to proxy access.

* **[XProxifier]** Implement configurable settings. (Read the TODO list inside that class for more info.)

* **[XProxifier]** Turn this into an IntelliJ plugin that allows proxifying any compiled file. Will probably
  need ASM to read the class file due to class loader issues? Not sure, needs to be tested.

* **[XProxifier]** Add a way to automatically generate mappings for different versions of a proxified class.
  We might define download links to mapping files, download the needed mapping, parse it and generate a class
  container for it?

* **[XTag]** Inline all fields. Using a `static {}` block is unnecessary and makes things really hard to track.
  Currently, this is not possible using IntelliJ's `Refactor -> Inline Field` feature, because you'll get a
  `No initializer present for the field` error. Not sure if this is a bug or some sort of tricky feature.
  Also, the formatting of the builder entries should in a way that the first entry is not in the same line as
  the builder's method. (No `FIELD = TagBuilder.simple(XMaterial.VALUE,\n`, but `FIELD = TagBuilder.simple(\n`)
  and each line should contain at least 3 entries, not one entry for each line.

* **[General]** Perhaps define a class named **XSeries** that contain general methods and information about the library
  including the current version and methods to enable/disable certain features that are currently handled by system
  properties? One thing we could do is to add an option to enable debug mode, certain exceptions that are normally
  suppressed are printed.

* **[General]** Don't forget to remove pre-12.0.0 deprecated codes in MC v1.22

* **[General]** Add a guide for Maven and Gradle users on how to properly exclude XSeries XReflection's III and IV proxy
  systems as many shade this library, and it's a pretty big extra code if you're not going to use it. Or maybe just
  split the entire XReflection API to a different project? Some of our own code here still relies on it though. We could
  include the library.

* **[General]** Adding a nice logo/banner to the main GitHub page would be nice, it feels quite empty right now. I will
  have to spend a lot of time to design one.

* **[Documentation]** While the javadocs are pretty comprehensive for most classes, they're mostly flooded with small
  and technical details that most developers don't have to be concerned about. We should make a guide on the wiki with
  screenshots and a general overview of all the features which makes it much easier for developers to get started.

* **[Mavne]** `[ERROR] MavenInvocationException: Error when invoking Maven, consult the invoker log file ...` when
  building the project. The log file simply says the dependencies couldn't be resolved? Seems to be related to Javadoc goal.
  Also `[ERROR] Error fetching link: XSeries/<project>/target/apidocs. Ignored it.` might be related to the previous error.
