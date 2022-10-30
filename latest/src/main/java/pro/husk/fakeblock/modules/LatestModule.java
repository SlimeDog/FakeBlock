package pro.husk.fakeblock.modules;

import org.bukkit.Bukkit;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.FakeBlockModuleHandler;
import pro.husk.fakeblock.objects.WallConfig;
import pro.husk.fakeblock.objects.LatestMaterialWall;
import pro.husk.fakeblock.objects.WallObject;

public class LatestModule implements FakeBlockModuleHandler {

    /**
     * Method to load walls from config
     */
    @Override
    public void loadWalls() {
        FakeBlock.getPlugin().getConfig().getKeys(false).forEach(key -> {
            if (!key.equalsIgnoreCase("inverse-permission-check")) new LatestMaterialWall(key).loadWall();
        });
    }

    /**
     * Method to load wall from config object
     *
     * @param wallConfig object to get values from
     * @return MaterialWall object
     */
    @Override
    public WallObject loadWall(WallConfig wallConfig) {
        return new LatestMaterialWall(wallConfig.getWallName(), wallConfig.getLocation1(), wallConfig.getLocation2());
    }
}
