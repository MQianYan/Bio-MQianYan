package com.MQianYan.qbio.managers;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

import java.util.*;

public class BucketManager {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    
    private final NamespacedKey bioBucketKey;
    private final NamespacedKey entityTypeKey;
    private final Map<UUID, Long> cooldowns;

    public BucketManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = ((com.MQianYan.qbio.QBioPlugin) plugin).getConfigManager();
        this.messageManager = ((com.MQianYan.qbio.QBioPlugin) plugin).getMessageManager();
        
        this.bioBucketKey = new NamespacedKey(plugin, "bio_bucket");
        this.entityTypeKey = new NamespacedKey(plugin, "entity_type");
        this.cooldowns = new HashMap<>();
    }

    public ItemStack createEmptyBioBucket() {
        ItemStack bucket = new ItemStack(Material.BARREL);
        ItemMeta meta = bucket.getItemMeta();
        
        if (meta == null) return bucket;
        
        double chance = configManager.getCaptureChance() * 100;
        String chanceStr = String.format("%.1f", chance);
        
        // 设置物品显示
        meta.setDisplayName(messageManager.getMessage("item.empty-name"));
        
        // 设置物品描述
        List<String> lore = new ArrayList<>();
        for (String line : getEmptyBucketLore()) {
            lore.add(line.replace("{chance}", chanceStr)
                        .replace("{cooldown}", String.valueOf(configManager.getCooldown())));
        }
        meta.setLore(lore);
        
        // 添加发光效果
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // 设置持久化数据
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(bioBucketKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(entityTypeKey, PersistentDataType.STRING, "EMPTY");
        
        bucket.setItemMeta(meta);
        return bucket;
    }

    private List<String> getEmptyBucketLore() {
        List<String> loreLines = new ArrayList<>();
        loreLines.add(messageManager.getMessage("item.empty-lore.0"));
        if (configManager.isShowCaptureChance()) {
            loreLines.add(messageManager.getMessage("item.empty-lore.1"));
        }
        loreLines.add(messageManager.getMessage("item.empty-lore.2"));
        return loreLines;
    }

    public boolean isBioBucket(ItemStack item) {
        if (item == null || item.getType() != Material.BARREL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(bioBucketKey, PersistentDataType.BYTE);
    }

    public boolean isEmptyBioBucket(ItemStack item) {
        if (!isBioBucket(item)) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String entityType = pdc.get(entityTypeKey, PersistentDataType.STRING);
        return "EMPTY".equals(entityType);
    }

    public ItemStack captureEntity(ItemStack bucket, Entity entity) {
        ItemStack newBucket = bucket.clone();
        ItemMeta meta = newBucket.getItemMeta();
        if (meta == null) return bucket;
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(entityTypeKey, PersistentDataType.STRING, entity.getType().toString());
        
        // 更新物品显示
        String entityName = getEntityDisplayName(entity);
        meta.setDisplayName(messageManager.getMessage("item.filled-name")
            .replace("{entity}", entityName));
        
        List<String> lore = new ArrayList<>();
        lore.add(messageManager.getMessage("item.filled-lore.0")
            .replace("{entity}", entityName));
        lore.add(messageManager.getMessage("item.filled-lore.1"));
        meta.setLore(lore);
        
        newBucket.setItemMeta(meta);
        
        // 播放特效
        playCaptureEffects(entity.getLocation());
        
        return newBucket;
    }

    public Entity releaseEntity(ItemStack bucket, Location location) {
        if (!isBioBucket(bucket) || isEmptyBioBucket(bucket)) return null;
        
        ItemMeta meta = bucket.getItemMeta();
        if (meta == null) return null;
        
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String entityTypeStr = pdc.get(entityTypeKey, PersistentDataType.STRING);
        
        if (entityTypeStr == null || "EMPTY".equals(entityTypeStr)) return null;
        
        try {
            EntityType entityType = EntityType.valueOf(entityTypeStr);
            Entity entity = location.getWorld().spawnEntity(location, entityType);
            
            // 播放特效
            playReleaseEffects(location);
            
            return entity;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("未知的生物类型: " + entityTypeStr);
            return null;
        }
    }

    private void playCaptureEffects(Location location) {
        if (configManager.isEnableParticles()) {
            World world = location.getWorld();
            if (world != null) {
                // 使用正确的粒子名称
                world.spawnParticle(Particle.EXPLOSION, location, 3);
                world.spawnParticle(Particle.LARGE_SMOKE, location, 15, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        if (configManager.isEnableSounds()) {
            World world = location.getWorld();
            if (world != null) {
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.0f);
            }
        }
    }

    private void playReleaseEffects(Location location) {
        if (configManager.isEnableParticles()) {
            World world = location.getWorld();
            if (world != null) {
                // 使用正确的粒子名称
                world.spawnParticle(Particle.EXPLOSION, location, 2);
                world.spawnParticle(Particle.CLOUD, location, 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
        
        if (configManager.isEnableSounds()) {
            World world = location.getWorld();
            if (world != null) {
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.2f);
            }
        }
    }

    private String getEntityDisplayName(Entity entity) {
        if (entity.getCustomName() != null) {
            return ChatColor.stripColor(entity.getCustomName());
        }
        
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
        entityNames.put(EntityType.WOLF, "狼");
        entityNames.put(EntityType.OCELOT, "豹猫");
        entityNames.put(EntityType.HORSE, "马");
        entityNames.put(EntityType.VILLAGER, "村民");
        
        return entityNames.getOrDefault(entity.getType(), 
               entity.getType().getKey().getKey());
    }

    public boolean canCaptureEntity(EntityType entityType) {
        return !configManager.isEntityBanned(entityType);
    }

    public boolean shouldCaptureSuccess() {
        double chance = configManager.getCaptureChance();
        return Math.random() <= chance;
    }

    public boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        
        long lastUse = cooldowns.get(player.getUniqueId());
        int cooldownTime = configManager.getCooldown() * 1000;
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }

    public long getCooldownRemaining(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return 0;
        
        long lastUse = cooldowns.get(player.getUniqueId());
        int cooldownTime = configManager.getCooldown() * 1000;
        long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
        return Math.max(0, (remaining + 999) / 1000); // 向上取整
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }
}