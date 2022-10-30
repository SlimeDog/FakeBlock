package pro.husk.fakeblock.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import pro.husk.fakeblock.FakeBlock;

import java.util.HashMap;
import java.util.UUID;

/**
 * Configuration object of a wall's state, storing the locations selected by a user
 * Preparing the data to be converted to a WallObject
 */
public class WallConfig {

    @Getter
    private final String wallName;

    @Getter
    @Setter
    private Location location1;

    @Getter
    @Setter
    private Location location2;

    /**
     * Constructor for a Config object
     *
     * @param wallName  of the wall they want to create
     * @param location1 of the wall
     * @param location2 of the wall
     */
    public WallConfig(String wallName, Location location1, Location location2) {
        this.wallName = wallName;
        this.location1 = location1;
        this.location2 = location2;
    }

    /**
     * Method to convert the current Config object into a WallObject
     *
     * @return WallObject implementation for the current version
     */
    public WallObject createWallObject() {
        return FakeBlock.getFakeBlockModuleHandler().loadWall(this);
    }
}
