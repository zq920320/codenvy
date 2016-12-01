---
title: "Commands"
excerpt: ""
---
Commands are script-like instructions that are injected into the workspace machine for execution. Commands are executed at by selecting a command from the IDE toolbar `CMD` drop down. You can add or edit commands at `Run > Edit Commands` or `CMD > Edit Commands` drop down. 

Commands are saved in the configuration storage of your workspace and will be part of any workspace export. 

Commands have type, simile to projects. Plug-in authors can register different command types that will inject additional behaviors into the command when it is executed. For example, Codenvy provides a `maven` command type for any project that has the maven project type. Maven commands have knowledge of how maven works and will auto-set certain flags and simplify the configuration.

