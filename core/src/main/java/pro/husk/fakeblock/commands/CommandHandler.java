package pro.husk.fakeblock.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.husk.fakeblock.FakeBlock;
import pro.husk.fakeblock.objects.WallConfig;
import pro.husk.fakeblock.objects.Language;
import pro.husk.fakeblock.objects.WallObject;

import java.util.HashSet;
import java.util.UUID;

/**
 * Primary command handler for the FakeBlock command
 */
@CommandAlias("fakeblock|fb")
@Description("FakeBlock-related commands")
public class CommandHandler extends BaseCommand {

    @Dependency
    private Language language;

    private static final HashSet<UUID> toggledPlayers = new HashSet<>();

    @Default
    @CommandPermission("fakeblock.admin")
    public void help(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.GREEN + " --------- " + ChatColor.AQUA + language.getPrefix() + ChatColor.GREEN + " Help --------- ");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock | Aliases: /fakeblock, /fb");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock create <wall name> <material name> | Creates a wall under specified name with given material");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock delete <wall name> | Deletes wall");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock reload | Reloads the walls from config");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock list | Lists all walls");
        commandSender.sendMessage(ChatColor.GREEN + "/fakeblock toggle <player> | Shows all nearby walls to a player");
        commandSender.sendMessage(ChatColor.GREEN + "------------------------------------");
    }

    @Subcommand("delete")
    @CommandPermission("fakeblock.admin")
    @CommandCompletion("@walls")
    public void delete(CommandSender commandSender, String wallName) {
        WallObject wallObject = WallObject.getByName(wallName);
        if (wallObject != null) {
            wallObject.delete();
            commandSender.sendMessage(language.getPrefix() + " " + language.getWallDeleted());
        }
    }

    @Subcommand("reload")
    @CommandPermission("fakeblock.admin")
    public void reload(CommandSender commandSender) {
        FakeBlock.getPlugin().reloadConfigs();
        WallObject.getWallObjectList().forEach(WallObject::loadWall);
        commandSender.sendMessage(language.getPrefix() + " " + language.getWallsReloaded());
    }

    @Subcommand("create")
    @CommandPermission("fakeblock.admin")
    public void create(Player player, String wallName) {
        LocalSession session = FakeBlock.getWorldEdit().getSession(player);
        com.sk89q.worldedit.world.World sessionWorld = session.getSelectionWorld();

        try {
            Region selection = session.getSelection(sessionWorld);
            BlockVector3 bv1 = selection.getMinimumPoint();
            BlockVector3 bv2 = selection.getMaximumPoint();

            Location location1 = new Location(Bukkit.getWorld(selection.getWorld().getName()), bv1.getBlockX(), bv1.getBlockY(), bv1.getBlockZ());
            Location location2 = new Location(Bukkit.getWorld(selection.getWorld().getName()), bv2.getBlockX(), bv2.getBlockY(), bv2.getBlockZ());

            WallConfig wallConfig = new WallConfig(wallName, location1, location2);

            wallConfig.createWallObject();

            player.sendMessage(language.getPrefix() + " " + language.getWallCreated());
        } catch (IncompleteRegionException e) {
            player.sendMessage(language.getPrefix() + " " + language.getIncompleteSelection());
        }
    }

    @Subcommand("list")
    @CommandPermission("fakeblock.admin")
    public void list(CommandSender commandSender) {
        commandSender.sendMessage(language.getPrefix() + ChatColor.GOLD + " Walls");
        WallObject.getWallObjectList().forEach(wallObject ->
                commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.GREEN + wallObject.getName()));
    }

    @Subcommand("toggle")
    @CommandPermission("fakeblock.admin")
    @CommandCompletion("@players")
    public void toggle(CommandSender commandSender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            boolean isToggled = toggledPlayers.contains(target.getUniqueId());
            FakeBlock.getWallUtility().processWall(target, 20, isToggled);

            if (isToggled) {
                toggledPlayers.remove(target.getUniqueId());
            } else {
                toggledPlayers.add(target.getUniqueId());
            }

            commandSender.sendMessage(language.getPrefix() + " " + language.getWallsToggled());
        } else {
            commandSender.sendMessage(language.getPrefix() + " " + language.getCantFindPlayer());
        }
    }
}
