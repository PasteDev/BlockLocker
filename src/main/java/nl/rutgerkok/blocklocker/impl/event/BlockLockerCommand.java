package nl.rutgerkok.blocklocker.impl.event;

import java.util.*;

import nl.rutgerkok.blocklocker.*;
import nl.rutgerkok.blocklocker.Translator.Translation;
import nl.rutgerkok.blocklocker.profile.PlayerProfile;
import nl.rutgerkok.blocklocker.protection.Protection;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import static nl.rutgerkok.blocklocker.BlockLockerAPIv2.getPlugin;

public final class BlockLockerCommand implements TabExecutor {

    private final BlockLockerPlugin plugin;

    public BlockLockerCommand(BlockLockerPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            return reloadCommand(sender);
        }

        if (args[0].equalsIgnoreCase("adduser")) {
            if (!sender.hasPermission("blocklocker.protect")) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_NO_PERMISSION);
                return true;
            }

            if (args.length < 3) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_ADDUSER_USAGE);
                return true;
            }

            if (!(sender instanceof Player player)) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_MUST_BE_PLAYER);
                return true;
            }

            String usernameToAdd = args[1];
            int line;
            try {
                line = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_INVALID_LINE_NUMBER);
                return true;
            }

            if (line < 2 || line > 4) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_LINE_OUT_OF_BOUNDS);
                return true;
            }

            Block targetBlock = getTargetBlock(player);
            if (targetBlock == null) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_NO_SELECTED_BLOCK);
                return true;
            }

            if (!addUserToProtection(player, targetBlock, usernameToAdd, line)) {
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_ADDUSER_FAILED);
            } else {
                String successMessage = plugin.getTranslator().get(Translation.COMMAND_ADDUSER_SUCCESS).replace("{username}", usernameToAdd);
                plugin.getTranslator().sendMessage(sender, Translation.COMMAND_ADDUSER_SUCCESS, successMessage);
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("adduser", "reload");
        }

        if (args[0].equalsIgnoreCase("adduser")) {
            if (args.length == 2) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }

            if (args.length == 3) {
                return Arrays.asList("2", "3", "4");
            }
        }

        return Collections.emptyList();
    }

    private boolean reloadCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.CAN_RELOAD)) {
            plugin.getTranslator().sendMessage(sender, Translation.COMMAND_NO_PERMISSION);
            return true;
        }

        plugin.reload();
        plugin.getLogger().info(plugin.getTranslator().getWithoutColor(Translation.COMMAND_PLUGIN_RELOADED));
        if (!(sender instanceof ConsoleCommandSender)) {
            plugin.getTranslator().sendMessage(sender, Translation.COMMAND_PLUGIN_RELOADED);
        }
        return true;
    }

    private Block getTargetBlock(Player player) {
        BlockIterator iterator = new BlockIterator(player, 10);
        Block targetBlock;
        while (iterator.hasNext()) {
            targetBlock = iterator.next();
            if (!targetBlock.getType().isAir()) {
                return targetBlock;
            }
        }
        return null;
    }

    public boolean addUserToProtection(Player executor, Block targetBlock, String usernameToAdd, int line) {
        Optional<Protection> protection = getPlugin().getProtectionFinder().findProtection(targetBlock);
        if (protection.isEmpty()) {
            return false;
        }

        ProfileFactory profileFactory = getPlugin().getProfileFactory();
        PlayerProfile executorProfile = profileFactory.fromNameAndUniqueId(executor.getName(), Optional.of(executor.getUniqueId()));

        if (executorProfile == null) {
            plugin.getTranslator().sendMessage(executor, Translation.ERROR_GETTING_PROFILE);
            return false;
        }

        if (!protection.get().isOwner(executorProfile) && !executor.hasPermission("blocklocker.admin")) {
            plugin.getTranslator().sendMessage(executor, Translation.PROTECTION_NO_PERMISSION_FOR_CLAIM);
            return false;
        }

        for (ProtectionSign sign : protection.get().getSigns()) {
            Block signBlock = sign.getLocation().getBlock();
            if (signBlock.getState() instanceof Sign bukkitSign) {
                if (!bukkitSign.getLine(line - 1).isEmpty()) {
                    return false;
                }
                bukkitSign.setLine(line - 1, usernameToAdd);
                bukkitSign.update();
                return true;
            }
        }
        return false;
    }
}