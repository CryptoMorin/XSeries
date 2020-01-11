I'm really thankful for all the bug fixes and performance improvements.\
Even if you changed a single line that has a good impact on the performance, it's welcomed.

Some changes may need a discussion about quality and usage.
Make sure to explain your changes clearly when creating a pull request.
Most of the pull requests are merged directly into the master branch.


### Rules
* Make sure you know what you're doing and tested the utility on different versions.
* Do not use Java streams unless it's for a constant. Use for-each loops instead.
* Use method and variable names that make sense and are related to the context.
* Don't use Optional everywhere that can return null.
* Using Guava's lib and Apache Commons is a plus, but make sure that what you're using is supported in
older versions of Bukkit.
* Add JavaDocs with proper formatting. It's also preferred to explain how the complex parts of the method work
inside the method.
