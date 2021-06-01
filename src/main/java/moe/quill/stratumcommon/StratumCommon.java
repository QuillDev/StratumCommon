package moe.quill.stratumcommon;

import moe.quill.StratumCommon.Database.IDatabaseService;
import moe.quill.StratumCommon.KeyManager.IKeyManager;
import moe.quill.StratumCommon.Serialization.ISerializer;
import moe.quill.stratumcommon.Services.DatabaseService;
import moe.quill.stratumcommon.Services.KeyManager;
import moe.quill.stratumcommon.Services.StratumSerialization;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class StratumCommon extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Create our services
        final var stratumSerialization = new StratumSerialization();
        final var keyManager = new KeyManager();
        final var databaseService = new DatabaseService(this);

        //Get the services manager
        final var servicesManager = getServer().getServicesManager();

        //Register our services
        servicesManager.register(ISerializer.class, stratumSerialization, this, ServicePriority.Highest);
        servicesManager.register(IKeyManager.class, keyManager, this, ServicePriority.Highest);
        servicesManager.register(IDatabaseService.class, databaseService, this, ServicePriority.Highest);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
