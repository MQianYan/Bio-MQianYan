package com.MQianYan.qbio.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "message.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // 加载默认配置
        InputStream defaultStream = plugin.getResource("message.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultConfig);
        }
        
        plugin.getLogger().info("消息文件加载完成");
    }

    public String getMessage(String path) {
        return getMessage(path, null);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null) {
            return ChatColor.RED + "消息未找到: " + path;
        }
        
        // 替换占位符
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    // 便捷方法
    public String getPluginEnableMessage() {
        return getMessage("plugin.enable");
    }

    public String getCaptureSuccessMessage(Entity entity) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("entity", getEntityDisplayName(entity));
        return getMessage("capture.success", placeholders);
    }

    public String getCaptureFailedMessage() {
        return getMessage("capture.failed");
    }

    public String getBannedEntityMessage() {
        return getMessage("capture.banned-entity");
    }

    public String getCooldownMessage(int seconds) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("time", String.valueOf(seconds));
        return getMessage("capture.cooldown", placeholders);
    }

    public String getReleaseSuccessMessage(Entity entity) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("entity", getEntityDisplayName(entity));
        return getMessage("release.success", placeholders);
    }

    public String getEmptyBucketMessage() {
        return getMessage("capture.empty-bucket");
    }

    public String getPlayerOfflineMessage(String playerName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);
        return getMessage("error.player-offline", placeholders);
    }

    public String getNoPermissionMessage() {
        return getMessage("error.no-permission");
    }

    // 获取实体显示名称
    private String getEntityDisplayName(Entity entity) {
        if (entity.getCustomName() != null) {
            return ChatColor.stripColor(entity.getCustomName());
        }
        
        // 实体类型到中文名称的映射
        Map<EntityType, String> entityNames = new HashMap<>();
        entityNames.put(EntityType.COW, "牛");
        entityNames.put(EntityType.PIG, "猪");
        entityNames.put(EntityType.SHEEP, "羊");
        entityNames.put(EntityType.CHICKEN, "鸡");
        entityNames.put(EntityType.CREEPER, "苦力怕");
        entityNames.put(EntityType.ZOMBIE, "僵尸");
        entityNames.put(EntityType.SKELETON, "骷髅");
        entityNames.put(EntityType.ENDERMAN, "末影人");
        entityNames.put(EntityType.WITCH, "女巫");
        entityNames.put(EntityType.BLAZE, "烈焰人");
        entityNames.put(EntityType.GHAST, "恶魂");
        entityNames.put(EntityType.SLIME, "史莱姆");
        entityNames.put(EntityType.SPIDER, "蜘蛛");
        entityNames.put(EntityType.CAVE_SPIDER, "洞穴蜘蛛");
        
        return entityNames.getOrDefault(entity.getType(), 
               entity.getType().getKey().getKey());
    }

    public void reloadMessages() {
        loadMessages();
    }
}