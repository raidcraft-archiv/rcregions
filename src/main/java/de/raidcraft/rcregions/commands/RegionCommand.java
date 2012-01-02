package de.raidcraft.rcregions.commands;

import com.silthus.raidcraft.util.RCCommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 17.12.11 - 11:30
 *
 * @author Silthus
 */
public class RegionCommand implements CommandExecutor {

    private final RCCommandManager cmd = new RCCommandManager();
    private CommandSender sender;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        String label;
        this.sender = sender;
        if (args.length > 0) {
            label = args[0];

            if (cmd.is(label, "buy", ""))
        }
    }
}
