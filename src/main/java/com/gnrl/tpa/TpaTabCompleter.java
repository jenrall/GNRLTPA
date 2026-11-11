package com.gnrl.tpa;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TpaTabCompleter implements TabCompleter {

    private static final List<String> AUTO_MODES = Arrays.asList("toggle", "deny", "accept");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("tpa") || command.getName().equalsIgnoreCase("tpahere")) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    names.add(p.getName());
                }
                StringUtil.copyPartialMatches(args[0], names, completions);
            }
            return completions;
        }

        if (command.getName().equalsIgnoreCase("tpauto")) {
            if (args.length == 1) {
                StringUtil.copyPartialMatches(args[0], AUTO_MODES, completions);
            }
            return completions;
        }

        return completions;
    }
}
