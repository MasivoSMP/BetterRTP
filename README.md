<p align="center">
  <b><a>Welcome to BetterRTP's repository!</a></b>
</p>

## MasivoSMP fork: 3.6.13-masivo.1

Fixes RTP chunk access on Folia/Pinac with Minecraft 26.x. PaperLib's old version
parser selects synchronous chunk loading for 26.x; this fork detects the async
chunk API directly and schedules safety checks on the destination region.
Queue generation and the chunk diagnostic use the same helper. Player actions,
teleport callbacks, and particles run on the player's scheduler through FoliaLib.
Failed chunk loads and rejected teleports report failure instead of silently
stalling or emitting a successful teleport event.

Build with JDK 17 or newer: `mvn clean verify`. This also runs the assertion-based
chunk scheduling regression check (async completion, legacy fallback, negative
coordinates, and load/inspection failures). Output:
`target/BetterRTP-3.6.13-masivo.1.jar`.

Replace the existing BetterRTP JAR while the server is stopped, then restart.
The plugin name, configuration, commands, and MasivoRTP event integration remain
compatible. A live RTP on Pinac 26.2 is still required to confirm the complete
deployment; the local check simulates scheduler ownership without a server.

## Where's the Lang files?/Want to Contribute translating?  
All language files are located [here](src/main/resources/lang)
feel free to fork one of the language files and help translate!

## Libraries
BetterRTP uses and is compiled with the following libraries:

- [ParticleLib](https://github.com/ByteZ1337/ParticleLib) (included) - Particles library by ByteZ1337. Find all supported particles [here](https://github.com/ByteZ1337/ParticleLib/blob/master/src/main/java/xyz/xenondevs/particle/ParticleEffect.java)
- [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib) (included) - Library for interfacing with Folia specific APIs, used for cross-platform timers.

## Build instructions on Ubuntu

mvn clean install

The file will be in the Target file.

## Where's the Wiki?  
The wiki is available [here](../../wiki)!
    
<p align="center">
  <b>Chat with us on Discord</b><br/>
  <a href="https://discord.gg/8Kt4wKm"><img src="https://img.shields.io/discord/182633513474850818.svg?longCache=true&style=flat-square&label=Discord" alt="Discord" /></a><br/>
  <b>Have a Suggestion? Make an issue!</b><br/>
  <a href="../../issues"><img src="https://img.shields.io/github/issues-raw/SuperRonanCraft/BetterRTP.svg?longCache=true&style=flat-square&label=Issues" alt="GitHub issues" /></a><br/>
  <br/>
  <a href="https://www.spigotmc.org/resources/36081/">Thank you for viewing the Wiki for BetterRTP!</a><br/>
  <i><a>Did this wiki help you out? Please give it a <b>Star</b> so I know it's getting use!</a></i><br/>
  <br/>
  <b><i><a href="https://www.spigotmc.org/resources/authors/superronancraft.13025/">Check out my other plugins!</a></i></b>
</p>
