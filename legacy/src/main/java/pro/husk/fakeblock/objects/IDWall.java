package pro.husk.fakeblock.objects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pro.husk.fakeblock.FakeBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * WallObject implementation for legacy versions of Minecraft that utilise int ID and byte data values
 */
public class IDWall extends WallObject {

    @Getter
    protected List<PacketContainer> fakeBlockPacketList;

    @Getter
    @Setter
    private HashMap<Location, FakeBlockData> fakeBlockDataHashMap;

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public IDWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public IDWall(String name, Location location1, Location location2) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);

        FakeBlock.newChain().async(() -> {
            this.blocksInBetween = loadBlocksInBetween();
            this.fakeBlockDataHashMap = buildDataMapFromWorld();
        }).sync(this::removeOriginalBlocks).async(() -> {
            this.fakeBlockPacketList = buildPacketList(true);
            this.realBlockPacketList = buildPacketList(false);
            saveWall();
        }).execute();
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        FakeBlock.newChain().async(() -> {
            FileConfiguration config = FakeBlock.getPlugin().getConfig();

            Location location1 = (Location) config.get(getName() + ".location1");
            Location location2 = (Location) config.get(getName() + ".location2");

            if (location1 != null && location2 != null) {
                if (location1.getWorld() == location2.getWorld()) {
                    setLocation1(location1);
                    setLocation2(location2);

                    this.fakeBlockDataHashMap = new HashMap<>();

                    ConfigurationSection materialSection = config.getConfigurationSection(getName() + ".material-data");

                    if (materialSection == null) return;

                    materialSection.getKeys(false).forEach(key -> {
                        String[] split = key.split(",");

                        World world = Bukkit.getWorld(split[0]);
                        int x = Integer.parseInt(split[1]);
                        int y = Integer.parseInt(split[2]);
                        int z = Integer.parseInt(split[3]);

                        Location built = new Location(world, x, y, z);
                        String materialDataString = materialSection.getString(key);

                        String[] dataSplit = materialDataString.split(":");

                        String materialString = dataSplit[0];
                        byte blockData = Byte.parseByte(dataSplit[1]);

                        if (materialString != null) {
                            Material material = Material.getMaterial(materialString);
                            fakeBlockDataHashMap.put(built, new FakeBlockData(material, blockData));
                        }
                    });

                    // Load all data to cache
                    this.blocksInBetween = loadBlocksInBetween();
                    this.fakeBlockPacketList = buildPacketList(true);
                    this.realBlockPacketList = buildPacketList(false);
                    FakeBlock.getConsole().info("Loaded wall '" + getName() + "' successfully");
                } else {
                    FakeBlock.getConsole().warning("Wall '" + getName() + "' is configured wrong, the world cannot be different");
                }
            }
        }).execute();
    }

    /**
     * Method to save wall to config
     */
    @Override
    public void saveWall() {
        FakeBlock plugin = FakeBlock.getPlugin();
        FileConfiguration config = plugin.getConfig();
        config.set(getName() + ".location1", getLocation1());
        config.set(getName() + ".location2", getLocation2());

        fakeBlockDataHashMap.keySet().forEach(location -> {
            String locationAsKey = location.getWorld().getName()
                    + "," + location.getBlockX() + ","
                    + location.getBlockY() + "," + location.getBlockZ();

            FakeBlockData fakeBlockData = fakeBlockDataHashMap.get(location);
            Material material;
            byte dataByte;
            if (fakeBlockData == null) {
                material = Material.AIR;
                dataByte = 0;
            } else {
                material = fakeBlockData.getMaterial();
                dataByte = fakeBlockData.getData();
            }

            if (material == Material.AIR) return;

            String materialString = material.toString();
            String saveString = materialString + ":" + dataByte;

            config.set(getName() + ".material-data." + locationAsKey, saveString);
        });

        plugin.saveConfig();
    }

    private HashMap<Location, FakeBlockData> buildDataMapFromWorld() {
        HashMap<Location, FakeBlockData> dataMap = new HashMap<>();
        getBlocksInBetween().forEach(location -> dataMap.put(location,
                new FakeBlockData(location.getBlock().getType(), location.getBlock().getData())));
        return dataMap;
    }

    /**
     * Method to build the packets required for sending the blocks
     *
     * @param fake whether or not you want the real or fake blocks
     * @return list of PacketContainer ready to send to player
     */
    protected List<PacketContainer> buildPacketList(boolean fake) {
        List<PacketContainer> fakeBlockPackets = new ArrayList<>();

        loadSortedChunkMap().forEach((chunkMapKey, locationList) -> {
            PacketContainer fakeChunk = new PacketContainer(PacketType.Play.Server.MULTI_BLOCK_CHANGE);
            ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkMapKey.getX(),
                    chunkMapKey.getZ());
            MultiBlockChangeInfo[] blockChangeInfo = new MultiBlockChangeInfo[locationList.size()];

            int i = 0;
            for (Location location : locationList) {
                Material material = Material.AIR;
                byte data = 0;
                if (fake) {
                    FakeBlockData fakeBlockData = fakeBlockDataHashMap.get(location);
                    if (fakeBlockData != null) {
                        material = fakeBlockData.getMaterial();
                        data = fakeBlockData.getData();
                    }
                } else {
                    Block block = location.getBlock();
                    material = block.getType();
                    data = block.getData();
                }
                if (material != null) {
                    blockChangeInfo[i] = new MultiBlockChangeInfo(location, WrappedBlockData.createData(material, data));
                    i++;
                }
            }

            fakeChunk.getChunkCoordIntPairs().write(0, chunkCoordIntPair);
            fakeChunk.getMultiBlockChangeInfoArrays().write(0, blockChangeInfo);

            fakeBlockPackets.add(fakeChunk);
        });

        return fakeBlockPackets;
    }

    @Override
    protected void restoreOriginalBlocks() {
        getBlocksInBetween().forEach(location -> {
            FakeBlockData fakeBlockData = fakeBlockDataHashMap.get(location);
            Block block = location.getBlock();

            if (fakeBlockData != null) {
                block.setType(fakeBlockData.getMaterial());
                block.setData(fakeBlockData.getData());
            } else {
                block.setType(Material.AIR);
            }
        });
    }
}
