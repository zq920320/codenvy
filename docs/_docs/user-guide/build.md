---
title: "Build"
excerpt: "Building and compiling projects with commands"
---
If your project has a built-in project type, Codenvy will install a series of type-specific commands that provide utilities for building projects. For example, Codenvy has a built-in maven project type that will install the Maven plug-in whenever one or more projects in your workspace are set with the maven project type.

Plug-in developers can optionally provide typed commands, which will appear in the Commands editor to simplify the creation of commands to perform compiling tasks. With Maven, this includes dependencies update, project tree view for external libraries, and maven flag interpolation.

For all other projects, you can write custom commands. Or, you can run command line processes through the terminal.

