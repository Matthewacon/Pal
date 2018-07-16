# Pal
Pal is a compile-time only **L**ibrary that expands on the abilities of java compiler **A**nnotation **P**rocessor extensions. It allows for traditional source transformations as well as post-compile bytecode transformations, using the [ByteBuddy library](https://github.com/raphw/byte-buddy). Pal provides support for multithreaded annotation processing, for both source and bytecode, compiler event hooks, processor chaining, differentiated compile-time and runtime class definitions, and much more. The public API further allows for user defined pal annotations and processors that may be used throughout any project. 

# Clarification
> Pal is a compile-time only library

Upon compilation with the Pal library, all annotations defined through the Pal API will be stripped from the output classes of a project, removing any dependency on the library in production.

# Requirements
 - JDK 8, 9 or 10+
 - Gradle 4.8+
 - CMake 3.8+

# Building
`gradle clean build exampleJar`

# License
This project is licensed under the [M.I.T License](https://github.com/Matthewacon/Pal/blob/master/LICENSE)

# Disclaimer
This library is very much still under development and does not currently have a stable release. The API is subject to dramatic and breaking changes. Use at your own risk. 
