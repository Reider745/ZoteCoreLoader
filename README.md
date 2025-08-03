[Russian](README-RU.md)

# Zote Core Loader

__Zote Core__ — A server core based on Inner Core 1.16.200, designed to create a unique gameplay with mods.

The server core is based on [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT ), it can be easily updated by replacing the corresponding library.

![Zote Core](/.github/logo.png)

## Installation

Any platform supporting Java 17 or higher is supported. Download the latest release of the server core, or build a repository using Gradle.

Run the assembled kernel by opening the console in the folder with it:

```sh
java -jar ZoteCore-SNAPSHOT.jar
```

## Configuration

The settings are made using two main files: *server.properties* and *zotecore.yml*. They will be created automatically immediately after the kernel is launched, the documentation for the first one is available in the Nukkit-MOT repository.

Some settings of Nukkit are blocked by the loader, including *multiversion-min/max-protocol* (422), *xbox-auth* (authorization using it is not available) and *save-player-data-by-uuid* (false).

To make client sockets fall off without crashing the server, disable Watchdog. In the current implementation, alternative fix options are not available, the necessary setting is `thread-watchdog=on'.

### zotecore.yml

Standard kernel settings that can be used as a basis:

```yml zotecore.yml
# Change resolution of pack that client should have to log into server.
# However, no deprecated or unimplemented methods will be added.
pack: Inner Core Test
pack-version: 2.3.0b125
pack-version-code: 171


# Sets the list of mods and configs to load, you can specify
# name, folder name of a modpack from /modpacks or a path.
# modpack: StoneBlock

# Use vanilla crafting table interface, instead of custom one.
# Attention: currently not recommended and recipes dislayed with bugs.
# use-legacy-workbench-override: off

# Disabling inventory fix for connected clients, outdated versions do not
# work properly with it, not allowing moving items around inventory.
# use-legacy-inventory: off

# Switches the debugging mode for unsupported methods (e.g., client-side).
# Might be one of: none, debug (default), warning, raise.
# unsupported-method-handling: raise

# Can mods use unsafe features (e.g., calling eval) or not.
# Attention: can create a security risk, however, for example,
# using requireGlobal can often disrupt operation of mods.  
# allow-unsafe-scripting: off

# Developer mode allows you to receive additional information
# about engine operation, as well as profiling of methods.
# developer-mode: on

# Time in seconds between auto-saving data of mods (any value above 20),
# as well as the world if it is included in another option.
# auto-save-period: 180

# Whether to save worlds in addition to mod data.
# auto-save-world: off

# Whether socket server enabled or not.
# Server socket requires an additional port, connection
# through server sockets is more unstable.
# socket-server-enabled: off

# Server socket port, usually should be between 10000 and 24999.
# But keep in mind that the client uses port 2304 by default.
# socket-port: 19131

# Sends form to players when ticking exception happens,
# contains only script stacktrace (without sensitive properties).
# display-ticking-errors-to-players: false

# If ticking callback interrupts too often, server will be stopped;
# otherwise it will continue trying restart itself continiously.
# stop-on-critical-ticking-error: true

# Maximum number of packets sent to the client
# max-packet-sending: 150

# Additional updatable threads
threading_additional_thread_count: 8

# Override player UUID
override-player-uuid: on

# Additional information in the log
# debug-logger: on
# info-logger: on
```

## Console commands

+ custom_items — displays a list of custom items
+ custom_blocks — displays a list of custom blocks
+ mods — displays a list of modifications on the server
+ inner_core_network — displays a list of players connected to the protocol on the server
+ state — outputs block statuses by the runtime ID
+ profilecallback [enabled] [showParameters] — debugging and profiling of callbacks
+ dimensions_list - displays a list of dimensions
+ tps - displays the load on the tick inner core

> All commands are for operators and cannot be called by players.

## Technical information

| Ключ                    | Значение |
|-------------------------|---|
| Protocol version        | 422 (1.16.200) |
| Commit InnerCore-1.16   | `96197c3b3426c6fa022332b7825cd018370d2a7c` |
| Commit Instant Referrer | `a264591` |
| Commit Nukkit-MOT       | `e85deb189b829c9e23908a6443dd06d67f340a90` |
