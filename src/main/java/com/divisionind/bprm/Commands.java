/*
 * BackpacksRemastered - remastered version of the popular Backpacks plugin
 * Copyright (C) 2019 Division Industries LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.divisionind.bprm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Commands {

    /*
        TODO
        for each subcommand use a colan and dont nest e.g.

        /bp config:set <key> <value>
        /bp config:remove <key>
        etc
     */

    protected static class Help extends ACommand {

        private static final int COMMANDS_PER_PAGE = 6;

        @Override
        public String alias() {
            return "help";
        }

        @Override
        public String desc() {
            return "displays this help information";
        }

        @Override
        public String usage() {
            return "<page>";
        }

        @Override
        public String permission() {
            return "backpacks.help";
        }

        @Override
        public void execute(CommandSender sender, String label, String[] args) {
            // determine what page of help to display
            int page;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    respond(sender, "&cError: This argument must be an integer.");
                    return;
                }
            } else page = 1;

            int numberOfPages = calculateNumberOfPages();

            if (page > numberOfPages) {
                respondf(sender, "&cError: This help page does not exist. The maximum help page is %s.", numberOfPages);
                return;
            }

            List<ACommand> commands = getOnPage(page);

            respondf(sender, "&bPage (&e%s &b/ &e%s&b)", page, numberOfPages);

            for (ACommand cmd : commands) {
                respondn(sender, String.format("&e/%s %s %s\n  &7%s", label, cmd.alias(), cmd.usage() == null ? "" : cmd.usage(), cmd.desc()));
            }
        }

        public static int calculateNumberOfPages() {
            return (Backpacks.getCommands().size() / COMMANDS_PER_PAGE) + 1;
        }

        public static List<ACommand> getOnPage(int page) {
            List<ACommand> commands = Backpacks.getCommands();
            List<ACommand> newCmds = new ArrayList<>();

            int startCmd = (page - 1) * COMMANDS_PER_PAGE;

            for (int i = startCmd;(i<commands.size() && (i - startCmd) < COMMANDS_PER_PAGE);i++) {
                newCmds.add(commands.get(i));
            }
            return newCmds;
        }
    }

    protected static class ConfigReload extends ACommand {
        @Override
        public String alias() {
            return "config:reload";
        }

        @Override
        public String desc() {
            return "reloads the yml configuration";
        }

        @Override
        public String usage() {
            return null;
        }

        @Override
        public String permission() {
            return "backpacks.config.reload";
        }

        @Override
        public void execute(CommandSender sender, String label, String[] args) {
            respond(sender, "&eReloading the configuration...");
            Backpacks.getInstance().reloadConfig();
            Backpacks.getInstance().setupFromConfig();
            respond(sender, "&eConfiguration has been reloaded.");
        }
    }

    protected static class Info extends ACommand {
        @Override
        public String alias() {
            return "info";
        }

        @Override
        public String desc() {
            return "displays info about the plugin";
        }

        @Override
        public String usage() {
            return null;
        }

        @Override
        public String permission() {
            return "backpacks.info";
        }

        @Override
        public void execute(CommandSender sender, String label, String[] args) {
            respond(sender, "&e&lInfo");
            respondn(sender, "&7Created by drew6017 as a remake of his original plugin with more features an optimizations.");
            respondnf(sender, " &eVersion: &a%s", Backpacks.VERSION);
            respondnf(sender, " &eGit Commit: &a%s", Backpacks.GIT_HASH);
            respondnf(sender, " &eGit Build: &a%s", Backpacks.GIT_NUM);
        }
    }

    protected static class ItemInfo extends ACommand {
        @Override
        public String alias() {
            return "item:info";
        }

        @Override
        public String desc() {
            return "displays info about the item you are holding";
        }

        @Override
        public String usage() {
            return null;
        }

        @Override
        public String permission() {
            return "backpacks.item.info";
        }

        @Override
        public void execute(CommandSender sender, String label, String[] args) {
            if (!(sender instanceof Player)) return;
            Player p = (Player)sender;
            ItemStack item = p.getInventory().getItemInMainHand();

            try {
                Object tagCompound = NMSReflector.getNBTTagCompound(NMSReflector.asNMSCopy(item));

                respond(sender, String.format("&ebackpack_key = %s", NMSReflector.hasNBTKey(tagCompound, "backpack_key")));

                if (!NMSReflector.hasNBTKey(tagCompound, "backpack_id")) { // also add backpack_type
                    respond(sender, "&eThis item doesnt have a backpack id.");
                    return;
                }

                if (!NMSReflector.hasNBTKey(tagCompound, "backpack_type")) {
                    respond(sender, "&eThis item doesnt have a backpack type.");
                    return;
                }

                respond(sender, Long.toString((long)NMSReflector.getNBT(tagCompound, "Long", "backpack_id")));
                respond(sender, Integer.toString((int)NMSReflector.getNBT(tagCompound, "Int", "backpack_type")));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }
}