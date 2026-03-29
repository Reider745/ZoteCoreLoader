# Zote Core — Inner Core Loader

[:ru: Русский](README-RU.md)

__Zote Core__ — server core based on Inner Core for Minecraft: Bedrock Edition 1.16.201 to facilitate unique gameplay with mods, often associated with creating customized modded servers.

The server core is based on [Nukkit-MOT](https://github.com/ZoteTeam/Nukkit-MOT), it can be easily updated by replacing the corresponding library.

![Zote Core](/.github/logo.png)

## Installation

Any platform supporting Java 17 is supported. Download the latest release of the server core, or build a repository using Gradle.

Run the assembled kernel by opening the console in the folder with it:

```sh
java -jar ZoteCore-SNAPSHOT.jar -XX:MaxRAMPercentage=95.0
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
pack: Inner Core
pack-version: 2.4.0b122 or higher
pack-version-code: ">163"

# Sets the list of mods and configs to load, you can specify
# name, folder name of a modpack from /modpacks or a path.
modpack: innercore

# Use vanilla crafting table interface, instead of custom one.
# Attention: currently not recommended and recipes dislayed with bugs.
use-legacy-workbench-override: off

# Disabling inventory fix for connected clients, outdated versions do not
# work properly with it, not allowing moving items around inventory.
use-legacy-inventory: on

# Switches the debugging mode for unsupported methods (e.g., client-side).
# Might be one of: none, debug (default), warning, raise.
unsupported-method-handling: debug

# Can mods use unsafe features (e.g., calling eval) or not.
# Attention: can create a security risk, however, for example,
# using requireGlobal can often disrupt operation of mods.  
allow-unsafe-scripting: on

# Developer mode allows you to receive additional information
# about engine operation, as well as profiling of methods.
developer-mode: off

# Time in seconds between auto-saving data of mods (any value above 20),
# as well as the world if it is included in another option.
auto-save-period: 60

# Whether to save worlds in addition to mod data.
auto-save-world: on

# Whether socket server enabled or not.
# Server socket requires an additional port, connection
# through server sockets is more unstable.
socket-server-enabled: off

# Server socket port, usually should be between 10000 and 24999.
# But keep in mind that the client uses port 2304 by default.
socket-port: 2304

# Sends form to players when ticking exception happens,
# contains only script stacktrace (without sensitive properties).
display-ticking-errors-to-players: true

# If ticking callback interrupts too often, server will be stopped;
# otherwise it will continue trying restart itself continiously.
stop-on-critical-ticking-error: true

# Maximum number of packets sent to the client
max-packet-sending: 200

# Additional updatable threads
threading_additional_thread_count: 0

# Override player UUID
override-player-uuid: on

# Additional information in the log
# debug-logger: on
# info-logger: on

# Enable detailed network debugging
# network-debug: off

# Allow unsupported/blocked Nukkit settings to be overridden
# allow-unsupported-options: off
```

## Console commands

+ custom_items — displays a list of custom items
+ custom_blocks — displays a list of custom blocks
+ mods — displays a list of modifications on the server
+ inner_core_network — displays a list of players connected to the protocol on the server
+ state [runtimeId] — outputs block statuses by the runtime ID
+ profilecallback [enabled] [showParameters] — debugging and profiling of callbacks
+ dimensions_list — displays a list of dimensions
+ tps — displays the load on the tick inner core
+ gen_chunks [dimension] [x1] [z1] [x2] [z2] — loads or generates chunks in specified area
+ get_all_players_id — gets name and runtime ID of all online players

> All commands are for operators and cannot be called by players.

## Technical information

| Key                     | Value              |
|-------------------------|--------------------|
| Protocol version        | 422 (1.16.200)     |
| Rhino version           | 1.7.14 (`5a917b5`) |
| Instant Referrer        | `a264591`          |
