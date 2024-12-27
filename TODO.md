This file simply highlighs things that need to be done in future releases or other issues and bugs that a proper
solution is yet to be found. It's also a simple list of planned decisions for the future of the project that other
developers can see and perhaps give suggestions about. Anyone is welcome to complete any of the listed issues.

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

* **[ReflectiveHandleProxyProcessor]** Finish this class for people who wish to use the XReflection's direct API instead
  of using the annotations. This is useful for situations where annotations will seem bulky or the data is more complex
  and must be calculated during runtime.