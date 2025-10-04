package com.MQianYan.qbio.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    
    private double captureChance;
    private int cooldown;
    private boolean debug;
    private Set<EntityType> bannedEntities;
    private boolean showCaptureChance;
    private boolean enableParticles;
    private boolean enableSounds;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // 加载基本设置
        captureChance = config.getDouble("settings.capture-chance", 0.8);
        cooldown = config.getInt("settings.cooldown", 3);
        debug = config.getBoolean("settings.debug", false);
        
        // 加载物品设置
        showCaptureChance = config.getBoolean("item-settings.show-capture-chance", true);
        enableParticles = config.getBoolean("item-settings.enable-particles", true);
        enableSounds = config.getBoolean("item-settings.enable-sounds", true);
        
        // 加载禁止的生物列表
        bannedEntities = new HashSet<>();
        List<String> bannedList = config.getStringList("banned-entities");
        for (String entityName : bannedList) {
            try {
                EntityType entityType = EntityType.valueOf(entityName);
                bannedEntities.add(entityType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("未知的生物类型: " + entityName);
            }
        }
        
        if (debug) {
            plugin.getLogger().info("配置文件加载完成");
            plugin.getLogger().info("抓取概率: " + (captureChance * 100) + "%");
            plugin.getLogger().info("冷却时间: " + cooldown + "秒");
            plugin.getLogger().info("禁止生物数量: " + bannedEntities.size());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    // Getter 方法
    public double getCaptureChance() {
        return captureChance;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean isDebug() {
        return debug;
    }

    public Set<EntityType> getBannedEntities() {
        return bannedEntities;
    }

    public boolean isEntityBanned(EntityType entityType) {
        return bannedEntities.contains(entityType);
    }

    public boolean isShowCaptureChance() {
        return showCaptureChance;
    }

    public boolean isEnableParticles() {
        return enableParticles;
    }

    public boolean isEnableSounds() {
        return enableSounds;
    }
}