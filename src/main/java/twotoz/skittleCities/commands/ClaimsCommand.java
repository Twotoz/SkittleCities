package twotoz.skittleCities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import twotoz.skittleCities.SkittleCities;
import twotoz.skittleCities.gui.ClaimsListGUI;
import twotoz.skittleCities.utils.MessageUtil;

public class ClaimsCommand implements CommandExecutor {
    private final SkittleCities plugin;

    public ClaimsCommand(SkittleCities plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, plugin.getConfig(), "player-only");
            return true;
        }

        // WORLD CHECK - Must be in configured world (even for admins!)
        if (!MessageUtil.checkWorld(player, plugin.getConfig())) {
            return true;
        }

        ClaimsListGUI gui = new ClaimsListGUI(plugin, player);
        gui.open();

        return true;
    }
}
