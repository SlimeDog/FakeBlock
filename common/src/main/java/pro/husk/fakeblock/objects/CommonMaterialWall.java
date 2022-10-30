package pro.husk.fakeblock.objects;

import com.comphenix.protocol.events.PacketContainer;
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

import java.util.HashMap;
import java.util.List;

public abstract class CommonMaterialWall extends WallObject {

    @Getter
    @Setter
    protected HashMap<Location, FakeBlockData> fakeBlockDataHashMap;

    /**
     * Constructor for walls loaded from config
     *
     * @param name of wall
     */
    public CommonMaterialWall(String name) {
        super(name);
    }

    /**
     * Constructor when creating a new wall
     *
     * @param name      of wall
     * @param location1 bound 1
     * @param location2 bound 2
     */
    public CommonMaterialWall(String name, Location location1, Location location2) {
        this(name);

        setLocation1(location1);
        setLocation2(location2);

        this.loadingData = true;
        FakeBlock.newChain()
                .async(() -> {
                    this.blocksInBetween = loadBlocksInBetween();
                    this.fakeBlockDataHashMap = loadFakeBlockDataHashMap();
                })
                .sync(this::removeOriginalBlocks)
                .async(() -> {
                    this.fakeBlockPacketList = buildPacketList(true);
                    this.realBlockPacketList = buildPacketList(false);
                    this.loadingData = false;
                    saveWall();
                }).execute();
    }

    /**
     * Method to load wall from config
     */
    @Override
    public void loadWall() {
        this.loadingData = true;
        FileConfiguration config = FakeBlock.getPlugin().getConfig();

        Location location1 = config.getLocation(getName() + ".location1");
        Location location2 = config.getLocation(getName() + ".location2");

        if (location1 != null && location2 != null) {
            if (location1.getWorld() == location2.getWorld()) {
                setLocation1(location1);
                setLocation2(location2);

                ConfigurationSection configurationSection = config.getConfigurationSection(getName() + ".material-data");

                if (configurationSection == null) return;

                fakeBlockDataHashMap = new HashMap<>();

                configurationSection.getKeys(false).forEach(key -> {
                    String[] split = key.split(",");

                    World world = Bukkit.getWorld(split[0]);
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    int z = Integer.parseInt(split[3]);

                    Location built = new Location(world, x, y, z);
                    String blockDataString = configurationSection.getString(key);

                    if (blockDataString != null) {
                        fakeBlockDataHashMap.put(built, new FakeBlockData(Bukkit.createBlockData(blockDataString)));
                    }
                });

                // Load all data to cache
                this.blocksInBetween = loadBlocksInBetween();
                this.fakeBlockPacketList = buildPacketList(true);
                this.realBlockPacketList = buildPacketList(false);
                this.loadingData = false;
                FakeBlock.getConsole().info("Loaded wall '" + getName() + "' successfully");
            } else {
                FakeBlock.getConsole().warning("Wall '" + getName() + "' is configured wrong, the world cannot be different");
            }
        }
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

        fakeBlockDataHashMap.forEach((location, blockData) -> {
            String locationAsKey = location.getWorld().getName()
                    + "," + location.getBlockX() + ","
                    + location.getBlockY() + "," + location.getBlockZ();

            if (blockData != null && blockData.getBlockData() != null) {
                String blockDataString = blockData.getBlockData().getAsString();
                config.set(getName() + ".material-data." + locationAsKey, blockDataString);
            }
        });

        plugin.saveConfig();
    }

    /**
     * Method to build the packets required for sending the blocks
     *
     * @param fake whether or not you want the real or fake blocks
     * @return list of PacketContainer ready to send to player
     */
    protected abstract List<PacketContainer> buildPacketList(boolean fake);

    /**
     * Method to prepare material map and remove the world blocks, replacing with "fake"
     */
    protected HashMap<Location, FakeBlockData> loadFakeBlockDataHashMap() {
        HashMap<Location, FakeBlockData> fakeBlockDataHashMap = new HashMap<>();
        getBlocksInBetween().forEach(location -> {
            Block block = location.getBlock();
            if (block.getType() != Material.AIR) {
                fakeBlockDataHashMap.put(location, new FakeBlockData(block.getBlockData()));
            }
        });
        return fakeBlockDataHashMap;
    }

    @Override
    protected void restoreOriginalBlocks() {
        getBlocksInBetween().forEach(location -> {
            Block block = location.getBlock();
            FakeBlockData fakeBlockData = fakeBlockDataHashMap.get(location);

            if (fakeBlockData == null || fakeBlockData.getBlockData() == null) {
                block.setType(Material.AIR);
            } else {
                block.setType(fakeBlockData.getBlockData().getMaterial());
                block.setBlockData(fakeBlockData.getBlockData());
            }
        });
    }
}