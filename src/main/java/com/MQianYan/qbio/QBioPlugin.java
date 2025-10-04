package com.MQianYan.qbio;

import com.MQianYan.qbio.commands.BioCommand;
import com.MQianYan.qbio.listeners.BucketInteractListener;
import com.MQianYan.qbio.managers.BucketManager;
import com.MQianYan.qbio.managers.ConfigManager;
import com.MQianYan.qbio.managers.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class QBioPlugin extends JavaPlugin {
    
    private static QBioPlugin instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private BucketManager bucketManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化管理器
        initializeManagers();
        
        // 注册事件监听器
        registerListeners();
        
        // 注册命令
        registerCommands();
        
        // 发送启用消息
        getLogger().info(getMessageManager().getPluginEnableMessage());
    }

    @Override
    public void onDisable() {
        getLogger().info("QBio插件已卸载");
    }

    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.bucketManager = new BucketManager(this);
        
        configManager.loadConfig();
        messageManager.loadMessages();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BucketInteractListener(this), this);
    }

    private void registerCommands() {
        BioCommand bioCommand = new BioCommand(this);
        getCommand("bio").setExecutor(bioCommand);
        getCommand("bio").setTabCompleter(bioCommand);
    }

    public static QBioPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public BucketManager getBucketManager() {
        return bucketManager;
    }
}