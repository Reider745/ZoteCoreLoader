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

## Behavior packs 1.16

Extract a pack into `behavior_packs/<pack-name>/` beside the server, with `manifest.json` directly inside that folder. Packs from the selected modpack, mods and Inner Core runtime packs are also loaded. Source directories are read in place and preserved. Duplicate paths and UUIDs are loaded once; registered mod/modpack sources take precedence over the server folder. Restart the server after changing a pack.

Server support is partial:

- Manifest versions 1 and 2, nested directories, optional `contents.json`, UTF-8/BOM and JSON comments. A malformed file is logged without stopping other content.
- Item formats `1.10` through `1.16.201`, including `1.16.100`: custom namespaces, display names, stack limits, maximum durability, creative categories and basic food nutrition/saturation. Vanilla item overrides are unsupported. Visual components are handled by the client.
- Shaped/shapeless crafting, shapeless stonecutter recipes and furnace recipes for furnaces, blast furnaces and both campfires. Item strings and objects, metadata, counts, wildcard input data (`-1`/`32767`) and additional shaped results are supported. Every pack registers its items before recipes are loaded.
- Entity formats `1.8` through `1.16.201`: per-actor components and Molang state, component groups, add/remove events, sequences, weighted randomization, nested filters, timers and environment sensors. Health, groups, variables, equipment and controller state persist in NBT. Both identifier-based creation and numeric spawn eggs are supported, with stable network definitions.
- Priority-based AI covers target selection, retaliation, melee/ranged attacks, wandering, swimming/flying, avoidance, panic, following, temptation, breeding and looking. Bounded A* routes around obstacles without generating chunks. These are server goal implementations, not exact copies of Bedrock algorithms.
- Lifecycle support includes fractional collision boxes/scaling, breathing, burning, growing, taming, healing, interactions, passengers, equipment, projectiles, explosions, transformations, entity spawning, despawning, boss bars, loot tables and XP. Natural spawning checks terrain/water, light, height, difficulty, biome, density, weight and herd size every 100 ticks in loaded chunks.
- Molang uses the MIT-licensed [Mocha 3.0.1](https://github.com/unnamed/mocha) parser/interpreter, with actor queries, persistent variables, temporary variables and math functions. Parsed expressions are cached; nesting is limited to 64 levels and loops to 4096 iterations per evaluation. The license is included as `META-INF/LICENSE-mocha.txt`.
- Server animation controllers/animations support states, Molang transitions, entry/exit actions, remapped variables and timelines containing events/commands. Bone transforms and rendering remain client-side.

Full parity with the Bedrock 1.16 engine is not guaranteed. Specialized goals/mechanics (villager trading/economy, raids, complete riding inventories/genetics, boss-specific attacks and other missing components/queries), `.mcfunction` files, custom blocks, food effects and smoker-only recipes remain unsupported. Individual AI parameters and natural spawn distributions may differ from Bedrock. Biome tags are mapped to Nukkit biome families; custom biomes are not added. Custom entity registration enables the Nukkit experiment-mode runtime flag. Unsupported content is diagnosed in the log. Install matching textures, models, animations and client item/entity definitions on clients; this loader supplies server logic and does not replace client resources.

See the [Bedrock furnace recipe reference](https://learn.microsoft.com/en-us/minecraft/creator/reference/content/recipereference/examples/recipedefinitions/minecraftrecipe_furnace?view=minecraft-bedrock-stable) for smelting syntax.

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
