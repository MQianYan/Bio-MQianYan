package com.MQianYan.qbio.listeners;

import com.MQianYan.qbio.QBioPlugin;
import com.MQianYan.qbio.managers.BucketManager;
import com.MQianYan.qbio.managers.MessageManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class BucketInteractListener implements Listener {
    
    private final QBioPlugin plugin;
    private final BucketManager bucketManager;
    private final MessageManager messageManager;

    public BucketInteractListener(QBioPlugin plugin) {
        this.plugin = plugin;
        this.bucketManager = plugin.getBucketManager();
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 检查是否是空的生物桶
        if (!bucketManager.isBioBucket(item) || !bucketManager.isEmptyBioBucket(item)) {
            return;
        }
        
        // 检查冷却
        if (bucketManager.isOnCooldown(player)) {
            long remaining = bucketManager.getCooldownRemaining(player);
            player.sendMessage(messageManager.getCooldownMessage((int) remaining));
            event.setCancelled(true);
            return;
        }
        
        Entity entity = event.getRightClicked();
        
        // 检查是否禁止捕获
        if (!bucketManager.canCaptureEntity(entity.getType())) {
            player.sendMessage(messageManager.getBannedEntityMessage());
            event.setCancelled(true);
            return;
        }
        
        // 检查概率
        if (!bucketManager.shouldCaptureSuccess()) {
            player.sendMessage(messageManager.getCaptureFailedMessage());
            bucketManager.setCooldown(player);
            event.setCancelled(true);
            return;
        }
        
        // 捕获生物
        ItemStack newBucket = bucketManager.captureEntity(item, entity);
        player.getInventory().setItemInMainHand(newBucket);
        
        player.sendMessage(messageManager.getCaptureSuccessMessage(entity));
        bucketManager.setCooldown(player);
        
        // 移除原生物
        entity.remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void onBucketUse(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 检查是否是已装载的生物桶
        if (!bucketManager.isBioBucket(item) || bucketManager.isEmptyBioBucket(item)) {
            return;
        }
        
        // 检查冷却
        if (bucketManager.isOnCooldown(player)) {
            long remaining = bucketManager.getCooldownRemaining(player);
            player.sendMessage(messageManager.getCooldownMessage((int) remaining));
            event.setCancelled(true);
            return;
        }
        
        // 修复后的生成位置计算
        Location spawnLoc;
        if (event.getClickedBlock() != null && event.getBlockFace() != null) {
            // 在点击的方块面向玩家的相邻位置生成
            spawnLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
        } else {
            // 在玩家位置上方1格处生成
            spawnLoc = player.getLocation().add(0, 1, 0);
        }
            
        Entity entity = bucketManager.releaseEntity(item, spawnLoc);
        
        if (entity != null) {
            player.sendMessage(messageManager.getReleaseSuccessMessage(entity));
            
            // 将桶恢复为空状态
            PlayerInventory inventory = player.getInventory();
            int slot = inventory.getHeldItemSlot();
            inventory.setItem(slot, bucketManager.createEmptyBioBucket());
            
            bucketManager.setCooldown(player);
        } else {
            player.sendMessage(messageManager.getMessage("release.failed"));
        }
        
        event.setCancelled(true);
    }
}