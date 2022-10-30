# FakeBlock

This versatile plugin allows you to create fake blocks that exist for some players and do not exist for others!

These fake blocks are indistinguishable from real blocks and prevent players from walking through them, or interacting
with them.

## Installation

First, download and install [WorldEdit](https://dev.bukkit.org/projects/worldedit)
& [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

Once that's complete, download and install FakeBlock

- Latest for 1.16.2+
- Intermediate for 1.13 -> 1.16.1
- Legacy for 1.7 -> 1.12

## Creation

To create a fake block selection, use WorldEdit's selection tooling and then run ``/fakeblock create <name>``
obviously replacing <name> with your desired name. (Be careful, large selections will cause lag. Ideally set this up
without players online)

Players WITH the permission
``fakeblock.<name>`` will be sent these "fake" blocks. This is different to v1, where it is inversed.

## Deletion

To delete a wall (and restore it's previous state), use ``/fakeblock delete <name>`` (Be careful, large selections will
cause lag. Ideally set this up without players online)

## Suggestions

If you have any suggestions or criticism, please report it on the GitHub page.

Optionally depends on [LuckPerms](https://luckperms.net/) to update wall visibility on permission change.

## Note: Version 1 configurations are NOT compatible with version 2

## [Spigot Resource Page](https://www.spigotmc.org/resources/fakeblock.12830/)

## Compiling from source

This project uses Gradle!

```gradle build``` or ```gradle publishToMavenLocal``` if you want it in your local maven repo

Artifacts can be found in their respective ``target`` folders!

## Developers

If you are planning to hook into FakeBlock, you will need to add the repository, as well as the dependency.

For example:

```xml

<repository>
    <id>husk</id>
    <url>https://maven.husk.pro/snapshots/</url>
</repository>
```

```xml

<dependency>
    <groupId>pro.husk</groupId>
    <artifactId>FakeBlock-latest</artifactId>
    <version>2.1.0-SNAPSHOT</version>
</dependency>
```

Once that's complete, you can work with the WallObject relevant to your target version.

Example of creating both persistent and non persistent wall

```java
// Persistent example... the rest is done for us!
LatestMaterialWall latestMaterialWallPersistent = new LatestMaterialWall("some_persistent_wall", location1, location2);

// Non-persistent example
LatestMaterialWall latestMaterialWall = new LatestMaterialWall("some_non_persistent_wall");
latestMaterialWall.setLocation1(location1);
latestMaterialWall.setLocation2(location2);

// For example, build map of Location -> FakeBlockData.. This example uses the world data, however, you might want to load from a schematic or something.
HashMap<Location, FakeBlockData> fakeBlockDataHashMap = new HashMap<>();
latestMaterialWall.loadBlocksInBetween().forEach(location -> fakeBlockDataHashMap.put(location, new FakeBlockData(location.getBlock().getBlockData())));

// Finally create the non persistent wall
latestMaterialWall.createNonPersistentWall(fakeBlockDataHashMap);
```

If you have any further questions feel free to reach out, or create an issue!
