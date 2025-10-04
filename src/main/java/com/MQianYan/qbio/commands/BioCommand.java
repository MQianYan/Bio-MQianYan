package com.MQianYan.qbio.commands;

import com.MQianYan.qbio.QBioPlugin;
import com.MQianYan.qbio.managers.BucketManager;
import com.MQianYan.qbio.managers.ConfigManager;
import com.MQianYan.qbio.managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BioCommand implements CommandExecutor, TabExecutor {
    
    private final QBioPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final BucketManager bucketManager;

    public BioCommand(QBioPlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.bucketManager = plugin.getBucketManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                return handleGiveCommand(sender, args);
            case "rate":
                return handleRateCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "qbio.give")) {
            sender.sendMessage(messageManager.getMessage("error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /bio give <玩家名>");
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(messageManager.getMessage("error.player-offline")
                .replace("{player}", targetName));
            return true;
        }

        ItemStack bioBucket = bucketManager.createEmptyBioBucket();
        if (target.getInventory().addItem(bioBucket).isEmpty()) {
            if (sender.equals(target)) {
                sender.sendMessage("§a你获得了一个生物桶!");
            } else {
                sender.sendMessage("§a已给予玩家 " + target.getName() + " 一个生物桶!");
                target.sendMessage("§a你获得了一个生物桶!");
            }
        } else {
            sender.sendMessage("§c玩家 " + target.getName() + " 背包已满!");
        }

        return true;
    }

    private boolean handleRateCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "qbio.rate")) {
            sender.sendMessage(messageManager.getMessage("error.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /bio rate <0-100>");
            return true;
        }

        try {
            double rate = Double.parseDouble(args[1]);
            
            if (rate < 0 || rate > 100) {
                sender.sendMessage("§c概率必须在 0 到 100 之间!");
                return true;
            }

            double decimalRate = rate / 100.0;
            configManager.getConfig().set("settings.capture-chance", decimalRate);
            configManager.saveConfig();
            configManager.reloadConfig();

            sender.sendMessage("§a抓取概率已设置为: " + rate + "%");
            plugin.getLogger().info(sender.getName() + " 将抓取概率修改为: " + rate + "%");
            return true;

        } catch (NumberFormatException e) {
            sender.sendMessage("§c请输入有效的数字!");
            return true;
        }
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!hasPermission(sender, "qbio.reload")) {
            sender.sendMessage(messageManager.getMessage("error.no-permission"));
            return true;
        }

        configManager.reloadConfig();
        messageManager.reloadMessages();
        
        sender.sendMessage(messageManager.getMessage("plugin.reload"));
        plugin.getLogger().info(sender.getName() + " 重载了 QBio 插件配置");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== QBio 插件指令 ===");
        if (hasPermission(sender, "qbio.give")) {
            sender.sendMessage("§e/bio give <玩家> §7- 给予玩家生物桶");
        }
        if (hasPermission(sender, "qbio.rate")) {
            sender.sendMessage("§e/bio rate <0-100> §7- 修改抓取概率");
        }
        if (hasPermission(sender, "qbio.reload")) {
            sender.sendMessage("§e/bio reload §7- 重载配置");
        }
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return !(sender instanceof Player) || sender.isOp() || sender.hasPermission(permission);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("give", "rate", "reload");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase()) && hasPermission(sender, "qbio." + subCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && "give".equals(args[0].toLowerCase())) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}